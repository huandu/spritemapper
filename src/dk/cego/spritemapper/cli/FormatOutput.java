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
package dk.cego.spritemapper.cli;

import java.io.File;
import java.io.FileOutputStream;
import dk.cego.spritemapper.SpriteMapper;
import dk.cego.spritemapper.SpriteMapperMetaStream;
import dk.cego.spritemapper.Zwoptex2MetaStream;

public class FormatOutput {
    private String format;
    private File out;

    public FormatOutput(String format, File out) {
        this.format = format;
        this.out = out;
    }

    public void writeMetaData(SpriteMapper mapper) throws Exception {
        mapper.setMetaStreamer(getStreamer(mapper)).doMetaStream(new FileOutputStream(out));
    }

    private SpriteMapperMetaStream getStreamer(SpriteMapper mapper) throws Exception {
        if (format.equals("zwoptex2")) {

            return new Zwoptex2MetaStream(out.getName(), mapper.getLayoutDimension());
        }

        throw new Exception("Unknown format '" + format + "'");
    }

    public String toString() {
        return format + "(" + out + ")";
    }
}

