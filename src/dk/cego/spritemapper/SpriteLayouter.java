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

import java.util.List;

public abstract class SpriteLayouter {
	private int spacing = 0;
    private int border = 0;

    public abstract void layout(int maxWidth, List<Sprite> sprites);

    public SpriteLayouter setSpacing(int spacing) {
    	if (spacing < 0) {
    		spacing = 0;
    	}

    	this.spacing = spacing;
    	return this;
    }

    public int getSpacing() {
    	return this.spacing;
    }

    public SpriteLayouter setBorder(int border) {
        if (border < 0) {
            border = 0;
        }

        this.border = border;
        return this;
    }

    public int getBorder() {
        return this.border;
    }
}
