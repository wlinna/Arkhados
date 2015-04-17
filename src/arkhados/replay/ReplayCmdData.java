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

import arkhados.net.Command;
import com.jme3.network.serializing.Serializable;

@Serializable
public class ReplayCmdData {
    private byte playerId;
    private Command command;
    private float time;

    public ReplayCmdData() {
    }

    public ReplayCmdData(int playerId, Command command, float time) {
        this.playerId = (byte) playerId;
        this.command = command;
        this.time = time;
    }

    public Command getCommand() {
        return command;
    }

    public float getTime() {
        return time;
    }        

    public int getPlayerId() {
        return playerId;
    }
}
