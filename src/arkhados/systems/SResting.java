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
package arkhados.systems;

import arkhados.components.CResting;
import arkhados.controls.InfluenceInterfaceControl;
import com.jme3.app.state.AbstractAppState;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class SResting extends AbstractAppState {

    private List<CResting> components = new ArrayList<>();
    
    private void regenerate(float tpf, CResting component) {
        InfluenceInterfaceControl influenceInterface =
                component.spatial.getControl(InfluenceInterfaceControl.class);
        influenceInterface.heal(2.1f * component.idleTime * tpf);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        for (CResting cResting : components) {
            Vector3f newLocation = cResting.spatial.getLocalTranslation();
            if (newLocation.distanceSquared(cResting.previousLocation) > 0.1f) {
                cResting.idleTime = 0f;                
            }

            cResting.idleTime += tpf;
            cResting.previousLocation.set(newLocation);
            
            if (cResting.idleTime > 2.5f) {
                regenerate(tpf, cResting);
            }
        }
    }
    
    public void addComponent(CResting component) {
        components.add(component);
    }
}
