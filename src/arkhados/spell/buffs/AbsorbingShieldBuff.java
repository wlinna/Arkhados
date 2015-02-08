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

public class AbsorbingShieldBuff extends AbstractBuff {

    private float cap = 220f;

    {
        friendly = true;
        name = "Purifying Flame";
        setTypeId(BuffTypeIds.PURIFYING_FLAME);
    }

    public AbsorbingShieldBuff(int buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void update(float time) {
        super.update(time);
        targetInterface.setImmuneToProjectiles(true);
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && cap > 0f;
    }
    
    public void reduce(float dmg) {
        cap -= dmg;
    }
}