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
package dk.cego.spritemapper.shelf;

import dk.cego.spritemapper.SpriteLayouter;
import dk.cego.spritemapper.Sprite;
import java.util.List;

public class ShelfLayouter implements SpriteLayouter {
    public void layout(int maxWidth, List<Sprite> sprites) {
        int x = 0, y = 0, nextY = 0;
        for (Sprite s : sprites) {
            if (x + s.w > maxWidth) {
                x = 0;
                y = nextY;
            }
            s.x = x;
            s.y = y;
            x = s.right();
            nextY = Math.max(nextY, s.bottom());
        }
    }

    public String toString() {
        return "Shelf()";
    }
}
