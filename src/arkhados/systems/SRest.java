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

import arkhados.components.CRest;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.util.ComponentArranger;
import arkhados.util.ComponentSystemMap;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class SRest extends AbstractAppState implements SSystem<CRest> {

    private List<CRest> components = new ArrayList<>();
    private SHeal sHeal;
    private ComponentArranger<CRest> arranger =
            new ComponentArranger<>(components);

    public SRest(SHeal sHeal) {
        this.sHeal = sHeal;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        ComponentSystemMap.get().put(CRest.class, this);
    }

    private void regenerate(float tpf, CRest component) {
        InfluenceInterfaceControl target =
                component.spatial.getControl(InfluenceInterfaceControl.class);
        sHeal.heal(target, 2.1f * component.idleTime * tpf);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        for (CRest cResting : components) {
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

    @Override
    public void addComponent(int entityId, CRest component) {
        arranger.add(entityId, component);
    }

    @Override
    public void removeEntity(int entityId) {
        arranger.remove(entityId);
    }
}
