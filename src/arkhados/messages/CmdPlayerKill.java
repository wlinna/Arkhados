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
public class CmdPlayerKill implements Command {
    private byte diedPlayerId;
    private byte killerPlayerId;
    private byte killingSpree;
    private byte combo;
    private byte endedSpree;

    public CmdPlayerKill() {
    }

    public CmdPlayerKill(int diedPlayerId, int killerPlayerId,
            int killingSpree, int combo, int endedSpree) {
        this.diedPlayerId = (byte) diedPlayerId;
        this.killerPlayerId = (byte) killerPlayerId;
        this.killingSpree = (byte) killingSpree;
        this.combo = (byte) combo;
        this.endedSpree = (byte) endedSpree;
    }

    @Override
    public boolean isGuaranteed() {
        return true;
    }

    public int getDiedPlayerId() {
        return diedPlayerId;
    }

    public int getKillerPlayerId() {
        return killerPlayerId;
    }

    public int getKillingSpree() {
        return killingSpree;
    }
    
    public int getCombo() {
        return combo;
    }

    public int getEndedSpree() {
        return endedSpree;
    }
}