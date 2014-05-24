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

import com.jme3.network.serializing.Serializable;

/**
 *
 * @author william
 */

@Serializable
public abstract class StateSyncMessage extends AbstractSyncMessage{
    private transient static long globalStateSyncMessageCounter = 0;
    private long orderNum = globalStateSyncMessageCounter++;    

    public StateSyncMessage() {
    }
    
    public StateSyncMessage(long id) {
        super(id, true);
    }
    
    public void resetGlobalStateSyncMessageCounter() {
        globalStateSyncMessageCounter = 0;
    }

    public long getOrderNum() {
        return this.orderNum;
    }       
}
