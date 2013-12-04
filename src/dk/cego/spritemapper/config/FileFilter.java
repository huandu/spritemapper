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

public interface FileFilter {
	/**
	 * Filter files in the list. Filter will alter files list directly.
	 * @param baseDir
	 * @param files
	 */
	public abstract void filter(File baseDir, Set<File> files);
	
	/**
	 * Test whether a file can pass this filter.
	 * @param file
	 * @return
	 */
	public abstract boolean test(File baseDir, File file);
}
