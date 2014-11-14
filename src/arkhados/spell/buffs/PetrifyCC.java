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

import arkhados.util.BuffTypeIds;

/**
 *
 * @author william
 */
public class PetrifyCC extends CrowdControlBuff {
    {
        setTypeId(BuffTypeIds.PETRIFY);
    }
        
    private final float damageCap = 100;
    private float totalDamageTaken = 0f;
    private final float damageReduction = 0.95f;

    public PetrifyCC(int id, float duration) {
        super(id, duration);
    }

    public float damage(float damage) {
        float damageTaken = damage * (1 - damageReduction);
        totalDamageTaken += damageTaken;
        return damageTaken;
    }

    @Override
    public boolean shouldContinue() {
        if (super.shouldContinue()) {
            return totalDamageTaken <= damageCap;
        }

        return false;
    }
}