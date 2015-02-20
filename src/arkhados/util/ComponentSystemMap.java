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

import arkhados.components.Component;
import arkhados.systems.SSystem;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */

public class ComponentSystemMap {
    public static int idCounter = 0;
    
    private static ComponentSystemMap instance = new ComponentSystemMap();
    
    private Map<Class<? extends Component>, SSystem> mapping = new HashMap<>();
    
    public static ComponentSystemMap get() {
        return instance;
    }
    
    public void put(Class _class, SSystem system) {
        mapping.put(_class, system);
    }
    
    public SSystem get(Class _class) {
        return mapping.get(_class);
    }
    
    public void addComponent(int entityId, Component component) {
        SSystem system = mapping.get(component.getClass());
        system.addComponent(entityId, component);
    }
    
    public void addComponents(int entityId, Collection<Component> components) {
        for (Component component : components) {
            addComponent(entityId, component);
        }
    }
}
