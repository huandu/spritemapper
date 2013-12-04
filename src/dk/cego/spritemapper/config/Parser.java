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
package dk.cego.spritemapper.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * SpriteMapperConfig is a config file can contain many pack options.
 * With this config, SpriteMapper can pack different images into different sprite maps.
 */
public class Parser {
	private final static String ROOT_ELEMENT = "spritemapper";
	private final static String OPTIONS_ELEMENT = "options";
	private final static String OPTION_ELEMENT = "option";
	private final static String SPRITES_ELEMENT = "sprites";
	private final static String SPRITE_ELEMENT = "sprite";
	private final static String INPUT_ELEMENT = "input";
	private final static String FILTER_ELEMENT = "filter";
	private final static String OUTPUT_ELEMENT = "output";
	private final static String TEXTURE_ELEMENT = "texture";
	private final static String META_ELEMENT = "meta";
	
	private final static String TYPE_ATTRIBUTE = "type";
	private final static String KEEP_DIR_ATTRIBUTE = "keep-dir";

	/**
	 * Parse config file.
	 * Config file is an xml file in following structure.
	 * <pre>{@code
	 * <?xml version="1.0" encoding="UTF-8"?>
	 * <spritemapper>
	 *   <!-- default options. option name is the same command line options. -->
	 *   <options>
	 *     <option type="base-dir">.</option>
	 *     <option type="max-width">2048</option>
	 *     <option type="max-height">2048</option>
	 *     <option type="trim">true</option>
	 *     <option type="spacing">1</option>
	 *   </options>
	 *   
	 *   <sprites>
	 *     <!-- define one sprite map. <sprite> property can overwrite any default option. -->
	 *     <sprite border="1">
	 *       <!-- input files or directories. -->
	 *       <input>res</input>
	 *       <input>some-image.png</input>
	 *     
	 *       <!-- file filters. -->
	 *       <filter type="include">*.jpg</filter>
	 *       <filter type="exclude">foo*.jpg</filter>
	 *       
	 *       <!-- output files. it's allowed to write more than one <output>. -->
	 *       <output>
	 *         <texture type="png">sample-output.png</texture>
	 *         <meta type="zwoptex2" keep-dir="true">sample-output.plist</meta>
	 *       </output>
	 *     </sprite>
	 *   
	 *     <!-- more sprite maps... -->
	 *     <sprite>...</srpite>
	 *   </sprites>
	 * </spritemapper>
	 * }</pre>
	 * @param config
	 * @param defOptions
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static List<Config> parse(String config, Map<String, String> defOptions) throws FileNotFoundException {
		List<Config> configs = new LinkedList<Config>();
		Map<String, String> options = new HashMap<String, String>();
		FileReader configFile = new FileReader(config);
		
		try {
			// create refined reader ignore all events except START_ELEMENT and END_ELEMENT.
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLStreamReader reader = inputFactory.createFilteredReader(inputFactory.createXMLStreamReader(configFile), new StreamFilter() {
				@Override
				public boolean accept(XMLStreamReader reader) {
					return reader.isStartElement() || reader.isEndElement();
				}
			});
			int eventType = 0;
			String tagName;
			
			// check root element.
			reader.require(XMLStreamReader.START_ELEMENT, null, ROOT_ELEMENT);
			
			// parse <options> or <sprites>
			while (reader.hasNext()) {
				eventType = reader.next();
				
				if (eventType != XMLStreamReader.START_ELEMENT) {
					if (eventType == XMLStreamReader.END_ELEMENT) {
						tagName = reader.getLocalName();
						
						// all tags are parsed. hooray!
						if (tagName.equals(ROOT_ELEMENT)) {
							break;
						}
					}
					
					throw new RuntimeException("Invalid config xml structure. Expect a start element or </spritemapper>. Location: " + reader.getLocation());
				}
				
				tagName = reader.getLocalName();
				
				if (tagName.equals(OPTIONS_ELEMENT)) {
					parseOptions(reader, options);
				} else if (tagName.equals(SPRITES_ELEMENT)) {
					parseSprites(reader, configs);
				} else {
					// ignore unknown elements for forward compatibility.
					skipElement(reader);
				}
			}
			
			// merge default options from argument and options in config file.
			// default options have higher priority than options.
			if (defOptions != null) {
				options.putAll(defOptions);
			}
			
			// apply options to all configs.
			// options set in each config have higher priority than default options.
			int optionsSize = options.size();
			
			for (Config c : configs) {
				Map<String, String> m = new HashMap<String, String>(optionsSize + c.options.size());
				m.putAll(options);
				m.putAll(c.options);
				c.options = m;
			}
		} catch (FactoryConfigurationError e) {
			throw new RuntimeException("Cannot find factory configuration. Message: " + e.getMessage());
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
		
		return configs;
	}
	
	private static void parseOptions(XMLStreamReader reader, Map<String, String> options) throws XMLStreamException {
		int eventType;
		String tagName;
		String value;
		String rootTag = OPTIONS_ELEMENT;
		
		// check root element.
		reader.require(XMLStreamReader.START_ELEMENT, null, rootTag);
		
		while (reader.hasNext()) {
			eventType = reader.next();
			tagName = reader.getLocalName();
			
			if (eventType == XMLStreamReader.START_ELEMENT) {				
				if (tagName.equals(OPTION_ELEMENT)) {
					value = reader.getAttributeValue(null, TYPE_ATTRIBUTE);
					
					if (value == null) {
						throw new RuntimeException("Missing 'type' attribute in <option>. Location: " + reader.getLocation());
					}
					
					options.put(value, reader.getElementText());
				} else {
					skipElement(reader);
				}
			}
			
			if (eventType == XMLStreamReader.END_ELEMENT) {
				if (!tagName.equals(rootTag)) {
					throw new RuntimeException("Invalid config xml structure. Expect </" + rootTag + ">. Location: " + reader.getLocation());
				}
				
				break;
			}
		}
	}
	
	private static void parseSprites(XMLStreamReader reader, List<Config> configs) throws XMLStreamException {
		int eventType;
		String tagName;
		String rootTag = SPRITES_ELEMENT;
		
		// check root element.
		reader.require(XMLStreamReader.START_ELEMENT, null, rootTag);
		
		while (reader.hasNext()) {
			eventType = reader.next();
			tagName = reader.getLocalName();
			
			if (eventType == XMLStreamReader.START_ELEMENT) {
				if (tagName.equals(SPRITE_ELEMENT)) {
					configs.add(parseSpriteConfig(reader));
				} else {
					skipElement(reader);
				}
			}
			
			if (eventType == XMLStreamReader.END_ELEMENT) {
				if (!tagName.equals(rootTag)) {
					throw new RuntimeException("Invalid config xml structure. Expect </" + rootTag + ">. Location: " + reader.getLocation());
				}
				
				break;
			}
		}
	}
	
	private static Config parseSpriteConfig(XMLStreamReader reader) throws XMLStreamException {
		Config config = new Config();
		
		int eventType;
		String tagName;
		String rootTag = SPRITE_ELEMENT;
		
		// check root element.
		reader.require(XMLStreamReader.START_ELEMENT, null, rootTag);
		
		// all <sprite> attributes are sprite-specific options.
		// they will overwrite default options in same keys.
		int attributes = reader.getAttributeCount();
		
		for (int i = 0; i < attributes; i++) {
			config.options.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
		}
		
		while (reader.hasNext()) {
			eventType = reader.next();
			tagName = reader.getLocalName();
			
			if (eventType == XMLStreamReader.START_ELEMENT) {
				if (tagName.equals(INPUT_ELEMENT)) {
					// parse <input>
					InputConfig c = new InputConfig();
					c.path = reader.getElementText();
					config.inputConfigList.add(c);
				} else if (tagName.equals(FILTER_ELEMENT)) {
					// parse <filter>
					String type = reader.getAttributeValue(null, TYPE_ATTRIBUTE);
					String value = reader.getElementText();
					
					if (type.equals("include")) {
						config.filters.add(new FileIncludeFilter(value));
					} else if (type.equals("exclude")) {
						config.filters.add(new FileExcludeFilter(value));
					} else {
						System.err.println("Unsupported 'type' in <filter>. Ignore this filter. Type: " + type);
					}
				} else if (tagName.equals(OUTPUT_ELEMENT)) {
					// parse <output>
					config.outputConfigList.add(parseOutputConfig(reader));
				} else {
					skipElement(reader);
				}
			}
			
			if (eventType == XMLStreamReader.END_ELEMENT) {
				if (!tagName.equals(rootTag)) {
					throw new RuntimeException("Invalid config xml structure. Expect </" + rootTag + ">. Location: " + reader.getLocation());
				}
				
				break;
			}
		}
		
		return config;
	}

	private static OutputConfig parseOutputConfig(XMLStreamReader reader) throws XMLStreamException {
		OutputConfig config = new OutputConfig();
		
		int eventType;
		String tagName;
		String rootTag = OUTPUT_ELEMENT;
		
		// check root element.
		reader.require(XMLStreamReader.START_ELEMENT, null, rootTag);
		
		while (reader.hasNext()) {
			eventType = reader.next();
			tagName = reader.getLocalName();
			
			if (eventType == XMLStreamReader.START_ELEMENT) {
				if (tagName.equals(TEXTURE_ELEMENT)) {
					TextureConfig c = new TextureConfig();
					c.type = reader.getAttributeValue(null, TYPE_ATTRIBUTE);
					c.path = reader.getElementText();
					
					config.textureConfigList.add(c);
				} else if (tagName.equals(META_ELEMENT)) {
					// parse <meta>
					MetaConfig c = new MetaConfig();
					String keepDir = reader.getAttributeValue(null, KEEP_DIR_ATTRIBUTE);
					
					if (keepDir.equals("true")) {
						c.keepDir = true;
					}
					
					// NOTE: reader.getElementText() will change reader state. must be call after all reader.getAttributeValue().
					c.type = reader.getAttributeValue(null, TYPE_ATTRIBUTE);
					c.path = reader.getElementText();
					
					config.metaConfigList.add(c);
				} else {
					skipElement(reader);
				}
			}

			if (eventType == XMLStreamReader.END_ELEMENT) {
				if (!tagName.equals(rootTag)) {
					throw new RuntimeException("Invalid config xml structure. Expect </" + rootTag + ">. Location: " + reader.getLocation());
				}
				
				break;
			}
		}
		
		return config;
	}
	
	private static void skipElement(XMLStreamReader reader) throws XMLStreamException {
		int level = 1;
		int eventType;
		
		while (reader.hasNext()) {
			eventType = reader.next();
			
			if (eventType == XMLStreamReader.START_ELEMENT) {
				level++;
			} else if (eventType == XMLStreamReader.END_ELEMENT) {
				level--;
			}
			
			if (level == 0) {
				break;
			}
		}
	}
}
