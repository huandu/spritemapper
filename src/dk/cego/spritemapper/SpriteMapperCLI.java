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
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import dk.cego.spritemapper.cli.*;
import dk.cego.spritemapper.spritehandlers.*;
import dk.cego.spritemapper.spritecomparators.*;

class ArgumentException extends Exception {
    // it's a fake version uid. however, javac requires it. 
    static final long serialVersionUID = 0L;

    ArgumentException(String msg) {
        super(msg);
    }
}

public class SpriteMapperCLI {
    private final static String DEFAULT_FILE_MATCHER = "\\.((png)|(jpe?g)|(gif))$";
    private final static String OPTION_ARGUMENT_PATTERN = "^--([^=]+)(=(.*))?$";
    private final static int DEFAULT_IMAGE_WIDTH = 1024;
    private final static String DEFAULT_BASE_DIR = ".";
    private final static String DEFAULT_ZWOPTEX2_PLIST_NAME = "zwoptex2.plist";
    private final static String DEFAULT_OUTPUT_NAME = "spritemap.png";
    private final static String DEFAULT_ALGORITHM = "maxrects";

    public static void main(String args[]) throws IOException {
        if (args.length == 0) {
            help();
            System.exit(1);
            return;
        }

        List<FormatOutput> metaOut = new LinkedList<FormatOutput>();
        File out = new File(DEFAULT_OUTPUT_NAME);
        File baseDir = new File(DEFAULT_BASE_DIR);
        List<File> images = new LinkedList<File>();
        OptimalAlgorithmLayouter layouter = new OptimalAlgorithmLayouter();

        int maxWidth = DEFAULT_IMAGE_WIDTH;
        boolean usePOTSize = false;
        boolean drawFrames = false;
        boolean trim = true;
        int spacing = 0;
        int border = 0;
        boolean reserveDirName = false;
        RegexFileMatcher fileMatcherRegex = new RegexFileMatcher(DEFAULT_FILE_MATCHER);
        String algorithm = DEFAULT_ALGORITHM;

        Map<String, List<String>> arguments = parseArguments(args);
        Map.Entry<String, List<String>> currentEntry = null;

        try {
            for (Map.Entry<String, List<String>> entry: arguments.entrySet()) {
                String option = entry.getKey();
                List<String> arg = entry.getValue();
                currentEntry = entry;

                if (option.equals("")) {
                    for (String f: arg) {
                        File fileOrDir = new File(baseDir, f);

                        if (!fileOrDir.exists()) {
                            throw new ArgumentException("File or directory \"" + f + "\" doesn't exist in base dir \"" + baseDir.toString() + "\".");
                        }

                        images.add(fileOrDir);
                    }

                // input options.
                } else if (option.equals("base-dir")) {
                    baseDir = new File(argument(arg, DEFAULT_BASE_DIR));

                    if (!baseDir.exists()) {
                        throw new ArgumentException("Base dir \"" + arg + "\" doesn't exist.");
                    }
                } else if (option.equals("file-pattern")) {
                    fileMatcherRegex = new RegexFileMatcher(argument(arg, DEFAULT_FILE_MATCHER));
                } else if (option.equals("zwoptex2")) {
                    metaOut.add(new FormatOutput("zwoptex2", new File(argument(arg, DEFAULT_ZWOPTEX2_PLIST_NAME))));
                } else if (option.equals("reserve-dir-name")) {
                    reserveDirName = Boolean.parseBoolean(argument(arg, "true"));

                // metadata options.
                } else if (option.equals("out")) {
                    out = new File(argument(arg, DEFAULT_OUTPUT_NAME));
                } else if (option.equals("algorithm")) {
                    algorithm = argument(arg);
                } else if (option.equals("max-width")) {
                    maxWidth = Integer.parseInt(argument(arg));

                    if (maxWidth < 0) {
                        maxWidth = 0;
                    }
                } else if (option.equals("pot-size")) {
                    usePOTSize = Boolean.parseBoolean(argument(arg, "true"));
                } else if (option.equals("draw-frames")) {
                    drawFrames = Boolean.parseBoolean(argument(arg, "true"));
                } else if (option.equals("trim")) {
                    trim = Boolean.parseBoolean(argument(arg, "true"));
                } else if (option.equals("spacing")) {
                    spacing = Integer.parseInt(argument(arg));

                    if (spacing < 0) {
                        spacing = 0;
                    }
                } else if (option.equals("border")) {
                    border = Integer.parseInt(argument(arg));

                    if (border < 0) {
                        border = 0;
                    }

                // others.
                } else if (option.equals("help")) {
                    help();
                    return;
                } else {
                    throw new ArgumentException("Unknown option \"" + originalArgument(entry) + "\".");
                }
            }

            if (images.isEmpty()) {
                throw new ArgumentException("Must set at least one input image file or directory.");
            }

            if (metaOut.isEmpty()) {
                throw new ArgumentException("Must set at least one metadata format.");
            }

            String[] algorithms = algorithm.split(",");

            for (String a: algorithms) {
                if (!layouter.add(a.trim())) {
                    throw new ArgumentException("Unknown packing algorithm '" + a.trim() + "'.");
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Error: In option \"" + originalArgument(currentEntry) + "\"");
            System.err.println(e);
            System.out.println("");
            help();
            System.exit(1);
            return;
        } catch (ArgumentException e) {
            System.out.println(e);
            System.out.println("");
            help();
            System.exit(1);
            return;
        }

        FileMatcher fileMatcher = new CompositeFileMatcher()
        .add(new FileTypeFileMatcher(FileTypeFileMatcher.FileType.FILE))
        .add(fileMatcherRegex);

        Scanner scanner = new Scanner(baseDir);
        Set<File> files = new TreeSet<File>();

        for (File directory: images) {
            if (directory.isDirectory()) {
                scanner.scan(files, directory, fileMatcher);
            } else {
                files.add(directory);
            }
        }

        List<Sprite> sprites = new SpriteImporter().importSprites(baseDir, files, reserveDirName);
        layouter.setUsePOTSize(usePOTSize)
        .setBorder(border)
        .setSpacing(spacing);

        SpriteMapper mapper = new SpriteMapper(sprites)
        .setTrim(trim)
        .setSpritePreHandler(new Landscape())
        .setSpriteSorter(new AreaComparator())
        .setLayouter(layouter)
        .doLayout(maxWidth);

        // draw output image.
        String fileFormat = fileExtension(out);
        BufferedImage result = getImageForFormat(fileFormat, mapper.getLayoutDimension(), border, usePOTSize);
        Graphics2D graphics = result.createGraphics();
        graphics.translate(border, border);
        mapper.paint(graphics, drawFrames);
        graphics.dispose();
        ImageIO.write(result, fileExtension(out), out);

        for (FormatOutput format : metaOut) {
            try {
                format.writeMetaData(mapper);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private final static BufferedImage getImageForFormat(String format, Dimension size, int border, boolean usePOTSize) {
        size.width += 2 * border;
        size.height += 2 * border;

        if (usePOTSize) {
            size.width = OptimalAlgorithmLayouter.toUpperPOT(size.width);
            size.height = OptimalAlgorithmLayouter.toUpperPOT(size.height);
        }

        return new BufferedImage(
            size.width,
            size.height,
            (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB
        );
    }

    private final static void help() {
        System.out.println("SpriteMapper can pack several image files and/or directories into one png file with minimum size.");
        System.out.println("");
        System.out.println("Usage: java -jar SpriteMapper.jar [options...] <image files or dirs...>");
        System.out.println("");
        System.out.println("Input options:");
        System.out.println("    --base-dir=.               - Base dir of image files and directories.");
        System.out.println("    --file-pattern=" + DEFAULT_FILE_MATCHER);
        System.out.println("                               - Only include files matching this pattern.");
        System.out.println("");
        System.out.println("Metadata options:");
        System.out.println("    --zwoptex2=zwoptex2.plist  - Output metadata in Zwoptex2 general plist format.");
        System.out.println("    --reserve-dir-name=false   - Reserve dir name for frame keys in metadata file.");
        System.out.println("");
        System.out.println("Packing options:");
        System.out.println("    --out=spritemap.png        - Output sprite map to 'spritemap.png'.");
        System.out.println("    --algorithm=maxrects       - Set packing algorithm. Can be 'maxrects', 'guillotine' and/or 'shelf'.");
        System.out.println("                                 Multiple algorithms can be used together, e.g. 'maxrects,guillotine,shelf'.");
        System.out.println("                                 The most optimal algorithm will be chosen for final output.");
        System.out.println("    --max-width=1024           - Set maximum width of sprite map to 1024 pixels.");
        System.out.println("    --pot-size=false           - Use POT (Power Of Two) value for width and height of sprite map.");
        System.out.println("    --draw-frames=false        - Draw frames around images in sprite map.");
        System.out.println("    --trim=true                - Trim transparent edges.");
        System.out.println("    --spacing=0                - Set sprite spacing.");
        System.out.println("    --border=0                 - Set border padding.");
        System.out.println("");
        System.out.println("Others:");
        System.out.println("    --help                     - Show this help message.");
    }

    private final static String fileExtension(File f) {
        String name = f.getName();
        int idx = name.lastIndexOf(".");
        return name.substring(idx + 1);
    }

    private final static Map<String, List<String>> parseArguments(String[] args) {
        Pattern valuePattern = Pattern.compile(OPTION_ARGUMENT_PATTERN);
        Map<String, List<String>> map = new TreeMap<String, List<String>>();
        String option;
        String value;

        for (String arg: args) {
            Matcher matcher = valuePattern.matcher(arg);

            if (!matcher.find()) {
                List<String> list = map.get("");

                if (list == null) {
                    list = new LinkedList<String>();
                }

                list.add(arg);
                map.put("", list);

                continue;
            }

            option = matcher.group(1);
            value = matcher.group(3);

            List<String> list = map.get(option);

            if (list == null) {
                list = new LinkedList<String>();
            }

            list.add(value);
            map.put(option, list);
        }

        return map;
    }

    private final static String argument(List<String> arg) {
        return argument(arg, "");
    }

    private final static String argument(List<String> arg, String def) {
        String result = arg.get(0);

        if (result == null || result.equals("")) {
            return def;
        }

        return result;
    }

    private final static String originalArgument(Map.Entry<String, List<String>> entry) {
        String result = "--" + entry.getKey();
        String arg = argument(entry.getValue(), null);

        if (arg == null) {
            return result;
        }
        
        result += "=" + arg;
        return result;
    }
}
