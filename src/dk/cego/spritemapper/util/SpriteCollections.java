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
package dk.cego.spritemapper.util;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class SpriteCollections {
    public static <T> List<T> filter(List<T> list, Filter<T> filter) {
        ArrayList<T> result = new ArrayList<T>();
        for (T e : list) {
            if (filter.filter(e)) {
                result.add(e);
            }
        }
        return result;
    }

    public static <T> List<T> remove(List<T> list, Filter<T> filter) {
        ArrayList<T> result = new ArrayList<T>();
        Iterator<T> ite = list.iterator();
        while (ite.hasNext()) {
            T e = ite.next();
            if (filter.filter(e)) {
                result.add(e);
                ite.remove();
            }
        }
        return result;
    }

    public static <T> void append(List<T> destination, List<T> list) {
        for (T e : list) {
            destination.add(e);
        }
    }
}
