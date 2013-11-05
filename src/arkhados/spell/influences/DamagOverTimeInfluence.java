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
package arkhados.spell.influences;

import arkhados.controls.InfluenceInterfaceControl;

/**
 * @author william
 */
public class DamagOverTimeInfluence implements Influence {

    private float dps;

    public DamagOverTimeInfluence(float dps) {
        this.dps = dps;
    }

    public void affect(final InfluenceInterfaceControl targetInterface, final float tpf) {
        if (targetInterface != null) {
            // FIXME: Rounding errors cause significant changes in total damage
            targetInterface.doDamage(this.dps * tpf, true);
        }
    }

    public boolean isFriendly() {
        return false;
    }
}