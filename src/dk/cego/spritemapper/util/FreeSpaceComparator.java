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
package dk.cego.spritemapper.util;

import dk.cego.spritemapper.Rectangle;
import dk.cego.spritemapper.Sprite;
import java.util.Comparator;

public class FreeSpaceComparator implements Comparator<Rectangle> {
    private FreeSpaceChooser chooser;
    private Sprite current;

    public FreeSpaceComparator(FreeSpaceChooser chooser) {
        this.chooser = chooser;
        current = null;
    }

    public FreeSpaceComparator setCurrentSprite(Sprite s) {
        this.current = s;
        return this;
    }

    public int compare(Rectangle r1, Rectangle r2) {
        return this.chooser == null ? 0 : this.chooser.choose(r1, r2, this.current);
    }
}

