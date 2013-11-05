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
package arkhados;

import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.UserDataStrings;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 *
 * @author william
 */
public class CharacterInteraction {

    public static void harm(final InfluenceInterfaceControl attacker,
            final InfluenceInterfaceControl target, final float rawDamage,
            final List<AbstractBuff> buffs, final boolean canBreakCC) {

        final float damageDone = target.doDamage(rawDamage, canBreakCC);

        final Spatial attackerSpatial = attacker.getSpatial();
        final Float lifeSteal = attackerSpatial.getUserData(UserDataStrings.LIFE_STEAL);
        final float lifeStolen = lifeSteal * damageDone;
        attacker.heal(lifeStolen);

        if (buffs != null) {
            for (AbstractBuff buff : buffs) {
                buff.attachToCharacter(target);
            }
        }

        // TODO: Collect stats
    }


}
