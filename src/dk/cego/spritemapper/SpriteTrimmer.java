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

import java.awt.image.Raster;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class SpriteTrimmer implements ObjectHandler<Sprite> {
    private boolean trim = false;

    public SpriteTrimmer setTrimTransparent(boolean trim) {
        this.trim = trim;
        return this;
    }

    public void handle(Sprite s) {
        Raster r = s.image.getAlphaRaster();

        if (r == null) {
            return;
        }

        if (this.trim) {
            int alpha[] = r.getPixels(0, 0, r.getWidth(), r.getHeight(), new int[r.getWidth() * r.getHeight()]);
            int w = r.getWidth();
            int h = r.getHeight();
            int x, y;
            //Find left
            outer:
            for (x = 0; x < w; x++) {
                for (y = 0; y < h; y++) {
                    if (alpha[y * w + x] > 0) {
                        break outer;
                    }
                }
            }
            s.colorRect.x = x;

            //Find right
            outer:
            for (x = w - 1; x >= 0; x--) {
                for (y = 0; y < h; y++) {
                    if (alpha[y * w + x] > 0) {
                        break outer;
                    }
                }
            }
            s.colorRect.w = Math.max(0, x - s.colorRect.x);

            //Find top
            outer:
            for (y = 0; y < h; y++) {
                for (x = 0; x < w; x++) {
                    if (alpha[y * w + x] > 0) {
                        break outer;
                    }
                }
            }
            s.colorRect.y = y;

            //Find bottom
            outer:
            for (y = h - 1; y >= 0; y--) {
                for (x = 0; x < w; x++) {
                    if (alpha[y * w + x] > 0) {
                        break outer;
                    }
                }
            }
            s.colorRect.h = Math.max(0, y - s.colorRect.y);
        }

        if (s.colorRect.w == s.w && s.colorRect.h == s.h) {
            return;
        }

        BufferedImage trimmed = new BufferedImage(s.colorRect.w, s.colorRect.h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = trimmed.createGraphics();
        g.drawImage(s.image, new AffineTransform(1f, 0f, 0f, 1f, 0f, 0f), null);
        g.dispose();

        s.image = trimmed;
        s.w = trimmed.getWidth();
        s.h = trimmed.getHeight();
    }
}
