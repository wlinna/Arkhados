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

import arkhados.controls.CCharacterAnimation;
import arkhados.messages.syncmessages.statedata.StateData;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */

@Serializable
public class CmdAction extends StateData {
    private byte actionId;

    public CmdAction() {
    }

    public CmdAction(int id, int actionId) {
        super(id);
        this.actionId = (byte) actionId;
    }

    @Override
    public void applyData(Object target) {
        Spatial character = (Spatial) target;
        character.getControl(CCharacterAnimation.class).animateAction(this.actionId);
    }

    public int getActionId() {
        return actionId;
    }
}
