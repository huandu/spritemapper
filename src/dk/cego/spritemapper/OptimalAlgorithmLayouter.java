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
package dk.cego.spritemapper;

import dk.cego.spritemapper.guillotine.OptimalGuillotineLayouter;
import dk.cego.spritemapper.shelf.ShelfLayouter;
import dk.cego.spritemapper.maxrects.OptimalMaxRectsLayouter;

/**
 * This layouter knows all the different algorithms and chooses
 * the best one for the specific set of sprites.
 */
public class OptimalAlgorithmLayouter extends OptimalLayouter {
    public OptimalAlgorithmLayouter() {
        add(new OptimalGuillotineLayouter());
        add(new ShelfLayouter());
        add(new OptimalMaxRectsLayouter());
    }
}
