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

import arkhados.controls.CInfluenceInterface;
import arkhados.util.BuffTypeIds;


public class BrimstoneBuff extends AbstractBuff {
    {
        setTypeId(BuffTypeIds.BRIMSTONE);
    }
    
    private static float maxDuration;
    private static final int stackCap = 3;

    public BrimstoneBuff(float duration) {
        super(duration);
        maxDuration = duration;
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        BrimstoneBuff existingBrimstone = null;
        for (AbstractBuff buff : targetInterface.getBuffs()) {
            if (buff instanceof BrimstoneBuff) {
                existingBrimstone = (BrimstoneBuff) buff;
                break;
            }
        }

        if (existingBrimstone != null) {
            existingBrimstone.duration = maxDuration;
            int existingStacks = existingBrimstone.getStacks();
            if (existingStacks < stackCap) {
                existingBrimstone.changeStackAmount(existingStacks + 1);
            }
        } else {
            super.attachToCharacter(targetInterface);
        }
    }
}
