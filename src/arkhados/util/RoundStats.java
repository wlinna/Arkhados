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
package arkhados.util;

import arkhados.PlayerData;
import com.jme3.util.IntMap;
import java.util.ArrayList;
import java.util.List;

public class RoundStats {

    private final IntMap<PlayerRoundStats> playerStats = new IntMap<>();

    public void initialize() {
        List<PlayerData> playerDataList = PlayerData.getPlayers();
        for (PlayerData playerData : playerDataList) {
            playerStats.put(playerData.getId(),
                    new PlayerRoundStats(playerData.getId()));
        }
    }

    public void addPlayer(int playerId) {
        playerStats.put(playerId, new PlayerRoundStats(playerId));
    }

    public void removePlayer(int playerId) {
        playerStats.remove(playerId);
    }

    public void addDamageForPlayer(Integer playerId, float damage) {
        PlayerRoundStats player = playerStats.get(playerId);
        if (player == null) {
            // TODO: Consider throwing exception here
            return;
        }

        player.damageDone += damage;
    }

    public void addHealthRestorationForPlayer(final Integer playerId,
            float restoration) {
        PlayerRoundStats player = playerStats.get(playerId);
        if (player == null) {
            // TODO: Consider throwing exception here
            return;
        }

        player.healthRestored += restoration;
    }

    public void addKill(int playerId) {
        if (playerId == -1) {
            return;
        }

        PlayerRoundStats player = playerStats.get(playerId);
        if (player == null) {
            // TODO: Consider throwing exception here
            return;
        }

        player.kills++;
    }

    public int getKills(int playerId) {
        if (playerId == -1) {
            return -1;
        }

        PlayerRoundStats player = playerStats.get(playerId);
        if (player == null) {
            return -1;
        }

        return player.kills;
    }

    public ArrayList<PlayerRoundStats> cloneCurrentPlayerRoundStatsList() {
        ArrayList<PlayerRoundStats> list = new ArrayList<>(playerStats.size());
        for (IntMap.Entry<PlayerRoundStats> entry : playerStats) {                    
            list.add(new PlayerRoundStats(entry.getValue()));
        }

        return list;
    }
}