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
package dk.cego.spritemapper.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OutputFilename is a wrapper of output filename, which may contain place holder string.
 * Supported place holder strings:
 * <li><code>{n}</code>: Number place holder starting with 0.</li>
 * <li><code>{n1}</code>: Number place holder starting with 1. 1 can be any other valid positive integer.</li>
 * <li><code>{n!}</code>: Number place holder starting with 0. The '!' means it will expend to a number even if there is only one file.</li>
 */
public class OutputFilename {
	private final static String PLACEHOLDER_PATTERN = "\\{([^\\}]+)\\}";
	private final static String NUMBER_PLACEHOLDER_PATTERN = "^n(\\d*)(!)?";
	
	private interface Fragment {
		public abstract void buildString(StringBuilder sb, int sequence, boolean yieldNumber);
	}

	private class NumberFragment implements Fragment {
		private String prefix;
		private int base;
		private boolean dontYieldNumber;
		
		public NumberFragment(String prefix, int base, boolean dontYieldNumber) {
			this.prefix = prefix;
			this.base = base;
			this.dontYieldNumber = dontYieldNumber;
		}

		@Override
		public void buildString(StringBuilder sb, int sequence, boolean yieldNumber) {
			sb.append(prefix);
			
			if (dontYieldNumber || !yieldNumber || sequence != 0) {
				sb.append(sequence + base);
			}
		}
	}
	
	private class PlainFragment implements Fragment {
		private String prefix;
		
		public PlainFragment(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public void buildString(StringBuilder sb, int sequence, boolean yieldNumber) {
			sb.append(prefix);
		}
	}
	
	private List<Fragment> fragments;
	private int maxNumber = 0;
	private int currentNumber = 0;
	
	private OutputFilename(List<Fragment> fragments) {
		this.fragments = fragments;
	}
	
	/**
	 * Parse filename place holder. Only 1 place holder will be parsed.
	 * @param filename
	 * @return
	 */
	public static OutputFilename parseString(String filename) {
		Pattern placeholderPattern = Pattern.compile(PLACEHOLDER_PATTERN);
		Pattern numberPattern = Pattern.compile(NUMBER_PLACEHOLDER_PATTERN);
		Matcher matches = placeholderPattern.matcher(filename);
		int lastIndex = 0;
		String placeholder;
		List<Fragment> fragments = new ArrayList<Fragment>();
		OutputFilename output = new OutputFilename(fragments);
		
		while (matches.find()) {
			placeholder = matches.group(1);
			
			Matcher m = numberPattern.matcher(placeholder);
			
			// unknown placeholder pattern.
			if (!m.find()) {
				continue;
			}
			
			// create new number fragment based on match result.
			String prefix = filename.substring(lastIndex, matches.start());
			String baseString = m.group(1);
			int base = 0;
			boolean yieldNumber = m.group(2) != null;
			
			if (!baseString.isEmpty()) {
				base = Integer.parseInt(baseString);
			}
			
			fragments.add(output.new NumberFragment(prefix, base, yieldNumber));
			
			lastIndex = matches.end();
		}
		
		// add remaining string to fragments list.
		if (lastIndex != filename.length()) {
			fragments.add(output.new PlainFragment(filename.substring(lastIndex)));
		}
		
		return output;
	}
	
	/**
	 * Set max number.
	 * @param maxNumber
	 */
	public void setMaxNumber(int maxNumber) {
		this.maxNumber = maxNumber;
	}
	
	/**
	 * Get current number.
	 * @return
	 */
	public int getCurrentNumber() {
		return currentNumber;
	}
	
	/**
	 * Reset current number to 0.
	 */
	public void resetCurrentNumber() {
		currentNumber = 0;
	}
	
	/**
	 * Return next filename.
	 * Each call to filename() will increase current number once.
	 * If current number exceeds max number, it throws RuntimeException.
	 * @return
	 */
	public String filename() throws RuntimeException {
		if (currentNumber >= maxNumber) {
			throw new RuntimeException("Call filename too many times. Increase max number before call it.");
		}
		
		StringBuilder sb = new StringBuilder();
		boolean yieldNumber = maxNumber == 1; // yield number only if there is only 1 file to generate.
		
		for (Fragment f : fragments) {
			f.buildString(sb, currentNumber, yieldNumber);
		}
		
		currentNumber++;
		return sb.toString();
	}
}
