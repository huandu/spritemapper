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

public class Rectangle {
    public int x, y, w, h;

    public Rectangle(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public int left() {
        return x;
    }

    public int right() {
        return x + w;
    }

    public int top() {
        return y;
    }

    public int bottom() {
        return y + h;
    }

    public int area() {
        return w * h;
    }

    /**
     * Return true if this fits inside 'other'.
     */
    public boolean fits(Rectangle other) {
        return w <= other.w && h <= other.h;
    }

    public boolean collides(Rectangle other) {
        return right() > other.left() && left() < other.right() &&
               bottom() > other.top() && top() < other.bottom();
    }

    public boolean contains(Rectangle other) {
        return left() <= other.left() && right() >= other.right() &&
               top() <= other.top() && bottom() >= other.bottom();
    }

    public boolean inside(Rectangle other) {
        return left() >= other.left() && right() <= other.right() &&
               top() >= other.top() && bottom() <= other.bottom();
    }

    public String toString() {
        return "Rectangle(" + x + "," + y + ", " + w + "," + h + ")";
    }
}
