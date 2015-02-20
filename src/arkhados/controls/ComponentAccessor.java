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
package arkhados.controls;

import arkhados.components.CRest;
import arkhados.components.Component;
import arkhados.systems.SSystem;
import arkhados.util.ComponentSystemMap;
import arkhados.util.UserDataStrings;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
/**
 * Control used for accessing entity's components. Used as migration step
 * between Entity-Controller and Entity-Component-System. This will be removed
 * when migration is nearing completion
 */
public class ComponentAccessor extends AbstractControl {

    public CRest resting;
    private List<Component> components = new ArrayList<>();

    @Override
    public void setSpatial(Spatial spatial) {
        if (spatial == null) {
            int entityId = this.spatial.getUserData(UserDataStrings.ENTITY_ID);
            super.setSpatial(spatial);
            for (Component component : components) {
                SSystem system =
                        ComponentSystemMap.get().get(component.getClass());
                system.removeEntity(entityId);
            }
        } else {
            super.setSpatial(spatial);
        }
    }

    public void addComponent(Component component) {
        components.add(component);
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public List<Component> getComponents() {
        return components;
    }
}
