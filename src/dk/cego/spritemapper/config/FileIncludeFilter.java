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

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class FileIncludeFilter implements FileFilter {
	private Pattern pattern;
	
	public FileIncludeFilter(String glob) {
		pattern = GlobPatternCompiler.compile(glob);
	}
	
	@Override
	public void filter(File baseDir, Set<File> files) {
		Set<File> includedFiles = new TreeSet<File>();
		
		for (File f : files) {
			if (test(baseDir, f)) {
				includedFiles.add(f);
			}
		}
		
		files.clear();
		files.addAll(includedFiles);
	}

	@Override
	public boolean test(File baseDir, File file) {
		if (pattern.matcher(file.getName()).find()) {
			return true;
		}
		
		// check relative path.
		String relativePath = baseDir.toURI().relativize(file.toURI()).getPath();

		if (pattern.matcher(relativePath).find()) {
			return true;
		}
		
		return false;
	}
}
