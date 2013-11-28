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

    public List<Sprite> importSprites(File base, Iterable<File> files, Boolean reserveDirName) throws IOException {
        List<Sprite> result = new LinkedList<Sprite>();
        String baseDir = base.getCanonicalPath();
        String separator = "\\" + File.separator;
        String[] baseDirParts = null;

        for (File f : files) {
            String filePath = f.getCanonicalPath();
            String path;

            if (reserveDirName) {
                // file path likely starts with base dir.
                if (filePath.startsWith(baseDir)) {
                    path = filePath.substring(baseDir.length() + 1);
                } else {
                    if (baseDirParts == null) {
                        baseDirParts = baseDir.split(separator);
                    }

                    String[] parts = filePath.split(separator);
                    int start = 0;

                    for (; start < parts.length && start < baseDirParts.length; start++) {
                        if (parts[start] != baseDirParts[start]) {
                            break;
                        }
                    }

                    // i don't know why java doesn't have built-in array join and slice.
                    if (start < parts.length) {
                        path = parts[start];

                        for (int i = start + 1; i < parts.length; i++) {
                            path += File.separator + parts[i];
                        }
                    } else {
                        path = "";
                    }
                }
            } else {
                path = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            }

            result.add(importSprite(f, path));
        }

        return result;
    }

    public Sprite importSprite(File file, String name) throws IOException {
        fileHandler.handle(file);
        Sprite s = new Sprite(name, ImageIO.read(file));
        spriteHandler.handle(s);
        return s;
    }
}
