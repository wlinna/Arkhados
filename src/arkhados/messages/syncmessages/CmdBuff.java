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

import arkhados.controls.CCharacterBuff;
import arkhados.messages.syncmessages.statedata.StateData;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
@Serializable
public class CmdBuff extends StateData {

    private short buffTypeId;
    private int buffId;
    private float duration;
    private boolean added;

    public CmdBuff() {
    }

    public CmdBuff(int entityId, int buffTypeId, int buffId, float duration,
            boolean added) {
        super(entityId);
        this.buffTypeId = (short) buffTypeId;
        this.buffId = buffId;
        this.duration = duration;
        this.added = added;
    }

    @Override
    public void applyData(Object target) {
        Node node = (Node) target;
        CCharacterBuff buffControl = node.getControl(CCharacterBuff.class);
        // TODO: Add to InfluenceInterfaceControl so that its non-visual effects
        // can be simulated
        if (added) {
            buffControl.addBuff(buffId, buffTypeId, duration);
        } else {
            buffControl.removeBuff(buffId);
        }
    }
}