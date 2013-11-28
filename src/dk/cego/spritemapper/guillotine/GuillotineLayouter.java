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
import dk.cego.spritemapper.SpriteLayouter;
import dk.cego.spritemapper.util.FreeSpaceComparator;
import dk.cego.spritemapper.util.FreeSpaceChooser;
import dk.cego.spritemapper.util.SpriteCollections;
import dk.cego.spritemapper.util.SpriteFitFilter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class GuillotineLayouter extends SpriteLayouter {
	private final static int MAX_HEIGHT = 1024 * 1024 * 1024;
	
    private FreeSpaceChooser freeSpaceChooser = null;
    private FreeSpaceSplitStrategy freeSpaceSplitter = null;

    private String nullOrClass(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    public String toString() {
        return "Guillotine(" + nullOrClass(freeSpaceChooser) + "," + nullOrClass(freeSpaceSplitter) + ")";
    }

    public GuillotineLayouter setFreeSpaceChooser(FreeSpaceChooser chooser) {
        this.freeSpaceChooser = chooser;
        return this;
    }

    public GuillotineLayouter setFreeSpaceSplitStrategy(FreeSpaceSplitStrategy splitStrategy) {
        this.freeSpaceSplitter = splitStrategy;
        return this;
    }

    public int layout(int maxWidth, int maxHeight, List<Sprite> sprites) {
    	if (sprites.isEmpty()) {
    		return 0;
    	}
    	
        if (maxHeight == 0) {
            maxHeight = MAX_HEIGHT;
        }
        
        int spacing = getSpacing();
        List<Rectangle> freeSpaces = new ArrayList<Rectangle>();
        freeSpaces.add(new Rectangle(0, 0, maxWidth, maxHeight));
        FreeSpaceComparator comparator = new FreeSpaceComparator(freeSpaceChooser);

        Rectangle newFree[] = new Rectangle[2];
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
        		//Find all spaces that the current sprite fits into
                List<Rectangle> fits = SpriteCollections.filter(freeSpaces, new SpriteFitFilter(s));
                if (fits.size() == 0) {
                    remaining.add(s);
                    continue;
                }

                //Order the list so that the preferred space is in the first location
                comparator.setCurrentSprite(s);
                Collections.sort(fits, comparator);

                //Remove it from our lists
                Rectangle chosenSpace = fits.get(0);
                freeSpaces.remove(chosenSpace);

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

                //Choose how to split the free space
                newFree[0] = null;
                newFree[1] = null;
                FreeSpaceSplitStrategy.Split split = freeSpaceSplitter == null ? 
                        FreeSpaceSplitStrategy.Split.HORIZONTALLY : 
                        freeSpaceSplitter.chooseSplit(chosenSpace, s, spacing);
                if (split == FreeSpaceSplitStrategy.Split.HORIZONTALLY) {
                    splitHorizontally(newFree, chosenSpace, s, spacing);
                } else {
                    splitVertically(newFree, chosenSpace, s, spacing);
                }

                //Add the ones that have an area larger than 0
                for (Rectangle r : newFree) {
                    if (r.area() > 0) {
                        freeSpaces.add(r);
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

    public static void splitVertically(Rectangle rects[], Rectangle r, Sprite s, int spacing) {
        if (rects[0] == null) {
            rects[0] = new Rectangle(0,0,0,0);
        }
        rects[0].x = r.x + s.w + spacing; rects[0].y = r.y; rects[0].w = r.w - s.w - spacing; rects[0].h = r.h;

        if (rects[1] == null) {
            rects[1] = new Rectangle(0,0,0,0);
        }
        rects[1].x = r.x; rects[1].y = r.y + s.h + spacing; rects[1].w = s.w + spacing; rects[1].h = r.h - s.h - spacing;
    }

    public static void splitHorizontally(Rectangle rects[], Rectangle r, Sprite s, int spacing) {
        if (rects[0] == null) {
            rects[0] = new Rectangle(0,0,0,0);
        }
        rects[0].x = r.x + s.w + spacing; rects[0].y = r.y; rects[0].w = r.w - s.w - spacing; rects[0].h = s.h + spacing;

        if (rects[1] == null) {
            rects[1] = new Rectangle(0,0,0,0);
        }
        rects[1].x = r.x; rects[1].y = r.y + s.h + spacing; rects[1].w = r.w; rects[1].h = r.h - s.h - spacing;
    }
}

