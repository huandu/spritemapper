/**
 * Copyright (C) 2013 Huan Du <i@huandu.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.cego.spritemapper;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dk.cego.spritemapper.config.Config;
import dk.cego.spritemapper.config.InputConfig;
import dk.cego.spritemapper.config.MetaConfig;
import dk.cego.spritemapper.config.OutputConfig;
import dk.cego.spritemapper.config.TextureConfig;
import dk.cego.spritemapper.guillotine.OptimalGuillotineLayouter;
import dk.cego.spritemapper.maxrects.OptimalMaxRectsLayouter;
import dk.cego.spritemapper.shelf.ShelfLayouter;
import dk.cego.spritemapper.spritecomparators.AreaComparator;
import dk.cego.spritemapper.spritehandlers.Landscape;

import com.esotericsoftware.wildcard.Paths;

/**
 * SpriteMapperRunner can use config to generate output.
 */
public class SpriteMapperRunner {
	private Config config;
	
	private File baseDir = new File(".");
	private OptimalLayouter layouter = new OptimalLayouter();
	private int maxWidth = 1024;
	private int maxHeight = 0;
	private boolean usePOTSize = false;
	private boolean drawFrames = false;
	private boolean trim = false;
	private int spacing = 0;
	private int border = 0;
	
	public SpriteMapperRunner(Config config) throws ArgumentException {
		this.config = config;
		parseConfig();
	}
	
	public void run() throws IOException, ArgumentException {
        List<File> files = scanInput();
        
        // skip this config if files set is empty.
        if (files.isEmpty()) {
        	return;
        }

        List<Sprite> sprites = new SpriteImporter().importSprites(baseDir, files);
        
        layouter.setUsePOTSize(usePOTSize)
        .setBorder(border)
        .setSpacing(spacing);

        SpriteMapper mapper = new SpriteMapper(sprites);
        
        mapper.setTrim(trim)
        .setSpritePreHandler(new Landscape())
        .setSpriteSorter(new AreaComparator())
        .setLayouter(layouter)
        .doLayout(maxWidth, maxHeight);
        
        Dimension[] dimensions = mapper.getLayoutDimension();

    	// consider border and POT size options.
        if (border > 0 || usePOTSize) {
        	for (Dimension d : dimensions) {
        		d.width += 2 * border;
                d.height += 2 * border;

                if (usePOTSize) {
                    d.width = OptimalAlgorithmLayouter.toUpperPOT(d.width);
                    d.height = OptimalAlgorithmLayouter.toUpperPOT(d.height);
                }
        	}
        }

        // write output image files.
        for (OutputConfig output : config.outputConfigList) {
        	// write images.
        	for (TextureConfig texture : output.textureConfigList) {
        		mapper.doWriteImages(texture.path, texture.type, dimensions, getImageType(texture.type), drawFrames);
        	}
        	
        	String texturePath = "";
        	
        	// meta data is associated to the first texture.
        	if (!output.textureConfigList.isEmpty()) {
        		texturePath = output.textureConfigList.get(0).path;
        	}
            
            // write meta data.
            for (MetaConfig meta : output.metaConfigList) {
            	SpriteMapperMetaStream stream = MetaStreamFactory.newInstance(meta.type);
            	
            	if (stream == null) {
            		throw new RuntimeException("Cannot create meta stream. Type: " + meta.type);
            	}
            	
            	mapper.doMetaStream(meta.path, texturePath, stream, dimensions, meta.keepDir);
            }
        }
	}
	
