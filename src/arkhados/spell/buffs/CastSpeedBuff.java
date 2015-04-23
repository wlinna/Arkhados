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

public class CastSpeedBuff extends AbstractBuff {
    // TODO: Add type id and icon

    private float factor;

    public CastSpeedBuff(int id, float duration, float by) {
        super(id, duration);
        factor = 1f + by;
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        super.attachToCharacter(targetInterface);
        targetInterface.getCastSpeedBuffs().add(this);
    }

    @Override
    public void destroy() {
        super.destroy();
        targetInterface.getCastSpeedBuffs().remove(this);
    }

    public float getFactor() {
        return factor;
    }
}
