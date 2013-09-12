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
package arkhados.spells.influences;

import com.jme3.scene.Spatial;
import arkhados.controls.InfluenceInterfaceControl;

/**
 * @author william
 */
public class DamagOverTimeeInfluence implements Influence {

    private float dps;

    public DamagOverTimeeInfluence(float dps) {
        this.dps = dps;
    }

    public void affect(Spatial spatial, float tpf) {
        InfluenceInterfaceControl characterInfluenceControl = spatial.getControl(InfluenceInterfaceControl.class);
        if (characterInfluenceControl != null) {
            // FIXME: Roundind errors cause significant changes in total damage
            characterInfluenceControl.doDamage(this.dps * tpf);
        }
    }
}