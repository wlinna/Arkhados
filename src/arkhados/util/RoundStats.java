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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author william
 */
public class RoundStats {

    private HashMap<Integer, PlayerRoundStats> playerStats = new HashMap<>();    
    
    public void initializeRound() {
        final List<PlayerData> playerDataList = PlayerData.getPlayers();
        for (PlayerData playerData : playerDataList) {
            playerStats.put(playerData.getId(), new PlayerRoundStats(playerData.getId()));
        }
    }
    
    public void addDamageForPlayer(Integer playerId, float damage) {
        PlayerRoundStats player = playerStats.get(playerId);
        if (player == null) {
            // TODO: Consider throwing exception here
            return;
        }

        player.damageDone += damage;
    }

    public void addHealthRestorationForPlayer(final Integer playerId, float restoration) {
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
    
    public ArrayList<PlayerRoundStats> buildCurrentPlayerRoundStatsList() {
        ArrayList<PlayerRoundStats> playerRoundStatsList = new ArrayList<>(playerStats.values());
        return playerRoundStatsList;
    }
}