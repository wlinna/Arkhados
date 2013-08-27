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
package magebattle.messages.syncmessages;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import magebattle.actions.RunToAction;
import magebattle.controls.ActionQueueControl;
import magebattle.controls.CharacterMovementControl;
import magebattle.controls.CharacterPhysicsControl;

/**
 *
 * @author william
 */
@Serializable
public class SyncCharacterMessage extends AbstractSyncMessage {

    private Vector3f location = new Vector3f();
    private Vector3f walkDirection = new Vector3f();
    private Vector3f viewDirection = new Vector3f();

    public SyncCharacterMessage() {
    }

    public SyncCharacterMessage(long id, Object object) {
        super(id);
        Spatial spatial = (Spatial) object;
        this.location.set(spatial.getLocalTranslation());
        this.walkDirection.set(spatial.getControl(CharacterPhysicsControl.class).getWalkDirection());
        this.viewDirection.set(spatial.getControl(CharacterPhysicsControl.class).getViewDirection());
    }

    public void readData(CharacterMovementControl control) {
    }

    @Override
    public void applyData(Object target) {
        Spatial character = (Spatial) target;
        character.getControl(CharacterPhysicsControl.class).warp(this.location);
//        character.getControl(CharacterMovementControl.class).runTo(this.location.add(this.walkDirection));
        ActionQueueControl actionQueue = character.getControl(ActionQueueControl.class);
//        if (actionQueue.getCurrent() instanceof RunToAction) {
//            RunToAction action = (RunToAction) actionQueue.getCurrent();
//            action.setTargetLocation(this.location.add(this.walkDirection));
//        } else {
        actionQueue.clear();
        actionQueue.enqueueAction(new RunToAction(this.location.addLocal(this.walkDirection)));
        character.getControl(CharacterPhysicsControl.class).setViewDirection(this.viewDirection);
//        }
    }
}