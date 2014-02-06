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
package arkhados.spell.buffs;

import arkhados.CharacterInteraction;

/**
 *
 * @author william
 */
public class DamageOverTimeBuff extends AbstractBuff {
    /**
     * Damage per second
     */
    private float dps;
    private float time;

    public DamageOverTimeBuff(long buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        this.time += tpf;        
        if (time > 0.05) {            
            CharacterInteraction.harm(super.getOwnerInterface(), super.targetInterface, this.dps * time, null, false);
            this.time = 0f;
        }
    }

    public void setDps(float dps) {
        this.dps = dps;
    }
}
