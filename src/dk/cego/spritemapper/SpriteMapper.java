/**
 * Copyright (C) 2011 CEGO ApS
 * Written by Robert Larsen <robert@komogvind.dk> for CEGO ApS
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.awt.geom.AffineTransform;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import dk.cego.spritemapper.util.OutputFilename;

public class SpriteMapper {
    private List<Sprite> sprites;

    private boolean trim = true;
    private ObjectHandler<Sprite> spritePreHandler = null;
    private Comparator<Sprite> spriteSorter = null;
    private SpriteLayouter layouter = null;
    private int maxMapNumber = 0;

    public SpriteMapper(List<Sprite> sprites) {
        this.sprites = sprites;
    }

    public SpriteMapper setTrim(boolean trim) {
        this.trim = trim;
        return this;
    }

    public SpriteMapper setSpriteSorter(Comparator<Sprite> spriteSorter) {
        this.spriteSorter = spriteSorter;
        return this;
    }

    public SpriteMapper setSpritePreHandler(ObjectHandler<Sprite> spritePreHandler) {
        this.spritePreHandler = spritePreHandler;
        return this;
    }

    public SpriteMapper setLayouter(SpriteLayouter layouter) {
        this.layouter = layouter;
        return this;
    }

    public SpriteMapper doLayout(int maxWidth, int maxHeight) {
        if (trim) {
            forEachSprite(new SpriteTrimmer());
        }

        if (spritePreHandler != null) {
            forEachSprite(spritePreHandler);
        }

        if (spriteSorter != null) {
            Collections.sort(sprites, spriteSorter);
        }

        if (layouter != null) {
            maxMapNumber = layouter.layout(maxWidth, maxHeight, sprites);
        }

        return this;
    }

    public Dimension[] getLayoutDimension() {
    	if (maxMapNumber == 0) {
    		throw new RuntimeException("Cannot get sprites dimension. There is no sprite map is created.");
    	}
    	
    	return Sprite.dimensions(sprites, maxMapNumber);
    }

    public SpriteMapper doMetaStream(String output, String texture, SpriteMapperMetaStream stream, Dimension[] dimensions, boolean keepDir) throws IOException {
        if (stream == null) {
        	throw new RuntimeException("Meta stream must be valid.");
        }
        
    	if (dimensions.length != maxMapNumber) {
    		throw new RuntimeException("Dimension array's length must equal to current max map number.");
    	}
    	
    	OutputFilename outFilename = OutputFilename.parseString(output);
    	outFilename.setMaxNumber(maxMapNumber);
    	
    	OutputFilename textureFilename = OutputFilename.parseString(texture);
    	textureFilename.setMaxNumber(maxMapNumber);
    	
    	for (int i = 0; i < maxMapNumber; i++) {
    		OutputStream out = new FileOutputStream(outFilename.filename());
    		String textureName = textureFilename.filename();
    		
    		// discard directory information if don't need to keep it.
    		if (!keepDir) {
    			File textureFile = new File(textureName);
    			textureName = textureFile.getName();
    		}
    		
    		// write meta file.
        	stream.write(textureName, sprites, i, dimensions[i], out);
    	}
        
        return this;
    }

	public SpriteMapper doWriteImages(String output, String type, Dimension[] dimensions, int imageType, boolean drawFrames) throws IOException {
        //Draw the sprites in memory.
        List<BufferedImage> images = new ArrayList<BufferedImage>(dimensions.length);
        Graphics2D[] graphics = new Graphics2D[dimensions.length];
        int number = 0;
        
        for (Dimension d : dimensions) {
	        BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
	        images.add(image);
	        graphics[number] = image.createGraphics();
	        number++;
        }
        
        Graphics2D g;
        
        for (Sprite s : sprites) {
        	AffineTransform t;
            BufferedImage img = s.image;
            
            if (s.rotated) {
                t = new AffineTransform(0, 1, -1, 0, s.x + img.getHeight(), s.y);
            } else {
                t = new AffineTransform(1, 0, 0, 1, s.x, s.y);
            }
            
        	g = graphics[s.mapNumber];
            g.drawImage(img, t, null);
            
            if (drawFrames) {
                g.setColor(java.awt.Color.red);
                g.drawRect(s.x, s.y, s.w - 1, s.h - 1);
            }
        }
        
        for (int i = 0, length = graphics.length; i < length; i++) {
        	graphics[i].dispose();
        }
        
        OutputFilename outFilename = OutputFilename.parseString(output);
    	outFilename.setMaxNumber(images.size());
    	
    	// save images to files.
        for (BufferedImage img : images) {
        	File outFile = new File(outFilename.filename());
    		ImageIO.write(img, type, outFile);
        }
        
        return this;
    }

    private SpriteMapper forEachSprite(ObjectHandler<Sprite> handler) {
        for (Sprite s : sprites) {
            handler.handle(s);
        }
        return this;
    }
}
