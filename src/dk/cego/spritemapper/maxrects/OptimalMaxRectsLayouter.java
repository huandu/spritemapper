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
package dk.cego.spritemapper.maxrects;

import dk.cego.spritemapper.util.FreeSpaceChooser;
import dk.cego.spritemapper.util.BestFitChooser;
import dk.cego.spritemapper.util.BestShortSideChooser;
import dk.cego.spritemapper.util.BestLongSideChooser;
import dk.cego.spritemapper.util.TopLeftChooser;
import dk.cego.spritemapper.OptimalLayouter;
import dk.cego.spritemapper.Sprite;

public class OptimalMaxRectsLayouter extends OptimalLayouter {
    public OptimalMaxRectsLayouter() {
        FreeSpaceChooser freeSpaceChoosers[] = new FreeSpaceChooser[] {
            new BestFitChooser(),
            new BestShortSideChooser(),
            new BestLongSideChooser(),
            new TopLeftChooser()
        };
        for (FreeSpaceChooser chooser : freeSpaceChoosers) {
            add(new MaxRectsLayouter().setFreeSpaceChooser(chooser));
        }
    }
}