	private final void parseConfig() throws ArgumentException {
		Map<String, String> options = config.options;
		String value;
		
		// base dir.
		value = options.get("base-dir");
		
		if (value != null) {
			baseDir = new File(value);
			
			if (!baseDir.canRead()) {
                throw new ArgumentException("Base dir is not readable. Dir: " + baseDir.getPath());
            }
			
			if (!baseDir.isDirectory()) {
				throw new ArgumentException("Base dir is not a directory. Dir: " + baseDir.getPath());
			}
		}
		
		// algorithm.
		value = options.get("algorithm");
		
		if (value == null) {
			// use "maxrects" as default.
			layouter.add(new OptimalMaxRectsLayouter());
		} else {
			String[] algorithms = value.split(",");
			
			for (String a : algorithms) {
				String algorithm = a.trim();
				
				if (algorithm.equals("maxrects")) {
					layouter.add(new OptimalMaxRectsLayouter());
				} else if (algorithm.equals("guillotine")) {
					layouter.add(new OptimalGuillotineLayouter());
				} else if (algorithm.equals("shelf")) {
					layouter.add(new ShelfLayouter());
				} else {
					throw new ArgumentException("Unsupported algorithm. Algorithm: " + algorithm);
				}
			}
		}
		
		// max width.
		value = options.get("max-width");
		
		if (value != null) {
			maxWidth = Integer.parseInt(value);

            if (maxWidth <= 0) {
                throw new ArgumentException("Max width must be positive integer. Max width: " + value);
            }
		}
		
		// max height.
		value = options.get("max-height");
		
		if (value != null) {
			maxHeight = Integer.parseInt(value);

            if (maxHeight <= 0) {
            	throw new ArgumentException("Max height must be positive integer. Max height: " + value);
            }
		}
		
		// use pot size.
		value = options.get("use-pot-size");
		
		if (value != null) {
			usePOTSize = Boolean.parseBoolean(value);
		}
		
		// draw frames.
		value = options.get("draw-frames");
		
		if (value != null) {
			drawFrames = Boolean.parseBoolean(value);
		}
		
		// trim.
		value = options.get("trim");
		
		if (value != null) {
			trim = Boolean.parseBoolean(value);
		}
		
		// spacing.
		value = options.get("spacing");
		
		if (value != null) {
			spacing = Integer.parseInt(value);

            if (spacing < 0) {
            	throw new ArgumentException("Spacing must be positive or 0 integer. Max height: " + value);
            }
		}
		
		// border.
		value = options.get("border");
		
		if (value != null) {
			border = Integer.parseInt(value);

            if (border < 0) {
            	throw new ArgumentException("Spacing must be positive or 0 integer. Max height: " + value);
            }
		}
	}
	
	/**
	 * Scan base-dir and filter files with input config using OR logic.
	 * @return
	 * @throws ArgumentException
	 * @throws IOException 
	 */
	private List<File> scanInput() throws ArgumentException, IOException {
		if (config.inputConfigList.isEmpty()) {
			throw new ArgumentException("Must set at least one input image file or directory.");
		}
		
		Paths paths = new Paths();
		String base = baseDir.getCanonicalPath();
		
		for (InputConfig input : config.inputConfigList) {
			File inputFile = new File(input.path);
			String inputBase = base;
			
			if (inputFile.isAbsolute()) {
				inputBase = "/";
			} else {
				inputFile = new File(baseDir, input.path);
			}
			
			if (inputFile.isFile()) {
				paths.addFile(inputFile.getCanonicalPath());
				continue;
			}
			
			List<String> filters = new LinkedList<String>();
			
			if (inputFile.isDirectory()) {
				filters.add(input.path + "/**");
			} else if (input.path.endsWith("/")) {
				filters.add(input.path + "**");
			} else {
				filters.add(input.path);
			}
			
			filters.addAll(config.filters);
			paths.glob(inputBase, filters);
		}
		
		paths = paths.filesOnly();
		
		// make sure files are sorted by name as file sequence affects packed image content.
		// if we don't do this, SpriteMapper cannot generate stable result.
		List<File> files = paths.getFiles();
		Collections.sort(files);
		return files;
	}
	
	private final static int getImageType(String format) {
    	if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
    		return BufferedImage.TYPE_INT_RGB;
    	}
    	
    	return BufferedImage.TYPE_INT_ARGB;
    }
}
