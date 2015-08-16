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
import java.util.Map;

public class NodeBuilderIdHeroNameMatcher {

    private static NodeBuilderIdHeroNameMatcher instance = null;
    private final Map<String, Integer> nameIdMap = new HashMap<>(3);

    private NodeBuilderIdHeroNameMatcher() {
    }

    public static NodeBuilderIdHeroNameMatcher get() {
        if (instance == null) {
            instance = new NodeBuilderIdHeroNameMatcher();
        }

        return instance;
    }

    public void addMapping(String name, int id) {
        nameIdMap.put(name, id);
    }

    public Integer getId(String name) {
        return nameIdMap.get(name);
    }
}
