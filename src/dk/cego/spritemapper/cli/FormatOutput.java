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

import java.awt.Dimension;
import dk.cego.spritemapper.SpriteMapper;
import dk.cego.spritemapper.SpriteMapperMetaStream;
import dk.cego.spritemapper.Zwoptex2MetaStream;
import dk.cego.spritemapper.util.OutputFilename;

public class FormatOutput {
    private SpriteMapperMetaStream stream = null;
    private OutputFilename output = null;

    public FormatOutput(String format, String filename) throws RuntimeException {
    	if (format.equals("zwoptex2")) {
    		stream = new Zwoptex2MetaStream();
    		output = OutputFilename.parseString(filename);
    		return;
    	}
    	
    	throw new RuntimeException("Unknown format '" + format + "'");
    }

    public void writeMetaData(SpriteMapper mapper, Dimension[] dimensions) throws Exception {
        mapper.doMetaStream(stream, dimensions, output);
    }
}

