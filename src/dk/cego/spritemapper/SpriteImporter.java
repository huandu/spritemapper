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

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.LinkedList;

public class SpriteImporter {
    private ObjectHandler<File> fileHandler;
    private ObjectHandler<Sprite> spriteHandler;

    public SpriteImporter() {
        fileHandler = new ObjectHandler<File>() {
            public void handle(File f) {}
        };

        spriteHandler = new ObjectHandler<Sprite>() {
            public void handle(Sprite f) {}
        };
    }

    public SpriteImporter setFileHandler(ObjectHandler<File> handler) {
        this.fileHandler = handler;
        return this;
    }

    public SpriteImporter setSpriteHandler(ObjectHandler<Sprite> handler) {
        this.spriteHandler = handler;
        return this;
    }

    public List<Sprite> importSprites(File base, Iterable<File> files) throws IOException {
        List<Sprite> result = new LinkedList<Sprite>();
        for (File f : files) {
            String path = f.getCanonicalPath().substring(base.getCanonicalPath().length() + 1);
            result.add(importSprite(base, path));
        }
        return result;
    }

    public Sprite importSprite(File baseDir, String path) throws IOException {
        File f = new File(baseDir, path);
        fileHandler.handle(f);
        Sprite s = new Sprite(path, ImageIO.read(f));
        spriteHandler.handle(s);
        return s;
    }
}
