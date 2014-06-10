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
import arkhados.util.RoundStats;
import arkhados.util.UserDataStrings;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class CharacterInteraction {
    private static ArrayList<RoundStats> roundStatList = new ArrayList<>();

    public static void harm(final InfluenceInterfaceControl attacker,
            final InfluenceInterfaceControl target, final float rawDamage,
            final List<AbstractBuff> buffs, final boolean canBreakCC) {

        if (target == null) {
            return;
        }
        
        if (target.isDead()) {
            return;
        }

        final float damageDone = target.doDamage(rawDamage, canBreakCC);

        if (attacker != null) {
            final Spatial attackerSpatial = attacker.getSpatial();
            final Float lifeSteal = attackerSpatial.getUserData(UserDataStrings.LIFE_STEAL);
            final float lifeStolen = lifeSteal * damageDone;
            attacker.heal(lifeStolen);

            final Integer playerId = attackerSpatial.getUserData(UserDataStrings.PLAYER_ID);
            getCurrentRoundStats().addDamageForPlayer(playerId, damageDone);
            getCurrentRoundStats().addHealthRestorationForPlayer(playerId, lifeStolen);

            if (target.isDead()) {
                getCurrentRoundStats().addKill(playerId);
            }
        }
        if (buffs != null) {
            for (AbstractBuff buff : buffs) {
                if (buff != null && !buff.isFriendly()) {
                    buff.attachToCharacter(target);
                } else if (buff == null) {
                    System.out.println("Null in buff-list");
                }
            }
        }
    }

    public static void startNewRound() {
        final RoundStats roundStats = new RoundStats();
        roundStats.initializeRound();
        roundStatList.add(roundStats);
    }

    public static RoundStats getCurrentRoundStats() {
        return roundStatList.get(roundStatList.size() - 1);
    }

    public static void cleanup() {
        roundStatList.clear();
    }
}