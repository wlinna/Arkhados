/*    This file is part of JMageBattle.

    JMageBattle is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JMageBattle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */

package magebattle.messages.usercommands;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Node;
import magebattle.actions.RunToAction;
import magebattle.controls.ActionQueueControl;
import magebattle.messages.syncmessages.AbstractSyncMessage;

/**
 *
 * @author william
 */
@Serializable
public class UcRunToMessage extends AbstractSyncMessage {

    private Vector3f location;

    public UcRunToMessage() {
    }

    public UcRunToMessage(Vector3f location) {
        this.location = location;
    }

    @Override
    public void applyData(Object target) {
        Node character = (Node) target;
//        CharacterMovementControl movementControl = character.getControl(CharacterMovementControl.class);
        BetterCharacterControl characterControl = character.getControl(BetterCharacterControl.class);

        if (characterControl != null) {
            character.getControl(ActionQueueControl.class).clear();
            character.getControl(ActionQueueControl.class).enqueueAction(new RunToAction(this.location));
//            movementControl.runTo(this.location);
        }
    }

    public Vector3f getLocation() {
        return this.location;
    }
}
