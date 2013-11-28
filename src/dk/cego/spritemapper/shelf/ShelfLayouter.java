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

public class ShelfLayouter extends SpriteLayouter {
	private final static int MAX_HEIGHT = 1024 * 1024 * 1024;
	
    public int layout(int maxWidth, int maxHeight, List<Sprite> sprites) {
    	if (sprites.isEmpty()) {
    		return 0;
    	}
    	
        if (maxHeight == 0) {
            maxHeight = MAX_HEIGHT;
        }
        
        int x = 0, y = 0, nextY = 0;
        int spacing = getSpacing();
        
        int mapNumber = 0;
        int layoutedCount = 0;

        for (Sprite s : sprites) {
            if (x + s.w > maxWidth) {
                // try to rotate sprite.
                if (x + s.h <= maxWidth) {
                    s.rotate();
                } else {
                    x = 0;
                    y = nextY;

                    if (s.w > maxWidth && s.h <= maxWidth) {
                        s.rotate();
                    }
                }
            }

            s.x = x;
            s.y = y;
            s.mapNumber = mapNumber;
            layoutedCount++;
            
            x = s.right() + spacing;
            nextY = Math.max(nextY, s.bottom() + spacing);
            
            if (nextY > maxHeight) {
            	if (layoutedCount == 0) {
            		throw new RuntimeException("No free space found.");
            	}
            	
            	layoutedCount = 0;
            	mapNumber++;
            	x = 0;
            	y = 0;
            	nextY = 0;
            }
        }
        
        return mapNumber;
    }

    public String toString() {
        return "Shelf()";
    }
}
