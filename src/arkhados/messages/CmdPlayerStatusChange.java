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
public class CmdPlayerStatusChange implements Command {
    private byte playerId;
    private byte newStatus;

    public CmdPlayerStatusChange() {
    }

    public CmdPlayerStatusChange(int playerId) {
        this.playerId = (byte) playerId;
    }

    @Override
    public boolean isGuaranteed() {
        return true;
    }

    public int getPlayerId() {
        return playerId;
    }
    
    public boolean joined() {
        return newStatus == 0;
    }
    
    public boolean left() {
        return newStatus == 1;
    }

    public CmdPlayerStatusChange setJoined() {
        newStatus = 0;
        return this;
    }
    
    public CmdPlayerStatusChange setLeft() {
        newStatus = 1;
        return this;
    }
}
