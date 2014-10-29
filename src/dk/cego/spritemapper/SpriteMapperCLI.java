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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.cego.spritemapper.config.Config;
import dk.cego.spritemapper.config.TextureConfig;
import dk.cego.spritemapper.config.MetaConfig;
import dk.cego.spritemapper.config.InputConfig;
import dk.cego.spritemapper.config.OutputConfig;
import dk.cego.spritemapper.config.Parser;

class ArgumentException extends Exception {
    // it's a fake version uid. however, javac requires it. 
    static final long serialVersionUID = 0L;

    ArgumentException(String msg) {
        super(msg);
    }
}

public class SpriteMapperCLI {
	private final static String VERSION = "2.1.0";
	private final static String OPTION_ARGUMENT_PATTERN = "^--([^=]+)(=(.*))?$";
	private final static String[] DEFAULT_FILE_INCLUDE_FILTER = {"*.jpg", "*.jpeg", "*.png", "*.gif"};

    public static void main(String args[]) throws IOException {
    	if (args.length == 0) {
            help();
            System.exit(1);
            return;
        }
        
        // argument default values.
        String config = "";
        boolean keepDir = false;
        List<String> images = new LinkedList<String>();
        
        // this config is only valid when --config is not set.
        Config singleConfig = new Config();
        List<TextureConfig> textureConfigList = new LinkedList<TextureConfig>();
        List<MetaConfig> metaConfigList = new LinkedList<MetaConfig>();
        
        // white-list for all supported but not checked arguments.
        Set<String> validArguments = new HashSet<String>();
        validArguments.add("base-dir");
        validArguments.add("algorithm");
        validArguments.add("max-width");
        validArguments.add("max-height");
        validArguments.add("use-pot-size");
        validArguments.add("draw-frames");
        validArguments.add("trim");
        validArguments.add("spacing");
        validArguments.add("border");

        try {
            Map<String, List<String>> arguments = parseArguments(args);
            
        	// verify and store all arguments.
            for (Map.Entry<String, List<String>> entry: arguments.entrySet()) {
                String option = entry.getKey();
                List<String> arg = entry.getValue();
                
                if (option.equals("")) {
                	images.addAll(arg);
                } else if (option.equals("config")) {
                	config = argument(arg);
                	
                	File configFile = new File(config);
                	
                	if (!configFile.canRead()) {
                		throw new ArgumentException("Config file is not readable. Config: " + configFile.getPath());
                	}
                } else if (option.equals("include")) {
                	singleConfig.filters.add(argument(arg));
                } else if (option.equals("exclude")) {
                	singleConfig.filters.add("!" + argument(arg));
                } else if (option.equals("zwoptex2")) {
                	MetaConfig metaConfig = new MetaConfig();
                	metaConfig.type = "zwoptex2";
                	metaConfig.path = argument(arg);
                	metaConfigList.add(metaConfig);
                } else if (option.equals("keep-dir")) {
                	keepDir = Boolean.parseBoolean(argument(arg, "true"));
                } else if (option.equals("output")) {
                	TextureConfig textureConfig = new TextureConfig();
                	textureConfig.path = argument(arg);
                	textureConfig.type = fileExtension(textureConfig.path);
                	
                    textureConfigList.add(textureConfig);
                } else if (validArguments.contains(option)) {
                    singleConfig.options.put(option, argument(arg));
                } else if (option.equals("version")) {
                	version();
                	return;
                } else if (option.equals("help")) {
                	help();
                    return;
                } else {
                	throw new ArgumentException("Unknown option \"" + originalArgument(entry) + "\".");
                }
            }
            
            List<Config> configs;

            if (config.isEmpty()) {
                // if there is no file filter, set a default one.
                if (singleConfig.filters.isEmpty()) {
                	singleConfig.filters.addAll(Arrays.asList(DEFAULT_FILE_INCLUDE_FILTER));
                }
                
                // add all images as input.
                for (String img : images) {
                	InputConfig inputConfig = new InputConfig();
                	inputConfig.path = img;
                	singleConfig.inputConfigList.add(inputConfig);
                }
                
                // if there is any meta file setting, update keep dir setting.
                for (MetaConfig meta : metaConfigList) {
                	meta.keepDir = keepDir;
                }
                
                // associated meta config to all output configs.
                OutputConfig outputConfig = new OutputConfig();
                outputConfig.textureConfigList = textureConfigList;
                outputConfig.metaConfigList = metaConfigList;
                singleConfig.outputConfigList.add(outputConfig);
                
                // create a list of configs.
                configs = new LinkedList<Config>();
                configs.add(singleConfig);
            } else {
            	// if config is set, ignore all images, input, output, meta and filters.
            	configs = Parser.parse(config, singleConfig.options);
            }
            
            // create runners and run.
            List<SpriteMapperRunner> runners = new ArrayList<SpriteMapperRunner>(configs.size());
            
            // create runners. config will be validated in this step.
            for (Config c : configs) {
            	runners.add(new SpriteMapperRunner(c));
            }
            
            // run sprite mapper with config.
            for (SpriteMapperRunner r : runners) {
            	r.run();
            }
        } catch (RuntimeException e) {
            System.err.println(e);
            e.printStackTrace(System.err);
            System.err.println();
            
            help();
            System.exit(1);
            return;
        } catch (ArgumentException e) {
            System.err.println(e);
            e.printStackTrace(System.err);
            System.err.println();
            
            help();
            System.exit(1);
            return;
        }
    }

