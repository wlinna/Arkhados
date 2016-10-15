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
public class CmdBuff extends StateData {

    public short buffTypeId;
    public int buffId;
    public float duration;    
    public byte stacks;
    private byte flags = 0;

    public CmdBuff() {
    }

    public CmdBuff(int entityId, int buffTypeId, int buffId,
            float duration, int stacks, boolean added) {
        super(entityId);
        this.buffTypeId = (short) buffTypeId;
        this.buffId = buffId;
        this.duration = duration;
        this.stacks = (byte) stacks;
        setAdded(added);
        setJustCreated(true);
    }

    @Override
    public void applyData(Object target) {
        Node node = (Node) target;
        CCharacterBuff cBuff = node.getControl(CCharacterBuff.class);

        if (getAdded()) {
            cBuff.addBuff(this);
        } else {
            cBuff.removeBuff(buffId);
        }
    }

    public boolean getAdded() {
        return (flags & 1) == 1;
    }

    public final void setAdded(boolean value) {
        flags = (byte) (value ? (flags | 1) : (flags & ~1));
    }

    public boolean getJustCreated() {
        return (flags & 2) == 2;
    }
    
    public final void setJustCreated(boolean value) {
        flags = (byte) (value ? (flags | 2) : (flags & ~2));
    }
}