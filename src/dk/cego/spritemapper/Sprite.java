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
import java.util.LinkedList;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class Sprite extends Rectangle {
    public String name;
    public BufferedImage image;
    public boolean rotated;
    public Rectangle colorRect;
    public Dimension originalDimension;

    public Sprite(String name, BufferedImage image) {
        this(name, image, 0, 0, image.getWidth(), image.getHeight(), false);
    }

    public Sprite(String name, BufferedImage image, int x, int y, int w, int h, boolean rotated) {
        this(name, image, x, y, w, h, 0, 0, w, h, w, h, rotated);
    }

    public Sprite(String name, BufferedImage image, int x, int y, int w, int h, int colorX, int colorY, int colorW, int colorH, int originalW, int originalH, boolean rotated) {
        super(x, y, w, h);
        this.name = name;
        this.image = image;
        this.rotated = rotated;
        this.colorRect = new Rectangle(colorX, colorY, colorW, colorH);
        this.originalDimension = new Dimension(originalW, originalH);
    }

    public Sprite rotate() {
        int tmp = w;
        w = h;
        h = tmp;

        rotated = !rotated;
        return this;
    }

    public final static Sprite copy(Sprite toCopy) {
        return new Sprite(toCopy.name, toCopy.image, 
                            toCopy.x, toCopy.y, toCopy.w, toCopy.h,
                            toCopy.colorRect.x, toCopy.colorRect.y, toCopy.colorRect.w, toCopy.colorRect.h, 
                            toCopy.originalDimension.width, toCopy.originalDimension.height,
                            toCopy.rotated);
    }

    public final static List<Sprite> copy(List<Sprite> toCopy) {
        LinkedList<Sprite> c = new LinkedList<Sprite>();
        for (Sprite s : toCopy) {
            c.add(copy(s));
        }
        return c;
    }

    public final static Dimension dimension(List<Sprite> sprites) {
        Dimension d = new Dimension(0, 0);
        for (Sprite s : sprites) {
            d.width = Math.max(d.width, s.right());
            d.height = Math.max(d.height, s.bottom());
        }

        return d;
    }

    public final static int collectiveArea(List<Sprite> sprites) {
        int area = 0;
        for (Sprite s : sprites) {
            area += s.area();
        }
        return area;
    }

    public String toString() {
        return "Sprite(" + name + "," + (rotated ? "rotated" : "not rotated") + "," + x + "," + y + "," + w + "," + h + ")";
    }
}
