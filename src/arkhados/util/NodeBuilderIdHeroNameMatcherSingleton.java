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
public class NodeBuilderIdHeroNameMatcherSingleton {
    private static NodeBuilderIdHeroNameMatcherSingleton instance = null;
    
    private final HashMap<String, Integer> nameIdMap = new HashMap<>(3);

    private NodeBuilderIdHeroNameMatcherSingleton() {
    }
    
    
    
    public static NodeBuilderIdHeroNameMatcherSingleton get() {
        if (instance == null) {
            instance = new NodeBuilderIdHeroNameMatcherSingleton();
        }
        
        return instance;
    }
    
    public void addMapping(String name, int id) {
        this.nameIdMap.put(name, id);
    }
    
    public Integer getId(String name) {
        return this.nameIdMap.get(name);
    }
}
