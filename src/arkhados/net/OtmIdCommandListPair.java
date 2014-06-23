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
package arkhados.net;

import com.jme3.network.serializing.Serializable;
import java.util.List;

/**
 *
 * @author william
 */

@Serializable
public class OtmIdCommandListPair {
    private int otmId;
    private List<Command> commandList;

    public OtmIdCommandListPair() {
    }

    public OtmIdCommandListPair(int otmId, List<Command> commandList) {
        this.otmId = otmId;
        this.commandList = commandList;
    }

    public int getOtmId() {
        return otmId;
    }

    public List<Command> getCommandList() {
        return commandList;
    }        
}
