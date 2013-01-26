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
package dk.cego.spritemapper.cli;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

public class Scanner {
    private File directory;

    public Scanner(String path) {
        this(new File(path));
    }

    public Scanner(File directory) {
        this.directory = directory;
    }

    public Set<File> scan(FileMatcher matcher) {
        return scan(new TreeSet<File>(), directory, matcher);
    }

    public Set<File> scan(Set<File> destination, File directory, FileMatcher matcher) {
        for (File f : directory.listFiles()) {
            if (matcher.matches(f)) {
                destination.add(f);
            }

            if (f.isDirectory()) {
                scan(destination, f, matcher);
            }
        }

        return destination;
    }
}
