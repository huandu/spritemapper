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

import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import dk.cego.spritemapper.cli.*;
import dk.cego.spritemapper.spritehandlers.*;
import dk.cego.spritemapper.spritecomparators.*;

public class SpriteMapperCLI {
    private final static String DEFAULT_FILE_MATCHER = "\\.((png)|(jpe?g)|(gif))$";

    public static void main(String args[]) throws IOException {
        List<FormatOutput> metaOut = new LinkedList<FormatOutput>();
        File out = new File("spritemap.png");
        File directory = null;
        int maxWidth = 1000;
        boolean drawFrames = false;
        boolean trim = true;
        boolean reserveBorder = false;
        String fileMatcherRegex = DEFAULT_FILE_MATCHER;

        Pattern valuePattern = Pattern.compile("[^=]*=(.*)");
        Matcher matcher;

        for (String arg : args) {
            if (arg.startsWith("--zwoptex2") && (matcher = valuePattern.matcher(arg)).find()) {
                metaOut.add(new FormatOutput("zwoptex2", new File(matcher.group(1))));
            } else if (arg.startsWith("--out") && (matcher = valuePattern.matcher(arg)).find()) {
                out = new File(matcher.group(1));
            } else if (arg.startsWith("--max-width") && (matcher = valuePattern.matcher(arg)).find()) {
                maxWidth = Integer.parseInt(matcher.group(1));
            } else if (arg.startsWith("--draw-frames") && (matcher = valuePattern.matcher(arg)).find()) {
                drawFrames = Boolean.parseBoolean(matcher.group(1));
            } else if (arg.startsWith("--trim") && (matcher = valuePattern.matcher(arg)).find()) {
                trim = Boolean.parseBoolean(matcher.group(1));
            } else if (arg.startsWith("--reserve-border") && (matcher = valuePattern.matcher(arg)).find()) {
            	reserveBorder = Boolean.parseBoolean(matcher.group(1));
            } else if (arg.startsWith("--file-pattern") && (matcher = valuePattern.matcher(arg)).find()) {
                fileMatcherRegex = matcher.group(1);
            } else {
                directory = new File(arg);
            }
        }

        if (out == null || directory == null) {
            help();
        } else {
            FileMatcher fileMatcher = new CompositeFileMatcher()
                .add(new FileTypeFileMatcher(FileTypeFileMatcher.FileType.FILE))
                .add(new RegexFileMatcher(fileMatcherRegex));

            Scanner s = new Scanner(directory);
            Set<File> files = s.scan(fileMatcher);

            List<Sprite> sprites = new SpriteImporter().importSprites(directory, files);

            SpriteMapper mapper = new SpriteMapper(sprites)
                                        .setTrim(trim)
                                        .setReserveBorder(reserveBorder)
                                        .setSpritePreHandler(new Landscape())
                                        .setSpriteSorter(new AreaComparator())
                                        .setLayouter(new OptimalAlgorithmLayouter())
                                        .doLayout(maxWidth);

            String fileFormat = fileExtension(out);
            BufferedImage result = getImageForFormat(fileFormat, mapper.getLayoutDimension());
            mapper.paint((Graphics2D)result.getGraphics(), drawFrames);
            ImageIO.write(result, fileExtension(out), out);

            if (metaOut.isEmpty()) {
                metaOut.add(new FormatOutput("zwoptex2", new File("spritemap.plist")));
            }

            for (FormatOutput format : metaOut) {
                try {
                    format.writeMetaData(mapper);
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }

    private final static BufferedImage getImageForFormat(String format, Dimension size) {
        return new BufferedImage(
            size.width,
            size.height,
            (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB
        );
    }

    private final static void help() {
        System.out.println("Usage: java -jar SpriteMapper.jar [options...] <image dir>");
        System.out.println("  Options and default values:");
        System.out.println("    --zwoptex2=spritemap.plist - Output metadata to 'spritemap.plist' in Zwoptex2 format.");
        System.out.println("    --out=spritemap.png        - Output sprite map to 'spritemap.png'.");
        System.out.println("    --max-width=1000           - Set maximum width of sprite map to 1000 pixels.");
        System.out.println("    --draw-frames=false        - Draw frames around images in sprite map.");
        System.out.println("    --trim=true                - Trim transparent edges.");
        System.out.println("    --reserve-border=false     - Reserve 1px transparent border for each trimmed image.");
        System.out.println("    --file-pattern=" + DEFAULT_FILE_MATCHER);
        System.out.println("                               - Only include files matching this pattern.");
    }

    private final static String fileExtension(File f) {
        String name = f.getName();
        int idx = name.lastIndexOf(".");
        return name.substring(idx + 1);
    }
}
