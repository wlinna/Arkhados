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

import arkhados.controls.CCharacterDamage;
import arkhados.controls.CCharacterHeal;
import arkhados.controls.CInfluenceInterface;
import arkhados.gamemode.GameMode;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.Builder;
import arkhados.util.RoundStats;
import arkhados.util.UserData;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterInteraction {

    private static ArrayList<RoundStats> roundStatList = new ArrayList<>();
    // TODO: Consider if we really want to put gameMode here or not
    public static GameMode gameMode = null;
    private static final Map<Integer, Integer> latestDamager = new HashMap<>();

    public static void harm(CInfluenceInterface attacker,
            CInfluenceInterface target, final float rawDamage,
            List<AbstractBuffBuilder> buffBuilders, boolean canBreakCC) {

        if (target == null || target.isDead()) {
            return;
        }

        final float damageDone = target.getSpatial()
                .getControl(CCharacterDamage.class)
                .doDamage(rawDamage, canBreakCC);

        int targetPlayerId = target.getSpatial()
                .getUserData(UserData.PLAYER_ID);
        int attackerPlayerId;

        if (attacker != null) {
            Spatial attackerSpatial = attacker.getSpatial();
            float lifeSteal = attackerSpatial
                    .getUserData(UserData.LIFE_STEAL);
            float lifeStolen = lifeSteal * damageDone;

            attackerSpatial.getControl(CCharacterHeal.class).heal(lifeStolen);

            attackerPlayerId =
                    attackerSpatial.getUserData(UserData.PLAYER_ID);
            getCurrentRoundStats()
                    .addDamageForPlayer(attackerPlayerId, damageDone);
            getCurrentRoundStats()
                    .addHealthRestorationForPlayer(attackerPlayerId,
                    lifeStolen);

            latestDamager.put(targetPlayerId, attackerPlayerId);
        } else {
            attackerPlayerId = -1;
        }

        if (target.isDead()) {
            Integer latestDamagerId = latestDamager.get(targetPlayerId);
            if (latestDamagerId != null) {
                latestDamager.remove(latestDamagerId);
                getCurrentRoundStats().addKill(latestDamagerId);
            } else {
                latestDamagerId = -1;
            }
            gameMode.playerDied(targetPlayerId, latestDamagerId);
        }

        if (buffBuilders != null) {
            for (Builder<AbstractBuff> buffBuilder : buffBuilders) {
                if (buffBuilder == null) {
                    System.out.println("Null buffBuilder in buff-list");
                    continue;
                }

                AbstractBuff buff = buffBuilder.build();

                if (buff.getOwnerInterface() == null) {
                    buff.setOwnerInterface(attacker);
                }

                if (!buff.isFriendly()) {
                    buff.attachToCharacter(target);
                }
            }
        }
    }

    public static void help(CInfluenceInterface healer,
            CInfluenceInterface target, float heal,
            List<AbstractBuffBuilder> buffBuilders) {

        if (target == null || target.isDead()) {
            return;
        }

        float healingDone = target.getSpatial()
                .getControl(CCharacterHeal.class).heal(heal);

        if (healer != null) {
            int healerPlayerId = healer.getSpatial()
                    .getUserData(UserData.PLAYER_ID);
            getCurrentRoundStats().addHealthRestorationForPlayer(
                    healerPlayerId, healingDone);
        }

        if (buffBuilders != null) {
            for (Builder<AbstractBuff> buffBuilder : buffBuilders) {
                if (buffBuilder == null) {
                    System.out.println("Null buffBuilder");
                    continue;
                }

                AbstractBuff buff = buffBuilder.build();

                if (buff == null) {
                    System.out.println("Builder built null buff");
                    continue;
                }

                if (buff.isFriendly()) {
                    if (buff.getOwnerInterface() == null) {
                        buff.setOwnerInterface(healer);
                    }

                    buff.attachToCharacter(target);
                }
            }
        }
    }

    public static void startNewRound() {
        RoundStats roundStats = new RoundStats();
        roundStats.initialize();
        roundStatList.add(roundStats);
    }

    public static void addPlayer(int playerId) {
        RoundStats round = roundStatList.get(roundStatList.size() - 1);
        round.addPlayer(playerId);
    }

    public static void removePlayer(int playerId) {
        RoundStats round = roundStatList.get(roundStatList.size() - 1);
        round.removePlayer(playerId);
    }

    public static RoundStats getCurrentRoundStats() {
        return roundStatList.get(roundStatList.size() - 1);
    }

    public static void cleanup() {
        roundStatList.clear();
    }
}