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
import com.jme3.scene.Node;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.UserInputControl;
import arkhados.messages.syncmessages.AbstractSyncMessage;

/**
 *
 * @author william
 */
@Serializable
public class UcWalkDirection extends AbstractSyncMessage {

    private int down;
    private int right;

    public UcWalkDirection() {
    }

    public UcWalkDirection(int down, int right) {
        this.down = down;
        this.right = right;
    }

    @Override
    public void applyData(Object target) {
        Node character = (Node) target;
        final UserInputControl inputControl = character.getControl(UserInputControl.class);
        if (inputControl == null) {
            return;
        }

        inputControl.setUpDownDirection(this.right, this.down);
    }
}
