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

import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.SpellCastControl;
import arkhados.messages.syncmessages.AbstractSyncMessage;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
@Serializable
public class UcMouseTargetMessage extends AbstractSyncMessage {

    private Vector3f location;

    public UcMouseTargetMessage() {
    }

    public UcMouseTargetMessage(Vector3f location) {
        this.location = location;
    }

    @Override
    public void applyData(Object target) {
        Spatial character = (Spatial) target;
        CharacterPhysicsControl physicsControl = character.getControl(CharacterPhysicsControl.class);
        physicsControl.setTargetLocation(this.location);        
        
        if (!physicsControl.isMotionControlled()
                && physicsControl.getWalkDirection().equals(Vector3f.ZERO)) {
            physicsControl.lookAt(this.location);
        }
    }
}
