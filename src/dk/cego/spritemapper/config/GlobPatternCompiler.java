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

import java.util.regex.Pattern;

/**
 * GlobPatternCompiler can parse any glob pattern to regular expression Pattern.
 * It supports glob pattern like <code>*.ext</code>, <code>foo???</code> and <code>foo{1,2}.bar</code>.
 */
public class GlobPatternCompiler {
	/**
	 * Parse glob pattern to a regular expression Pattern.
	 * @param glob
	 * @return
	 */
	public static Pattern compile(String glob) {
		char[] chars = glob.toCharArray();
		int startIndex = 0;
		int endIndex = chars.length;
		boolean leadingStar = false;
		boolean tailingStar = false;
		
		// skip leading white spaces.
		for (; startIndex < endIndex; startIndex++) {
			if (!Character.isWhitespace(chars[startIndex])) {
				break;
			}
		}
		
		// skip leading '*' as they are useless.
		for (; startIndex < endIndex; startIndex++) {
			if (chars[startIndex] != '*') {
				break;
			}
			
			leadingStar = true;
		}
		
		// skip tailing white spaces.
		for (; startIndex < endIndex; endIndex--) {
			if (!Character.isWhitespace(chars[endIndex - 1])) {
				break;
			}
		}
		
		// skip tailing '*' as they are useless.
		for (; startIndex < endIndex; endIndex--) {
			if (chars[endIndex - 1] != '*') {
				break;
			}
			
			tailingStar = true;
		}
	    
		StringBuilder sb = new StringBuilder((endIndex - startIndex) * 2);
	    boolean escaping = false;
	    int curlies = 0;
	    char c;
	    
	    if (!leadingStar) {
	    	sb.append('^');
	    }
	    
	    for (int i = startIndex; i < endIndex; i++) {
	    	c = chars[i];
	    	
	    	switch (c) {
	    	case '*':
	    		if (escaping) {
	    			sb.append('\\');
	    			escaping = false;
	    		} else {
	    			sb.append('.');
	    		}
	    		
	    		sb.append(c);
	    		break;
	    		
	    	case '?':
	    		if (escaping) {
	    			sb.append('\\');
	    			sb.append(c);
	    			escaping = false;
	    		} else {
	    			sb.append('.');
	    		}
	    		
	    		break;
	    		
	    	case '.':
	        case '(':
	        case ')':
	        case '+':
	        case '|':
	        case '^':
	        case '$':
	        case '@':
	        case '%':
	        case '/':
	        	sb.append('\\');
	        	sb.append(c);
	        	escaping = false;
	        	break;
	        	
	        case '\\':
	        	if (escaping) {
	        		sb.append('\\');
	        		sb.append(c);
	        		escaping = false;
	        	} else {
	        		escaping = true;
	        	}
	        	
	        	break;
	        	
	        case '{':
	        	if (escaping) {
	        		sb.append('\\');
	        		sb.append(c);
	        		escaping = false;
	        	} else {
	        		sb.append('(');
	        		curlies++;
	        	}
	        	
	        	break;
	        	
	        case '}':
	        	if (curlies > 0 && !escaping) {
	                sb.append(')');
	                curlies--;
	            } else if (escaping) {
	                sb.append('\\');
	                sb.append(c);
	                escaping = false;
	            } else {
	                sb.append(c);
	            }
	        	
	            break;
	            
	        case ',':
	        	if (curlies > 0 && !escaping) {
	                sb.append('|');
	            } else if (escaping) {
	                sb.append('\\');
	                sb.append(c);
	                escaping = false;
	            } else {
	                sb.append(c);
	            }
	        	
	            break;
	        
	        default:
	        	sb.append(c);
	        	escaping = false;
	    	}
	    }
	    
	    if (!tailingStar) {
	    	sb.append('$');
	    }
	    
	    return Pattern.compile(sb.toString());
	}
}
