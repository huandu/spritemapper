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

import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * This layouter contains a list of layouters and
 * finds the most optimal by trial and error. The most
 * optimal is the one that arranges the sprites to the
 * smalles area.
 */
public class OptimalLayouter extends SpriteLayouter implements Iterable<SpriteLayouter> {
    private LinkedList<SpriteLayouter> layouters;
    private SpriteLayouter lastUsed;
    private boolean usePOTSize = false;

    public OptimalLayouter() {
        this.layouters = new LinkedList<SpriteLayouter>();
        this.lastUsed = null;
    }

    public Iterator<SpriteLayouter> iterator() {
        return layouters.iterator();
    }

    public OptimalLayouter add(SpriteLayouter layouter) {
        this.layouters.add(layouter);
        return this;
    }

    public SpriteLayouter getLastUsed() {
        return lastUsed;
    }

    public OptimalLayouter setUsePOTSize(boolean usePOTSize) {
        this.usePOTSize = usePOTSize;
        return this;
    }

    public boolean getUsePOTSize() {
        return usePOTSize;
    }

    /**
     * @throws NullPointerException if no layouters have been added to this instance.
     */
    public int layout(int maxWidth, int maxHeight, List<Sprite> sprites) {
        if (usePOTSize) {
            maxWidth = toLowerPOT(maxWidth);
        }

        return findOptimal(maxWidth, maxHeight, sprites);
    }

    public String toString() {
        return lastUsed == null ? "null" : lastUsed.toString();
    }

    public static int toUpperPOT(int size) {
        size -= 1;
        size |= size >>> 1;
        size |= size >>> 2;
        size |= size >>> 4;
        size |= size >>> 8;
        size |= size >>> 16;
        return size + 1;
    }

    public static int toLowerPOT(int size) {
        int pot = toUpperPOT(size);

        if (pot > size) {
            pot = pot >>> 1;
        }

        return pot;
    }

    /**
     * Find and return the layouter which packs the listed sprites to the
     * smallest area. If no layouters have been added null is returned.
     * @return return the layouter which packs the listed sprites to the
     * smallest area or null if no layouters have been added yet.
     */
    private int findOptimal(int maxWidth, int maxHeight, List<Sprite> sprites) {
        SpriteLayouter optimal = null;

        if (layouters.isEmpty()) {
            return 0;
        }

        int border = getBorder();
        maxWidth -= border * 2;

        // impossible to do layout.
        if (maxWidth <= 0) {
            return 0;
        }

        // we only use one layouter by default.
        if (layouters.size() == 1) {
            try {
                optimal = layouters.get(0);
                lastUsed = optimal;
                
                return optimal.setSpacing(getSpacing())
                .layout(maxWidth, maxHeight, sprites);
            } catch (RuntimeException re) {
                // nothing
            }
            
            return 0;
        }

        List<Sprite> optimizedSprites = null;
        int minArea = 0;
        float minFactor = 0f;
        int mapNumber = 0;

        for (SpriteLayouter l : layouters) {
            List<Sprite> copy = Sprite.copy(sprites);

            try {
                int currentMapNumber = l.setSpacing(getSpacing())
                .layout(maxWidth, maxHeight, copy);

                Dimension[] dimensions = Sprite.dimensions(copy, currentMapNumber);

                if (optimal == null) {
                    optimal = l;
                    optimizedSprites = copy;
                    minArea = area(dimensions);
                    minFactor = factor(dimensions, maxWidth, maxHeight);
                    mapNumber = currentMapNumber;

                    continue;
                }

                // smallest area and min width/height factor layouter wins.
                int curArea = area(dimensions);
                float curFactor = factor(dimensions, maxWidth, maxHeight);

                if (curArea < minArea || (curArea == minArea && curFactor < minFactor)) {
                    optimal = l;
                    minArea = curArea;
                    minFactor = curFactor;
                }
            } catch (RuntimeException re) {
            	System.err.println(re);
            }
        }

        sprites.clear();
        sprites.addAll(optimizedSprites);

        lastUsed = optimal;
        return mapNumber;
    }

    /**
     * Calculate total area of dimensions.
     * @param dimensions
     * @return
     */
    private int area(Dimension[] dimensions) {
        int border = getBorder();
        int areaValue = 0;
        
        for (Dimension d : dimensions) {
        	if (usePOTSize) {
                areaValue += toUpperPOT(d.width + 2 * border) * toUpperPOT(d.height + 2 * border);
            } else {
                areaValue += (d.width + 2 * border) * (d.height + 2 * border);
            }
        }

        return areaValue;
    }

    /**
     * Calculate optimization factor. The smaller the better.
     * @param dimensions
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    private float factor(Dimension[] dimensions, int maxWidth, int maxHeight) {
    	int length = dimensions.length;
    	float factorValue = 0.0f;
    	Dimension d = null;
    	
    	if (length == 0) {
    		return 0.0f;
    	}
    	
    	// best algorithm can fill all possible spaces in dimensions.
    	// NOTE: last dimension is not considered in factor.
    	for (int i = 0; i < length - 1; i++) {
    		d = dimensions[i];
    		factorValue += 1.0f * (maxWidth - d.width) / maxWidth;
    		
    		if (maxHeight > 0) {
    			factorValue += 1.0f * (maxHeight - d.height) / maxHeight;
    		}
    		
    		// add penalty value for dimensions others than last one.
    		factorValue += 1.0f;
    	}
    	
    	// the less dimensions the better.
    	factorValue *= length;
    	
    	// last dimension should be as square as possible.
    	d = dimensions[length - 1];
    	
        if (d.width > d.height) {
            factorValue += 1f * d.width / d.height;
        } else {
            factorValue += 1f * d.height / d.width;
        }
        
        return factorValue;
    }
}

