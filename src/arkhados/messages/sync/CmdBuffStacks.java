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

package arkhados.messages.sync;

import arkhados.controls.CCharacterBuff;
import arkhados.messages.sync.statedata.StateData;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Node;

@Serializable
public class CmdBuffStacks extends StateData{
    private int buffId;
    private byte stacks;

    public CmdBuffStacks() {
    }

    public CmdBuffStacks(int entityId, int buffId, int stacks) {
        super(entityId);
        this.buffId = buffId;
        this.stacks = (byte) stacks;
    }

    @Override
    public void applyData(Object target) {
        Node node = (Node) target;
        node.getControl(CCharacterBuff.class).changeStacks(buffId, stacks);
        
    }
}
