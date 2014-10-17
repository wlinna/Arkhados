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
import arkhados.gamemode.GameMode;
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
    // TODO: Consider if we really want to put gameMode here or not
    public static GameMode gameMode = null;

    public static void harm(InfluenceInterfaceControl attacker,
            InfluenceInterfaceControl target, final float rawDamage,
            List<AbstractBuff> buffs, boolean canBreakCC) {

        if (target == null) {
            return;
        }

        if (target.isDead()) {
            return;
        }

        final float damageDone = target.doDamage(rawDamage, canBreakCC);

        int attackerPlayerId;

        if (attacker != null) {
            Spatial attackerSpatial = attacker.getSpatial();
            float lifeSteal = attackerSpatial.getUserData(UserDataStrings.LIFE_STEAL);
            float lifeStolen = lifeSteal * damageDone;
            attacker.heal(lifeStolen);

            attackerPlayerId = attackerSpatial.getUserData(UserDataStrings.PLAYER_ID);
            getCurrentRoundStats().addDamageForPlayer(attackerPlayerId, damageDone);
            getCurrentRoundStats().addHealthRestorationForPlayer(attackerPlayerId, lifeStolen);
        } else {
            attackerPlayerId = -1;
        }

        if (target.isDead()) {
            getCurrentRoundStats().addKill(attackerPlayerId);
            int deadPlayerId = target.getSpatial().getUserData(UserDataStrings.PLAYER_ID);
            gameMode.playerDied(deadPlayerId, attackerPlayerId);
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