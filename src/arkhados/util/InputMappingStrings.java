/*    This file is part of Arkhados.

 Arkhados is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Arkhados is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Arkhados.  If not, see <http://www.gnu.org/licenses/>. */
package arkhados.util;

import java.util.HashMap;

/**
 *
 * @author william
 */
public class InputMappingStrings {

    public final static String MOVE_RIGHT = "move-right";
    public final static String MOVE_LEFT = "move-left";
    public final static String MOVE_UP = "move-up";
    public final static String MOVE_DOWN = "move-down";
    public final static String M1 = "m1";
    public final static String M2 = "m2";
    public final static String Q = "q";
    public final static String E = "e";
    public final static String R = "r";
    public final static String SPACE = "space";
    public final static String SEC1 = "sec1";
    public final static String SEC2 = "sec2";
    public final static String MODIFIER = "modifier";
    private final static HashMap<String, Integer> StringNumberMap =
            new HashMap<>(10);

    static {
        StringNumberMap.put(MOVE_RIGHT, 0);
        StringNumberMap.put(MOVE_LEFT, 1);
        StringNumberMap.put(MOVE_UP, 2);
        StringNumberMap.put(MOVE_DOWN, 3);
        StringNumberMap.put(M1, 4);
        StringNumberMap.put(M2, 5);
        StringNumberMap.put(Q, 6);
        StringNumberMap.put(E, 7);
        StringNumberMap.put(R, 8);
        StringNumberMap.put(SPACE, 9);
        StringNumberMap.put(SEC1, 10);
        StringNumberMap.put(SEC2, 11);
    }
    public final static String VOLUME_DOWN = "volume-down";
    public final static String VOLUME_UP = "volume-up";
    public final static String TOGGLE_STATS = "toggle-stats";

    public static Integer getId(String name) {
        return StringNumberMap.get(name);
    }
}
