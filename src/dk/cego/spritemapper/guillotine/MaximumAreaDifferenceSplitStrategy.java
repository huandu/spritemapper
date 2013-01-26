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
package dk.cego.spritemapper.guillotine;

import dk.cego.spritemapper.Rectangle;
import dk.cego.spritemapper.Sprite;

public class MaximumAreaDifferenceSplitStrategy implements FreeSpaceSplitStrategy {
    private Rectangle areas[];

    public MaximumAreaDifferenceSplitStrategy() {
        areas = new Rectangle[2];
    }

    public FreeSpaceSplitStrategy.Split chooseSplit(Rectangle r, Sprite s) {
        GuillotineLayouter.splitVertically(areas, r, s);
        int verticalSplitAreaDiff = Math.abs(areas[0].area() - areas[1].area());

        GuillotineLayouter.splitHorizontally(areas, r, s);
        int horizontalSplitAreaDiff = Math.abs(areas[0].area() - areas[1].area());

        return verticalSplitAreaDiff > horizontalSplitAreaDiff ? FreeSpaceSplitStrategy.Split.VERTICALLY : FreeSpaceSplitStrategy.Split.HORIZONTALLY;
    }
}

