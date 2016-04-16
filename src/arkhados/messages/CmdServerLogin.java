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

package arkhados.messages;

import arkhados.net.Command;
import com.jme3.network.serializing.Serializable;

@Serializable
public class CmdServerLogin implements Command {
    private String name;
    private int playerId;
    private boolean accepted;    
    private String gameMode;

    public CmdServerLogin() {
    }

    public CmdServerLogin(String nick, int playerId, boolean accepted, String gameMode) {
        this.name = nick;
        this.playerId = playerId;
        this.accepted = accepted;
        this.gameMode = gameMode;
    }

    public String getName() {
        return name;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public boolean isGuaranteed() {
        return true;
    }

    public String getGameMode() {
        return gameMode;
    }
}