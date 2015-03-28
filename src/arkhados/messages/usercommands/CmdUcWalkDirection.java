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
package arkhados.messages.usercommands;

import com.jme3.network.serializing.Serializable;
import arkhados.messages.syncmessages.statedata.StateData;

/**
 *
 * @author william
 */
@Serializable
public class CmdUcWalkDirection extends StateData {

    private byte down;
    private byte right;

    public CmdUcWalkDirection() {
    }

    public CmdUcWalkDirection(int down, int right) {
        this.down = (byte) down;
        this.right = (byte) right;
    }

    @Override
    public void applyData(Object target) {
    }       

    public byte getDown() {
        return down;
    }

    public byte getRight() {
        return right;
    }
}
