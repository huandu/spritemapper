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
public class OptimalLayouter implements SpriteLayouter, Iterable<SpriteLayouter> {
    private LinkedList<SpriteLayouter> layouters;
    private SpriteLayouter lastUsed;

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

    /**
     * Find and return the layouter which packs the listed sprites to the
     * smallest area. If no layouters have been added null is returned.
     * @return return the layouter which packs the listed sprites to the
     * smallest area or null if no layouters have been added yet.
     */
    public SpriteLayouter findOptimal(int maxWidth, List<Sprite> sprites) {
        SpriteLayouter optimal = null;
        int optimalHeight = Integer.MAX_VALUE;
        int fitHeight = Integer.MAX_VALUE;
        boolean hasFitLayouter = false;

        for (SpriteLayouter l : layouters) {
            List<Sprite> copy = Sprite.copy(sprites);
            try {
                l.layout(maxWidth, copy);
                Dimension d = Sprite.dimension(copy);

                if (optimal == null) {
                    optimal = l;
                }

                // the layouter fits max width and has minimum height wins.
                if (d.width == maxWidth) {
                    if (fitHeight > d.height) {
                        fitHeight = d.height;
                        optimal = l;
                        hasFitLayouter = true;

                        continue;
                    }
                }

                if (hasFitLayouter) {
                    continue;
                }

                if (optimalHeight > d.height) {
                    optimalHeight = d.height;
                    optimal = l;
                }
            } catch (RuntimeException re) {
            }
        }

        return optimal;
    }

    private int area(Dimension d) {
        return d.width * d.height;
    }

    /**
     * @throws NullPointerException if no layouters have been added to this instance.
     */
    public void layout(int maxWidth, List<Sprite> sprites) {
        lastUsed = findOptimal(maxWidth, sprites);
        lastUsed.layout(maxWidth, sprites);
    }

    public String toString() {
        return lastUsed == null ? "null" : lastUsed.toString();
    }
}

