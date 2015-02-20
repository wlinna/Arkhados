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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author william
 */

public class ComponentArranger<T> {
    private Map<Integer, Integer> idToIndex = new HashMap<>();
    private ArrayList<Integer> indexToId = new ArrayList<>();
    private final List<T> components;

    public ComponentArranger(List<T> components) {
        this.components = components;        
    }
    
    public void add(int entityId, T component) {
        int index = components.size();
        components.add(component);
        
        idToIndex.put(entityId, index);
        indexToId.add(entityId);
    }
    
    public void remove(int id) {
        int index = idToIndex.remove(id);
        T last = components.get(components.size() - 1);
        components.set(index, last);
        components.remove(components.size() - 1);
        
        int idOfMoved = indexToId.remove(indexToId.size() - 1);
        idToIndex.put(idOfMoved, index);
    }
}
