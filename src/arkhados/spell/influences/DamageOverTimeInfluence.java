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

import arkhados.CharacterInteraction;
import arkhados.controls.CInfluenceInterface;

/**
 * @author william
 */
public class DamageOverTimeInfluence extends AbstractInfluence {

    private float dps;
    private boolean breaksCrowdControl = true;

    public DamageOverTimeInfluence(float dps) {
        this.dps = dps;
    }

    @Override
    public void affect(CInfluenceInterface targetInterface, float tpf) {
        if (targetInterface != null) {
            // FIXME: Rounding errors cause significant changes in total damage
            CharacterInteraction.harm(getOwner(), targetInterface, dps * tpf, null,
                    breaksCrowdControl);
        }
    }

    @Override
    public boolean isFriendly() {
        return false;
    }

    public void setBreaksCrowdControl(boolean breaksCrowdControl) {
        this.breaksCrowdControl = breaksCrowdControl;
    }
}