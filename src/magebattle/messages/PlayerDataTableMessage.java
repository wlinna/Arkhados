/*    This file is part of JMageBattle.

    JMageBattle is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JMageBattle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */

package magebattle.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import java.util.List;
import magebattle.PlayerData;

/**
 *
 * @author william
 */
@Serializable
public class PlayerDataTableMessage extends AbstractMessage {
    private List<String> names;

    public List<String> getNames() {
        return names;
    }

    public PlayerDataTableMessage() {

    }
    private PlayerDataTableMessage(List<String> names) {
        this.names = names;
    }

    public static PlayerDataTableMessage makeFromPlayerDataList() {
        List<PlayerData> list = PlayerData.getPlayers();
        List<String> dataTable = new ArrayList<String>(list.size());
        for (PlayerData data : list) {
            dataTable.add(data.getStringData("name"));
        }

        return new PlayerDataTableMessage(dataTable);
    }

}
