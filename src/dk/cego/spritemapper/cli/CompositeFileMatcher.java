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
import java.util.List;
import java.util.LinkedList;

public class CompositeFileMatcher implements FileMatcher {
    private List<FileMatcher> matchers;

    public CompositeFileMatcher() {
        this.matchers = new LinkedList<FileMatcher>();
    }

    public CompositeFileMatcher add(FileMatcher matcher) {
        matchers.add(matcher);
        return this;
    }

    public boolean matches(File f) {
        for (FileMatcher m : matchers) {
            if (m.matches(f) == false) {
                return false;
            }
        }
        return true;
    }
}