    private final static void help() {
        System.out.println("SpriteMapper can pack several image files and/or directories into one png file with minimum size.");
        System.out.println();
        System.out.println("Usage: java -jar SpriteMapper.jar [--options...] <image files or dirs...>");
        System.out.println();
        System.out.println("Config file:");
        System.out.println("  --config=CONFIG_FILE_NAME  - Use config file to pack many different sprites to differnt sprite maps.");
        System.out.println("                               Once config file is set, image files and dirs in argument is ignored.");
        System.out.println("                               See online help document for config file structure.");
        System.out.println("                               https://github.com/huandu/spritemapper/blob/master/README.md#config-file");
        System.out.println();
        System.out.println("Filter options:");
        System.out.println("  --include=" + DEFAULT_FILE_INCLUDE_FILTER);
        System.out.println("                             - Only include files matching this pattern. Can set more than 1 filter.");
        System.out.println("  --exclude=PATTER           - Exclude files matching this pattern. Can set more than 1 filter.");
        System.out.println();
        System.out.println("Metadata options:");
        System.out.println("  --zwoptex2=meta{n}.plist   - Output metadata in Zwoptex2 general plist format.");
        System.out.println("  --keep-dir=false           - Keep dir name for frame keys in metadata file.");
        System.out.println();
        System.out.println("Output options:");
        System.out.println("  --output=spritemap{n}.png  - Output sprite map to 'spritemap{n}.png'. '{n}' is output sequence number.");
        System.out.println("                               Sequence number stars with 0 by default. Use '{n1}' to make the number");
        System.out.println("                               start with 1 instead of 0.");
        System.out.println("                               If --max-height is 0 or files can be packed in one sprite map, sequence");
        System.out.println("                               number will become an empty string. Use '{n!}' to force generating number.");
        System.out.println();
        System.out.println("Packing options:");
        System.out.println("  --base-dir=.               - Base dir of image files and directories.");
        System.out.println("  --algorithm=maxrects       - Set packing algorithm. Can be 'maxrects', 'guillotine' and/or 'shelf'.");
        System.out.println("                               Multiple algorithms can be used together, e.g. 'maxrects,guillotine,shelf'.");
        System.out.println("                               The most optimal algorithm will be chosen for final output.");
        System.out.println("  --max-width=1024           - Set maximum width. Default maximum width is 1024 pixels.");
        System.out.println("  --max-height=0             - Set maximum height. Default maximum height is 0, which means no limit.");
        System.out.println("                               If image files cannot be packed into one sprite due to max height, ");
        System.out.println("  --use-pot-size=false       - Use POT (Power Of Two) value for width and height of sprite map.");
        System.out.println("  --draw-frames=false        - Draw frames around images in sprite map.");
        System.out.println("  --trim=false               - Trim transparent edges.");
        System.out.println("  --spacing=0                - Set sprite spacing.");
        System.out.println("  --border=0                 - Set border padding.");
        System.out.println();
        System.out.println("Others:");
        System.out.println("  --version                  - Show SpriteMapper version number.");
        System.out.println("  --help                     - Show this help message.");
        System.out.println();
        System.out.println("SpriteMapper is maintained by Huan Du <i@huandu.me>. Visit project page to get help or report bugs.");
        System.out.println("Project Page: https://github.com/huandu/spritemapper");
    }
    
    private final static void version() {
    	System.out.println("SpriteMapper " + VERSION);
    }

    private final static String fileExtension(String name) {
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
