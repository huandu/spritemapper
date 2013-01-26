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
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

public class Zwoptex2MetaStream implements SpriteMapperMetaStream {
    private String filename;
    private Dimension size;

    public Zwoptex2MetaStream(String filename, Dimension size) {
        this.filename = filename;
        this.size = size;
    }

    public void write(List<Sprite> sprites, OutputStream out) throws IOException {
        // always use UTF-8. it's the only encoding supported by XCode 4.4
        Charset c = Charset.forName("UTF-8");
        OutputStreamWriter o = new OutputStreamWriter(out, c);
        o.write("<?xml version=\"1.0\" encoding=\"" + c + "\"?>\n");
        o.write("<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n");
        o.write("<plist version=\"1.0\">\n");
        o.write("    <dict>\n");
        o.write("        <key>frames</key>\n");
        o.write("        <dict>\n");

        for (Sprite s : sprites) {
            Dimension d = new Dimension(s.w, s.h);
            if (s.rotated) {
                d.width = s.h;
                d.height = s.w;
            }

            o.write("            <key>" + s.name.replace('\\', '/') + "</key>\n");
            o.write("            <dict>\n");
            o.write("                <key>frame</key>\n");
            o.write("                <string>{{" + s.x + "," + s.y + "},{" + d.width + "," + d.height + "}}</string>\n");
            o.write("                <key>rotated</key>\n");
            o.write("                <" + s.rotated + "/>\n");
            o.write("                <key>sourceColorRect</key>\n");
            o.write("                <string>{{" + s.colorRect.x + "," + s.colorRect.y + "},{" + s.colorRect.w + "," + s.colorRect.h + "}}</string>\n");
            o.write("                <key>sourceSize</key>\n");
            o.write("                <string>{" + s.originalDimension.width + "," + s.originalDimension.height + "}</string>\n");
            o.write("            </dict>\n");
        }

        o.write("        </dict>\n");
        o.write("        <key>metadata</key>\n");
        o.write("        <dict>\n");
        o.write("            <key>format</key>\n");
        o.write("            <integer>2</integer>\n");
        o.write("            <key>size</key>\n");
        o.write("            <string>{" + size.width + "," + size.height + "}</string>\n");
        o.write("            <key>textureFileName</key>\n");
        o.write("            <string>" + filename + "</string>\n");
        o.write("        </dict>\n");
        o.write("    </dict>\n");
        o.write("</plist>\n");
        o.flush();
    }
}

