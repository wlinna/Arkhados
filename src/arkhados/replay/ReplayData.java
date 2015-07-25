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
package arkhados.replay;

import arkhados.PlayerData;
import arkhados.net.Command;
import com.jme3.network.Message;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Has all data that is needed to allow players to replay game. First version is
 * implemented as Message for easiness.
 *
 * @author william
 */
@Serializable
public class ReplayData implements Message {

    private ReplayHeader header = new ReplayHeader();
    private List<ReplayCmdData> commands = new ArrayList<>();

    public ReplayData() {
    }

    public void addCommand(Command cmd, int playerId, float time) {
        ReplayCmdData cmdData = new ReplayCmdData(playerId, cmd, time);

        String name =
                PlayerData.getStringData(playerId, PlayerData.NAME);
        header.getPlayers().put(playerId, name);

        commands.add(cmdData);
    }

    @Override
    public Message setReliable(boolean f) {
        return this;
    }

    @Override
    public boolean isReliable() {
        return false;
    }

    public ReplayHeader getHeader() {
        return header;
    }

    public List<ReplayCmdData> getCommands() {
        return commands;
    }
}
