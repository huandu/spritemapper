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

public class GuillotineLayouter implements SpriteLayouter {
    private int maxHeight;
    private FreeSpaceChooser freeSpaceChooser = null;
    private FreeSpaceSplitStrategy freeSpaceSplitter = null;

    public GuillotineLayouter() {
        this(100000);
    }

    public GuillotineLayouter(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    private String nullOrClass(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    public String toString() {
        return "Guillotine(" + nullOrClass(freeSpaceChooser) + "," + nullOrClass(freeSpaceSplitter) + ")";
    }

    public GuillotineLayouter setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public GuillotineLayouter setFreeSpaceChooser(FreeSpaceChooser chooser) {
        this.freeSpaceChooser = chooser;
        return this;
    }

    public GuillotineLayouter setFreeSpaceSplitStrategy(FreeSpaceSplitStrategy splitStrategy) {
        this.freeSpaceSplitter = splitStrategy;
        return this;
    }

    public void layout(int maxWidth, List<Sprite> sprites) {
        List<Rectangle> freeSpaces = new ArrayList<Rectangle>();
        freeSpaces.add(new Rectangle(0, 0, maxWidth, maxHeight));
        FreeSpaceComparator comparator = new FreeSpaceComparator(freeSpaceChooser);

        Rectangle newFree[] = new Rectangle[2];

        for (Sprite s : sprites) {
            //Find all spaces that the current sprite fits into
            List<Rectangle> fits = SpriteCollections.filter(freeSpaces, new SpriteFitFilter(s));
            if (fits.size() == 0) {
                throw new RuntimeException("No free space found.");
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

            //Choose how to split the free space
            newFree[0] = null;
            newFree[1] = null;
            FreeSpaceSplitStrategy.Split split = freeSpaceSplitter == null ? 
                    FreeSpaceSplitStrategy.Split.HORIZONTALLY : 
                    freeSpaceSplitter.chooseSplit(chosenSpace, s);
            if (split == FreeSpaceSplitStrategy.Split.HORIZONTALLY) {
                splitHorizontally(newFree, chosenSpace, s);
            } else {
                splitVertically(newFree, chosenSpace, s);
            }

            //Add the ones that have an area larger than 0
            for (Rectangle r : newFree) {
                if (r.area() > 0) {
                    freeSpaces.add(r);
                }
            }
        }
    }

    static void splitVertically(Rectangle rects[], Rectangle r, Sprite s) {
        if (rects[0] == null) {
            rects[0] = new Rectangle(0,0,0,0);
        }
        rects[0].x = r.x + s.w; rects[0].y = r.y; rects[0].w = r.w - s.w; rects[0].h = r.h;

        if (rects[1] == null) {
            rects[1] = new Rectangle(0,0,0,0);
        }
        rects[1].x = r.x; rects[1].y = r.y + s.h; rects[1].w = s.w; rects[1].h = r.h - s.h;
    }

    static void splitHorizontally(Rectangle rects[], Rectangle r, Sprite s) {
        if (rects[0] == null) {
            rects[0] = new Rectangle(0,0,0,0);
        }
        rects[0].x = r.x + s.w; rects[0].y = r.y; rects[0].w = r.w - s.w; rects[0].h = s.h;

        if (rects[1] == null) {
            rects[1] = new Rectangle(0,0,0,0);
        }
        rects[1].x = r.x; rects[1].y = r.y + s.h; rects[1].w = r.w; rects[1].h = r.h - s.h;
    }
}

