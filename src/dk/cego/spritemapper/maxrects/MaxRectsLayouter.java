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

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import dk.cego.spritemapper.util.FreeSpaceChooser;
import dk.cego.spritemapper.util.FreeSpaceComparator;
import dk.cego.spritemapper.util.SpriteCollections;
import dk.cego.spritemapper.util.SpriteFitFilter;
import dk.cego.spritemapper.util.RectangleCollisionFilter;
import dk.cego.spritemapper.Sprite;
import dk.cego.spritemapper.Rectangle;
import dk.cego.spritemapper.SpriteLayouter;

public class MaxRectsLayouter extends SpriteLayouter {
    private final static int MAX_HEIGHT = 1024 * 1024 * 1024;
    private FreeSpaceChooser freeSpaceChooser = null;

    private static String nullOrClass(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    public MaxRectsLayouter setFreeSpaceChooser(FreeSpaceChooser chooser) {
        this.freeSpaceChooser = chooser;
        return this;
    }

    public String toString() {
        return "MaxRects(" + nullOrClass(freeSpaceChooser) + ")";
    }

    public int layout(int maxWidth, int maxHeight, List<Sprite> sprites) {
    	if (sprites.isEmpty()) {
    		return 0;
    	}
    	
        if (maxHeight == 0) {
            maxHeight = MAX_HEIGHT;
        }

        List<Rectangle> freeSpaces = new ArrayList<Rectangle>();
        FreeSpaceComparator comparator = new FreeSpaceComparator(freeSpaceChooser);

        List<Sprite> current = new ArrayList<Sprite>(sprites);
        List<Sprite> remaining = new ArrayList<Sprite>(sprites.size());
        int layoutedCount;
        int mapNumber;

        for (mapNumber = 0; !current.isEmpty(); mapNumber++) {
        	layoutedCount = 0;
        	remaining.clear();
        	freeSpaces.clear();
        	freeSpaces.add(new Rectangle(0, 0, maxWidth, maxHeight));
        	
        	for (Sprite s : current) {
                List<Rectangle> fits = SpriteCollections.filter(freeSpaces, new SpriteFitFilter(s));
                
                // cannot find suitable space in current rectangle? add sprite to remaining for next round.
                if (fits.size() == 0) {
                	remaining.add(s);
                    continue;
                }

                //Order the list so that the preferred space is in the first location
                comparator.setCurrentSprite(s);
                Collections.sort(fits, comparator);
                Rectangle chosenSpace = fits.get(0);

                //If the sprite does not fit the chosen space with its current rotation...
                if (s.fits(chosenSpace) == false) {
                    //...then tilt it
                    s.rotate();
                }

                //Place the sprite into the chosen free space
                s.x = chosenSpace.x;
                s.y = chosenSpace.y;
                s.mapNumber = mapNumber;
                layoutedCount++;

                //Now find and remove all spaces that collide with the sprite
                List<Rectangle> collidingRects = SpriteCollections.remove(freeSpaces, new RectangleCollisionFilter(s));

                //Slice up the colliding rectangles and add the slices to the free list
                for (Rectangle r : collidingRects) {
                    List<Rectangle> sliced = slice(r, s);
                    SpriteCollections.append(freeSpaces, sliced);
                }

                //Now remove all spaces that are completely covered by another space
                for (int i = freeSpaces.size() - 1; i >= 0; i--) {
                    Rectangle cur = freeSpaces.get(i);
                    for (int j = freeSpaces.size() - 1; j >= 0; j--) {
                        Rectangle subject = freeSpaces.get(j);
                        if (subject.inside(cur) && subject != cur) {
                            freeSpaces.remove(subject);
                            if (j < i) i--;
                        }
                    }
                }
            }
        	
        	if (layoutedCount == 0) {
        		throw new RuntimeException("No free space found.");
        	}
        	
        	// swap remaining and current list.
        	List<Sprite> temp = remaining;
        	remaining = current;
        	current = temp;
        }
        
        return mapNumber;
    }

    private List<Rectangle> slice(Rectangle r, Sprite s) {
        List<Rectangle> result = new ArrayList<Rectangle>(4);

        Rectangle re;
        if ((re = sliceNorth(r, s)).area() > 0) result.add(re);
        if ((re = sliceSouth(r, s)).area() > 0) result.add(re);
        if ((re = sliceEast(r, s)).area() > 0) result.add(re);
        if ((re = sliceWest(r, s)).area() > 0) result.add(re);

        return result;
    }

    private Rectangle sliceNorth(Rectangle r, Sprite s) {
        return new Rectangle(r.x, r.y, r.w, s.y - r.y);
    }

    private Rectangle sliceSouth(Rectangle r, Sprite s) {
        return new Rectangle(r.x, s.bottom() + getSpacing(), r.w, r.bottom() - s.bottom() - getSpacing());
    }

    private Rectangle sliceWest(Rectangle r, Sprite s) {
        return new Rectangle(r.x, r.y, s.x - r.x, r.h);
    }

    private Rectangle sliceEast(Rectangle r, Sprite s) {
        return new Rectangle(s.right() + getSpacing(), r.y, r.right() - s.right() - getSpacing(), r.h);
    }
}
