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
package arkhados.messages.syncmessages;

import arkhados.messages.syncmessages.statedata.StateData;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.List;

/**
 *
 * @author william
 */
@Serializable
public class MassSyncMessage extends AbstractMessage {

    private transient static int globalStateSyncMessageCounter = 0;
    private int orderNum = globalStateSyncMessageCounter++;
    private List<StateData> stateData;

    public MassSyncMessage() {
        super(false);
    }

    public List<StateData> getStateData() {
        return stateData;
    }

    public void setStateData(List<StateData> stateData) {
        this.stateData = stateData;
    }

    public void resetGlobalStateSyncMessageCounter() {
        globalStateSyncMessageCounter = 0;
    }

    public int getOrderNum() {
        return this.orderNum;
    }
}