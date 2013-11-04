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
package arkhados.spell.buffs;

import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class FearCC extends CrowdControlBuff {
    private Vector3f initialDirection = new Vector3f();
    public FearCC(long id, float duration) {
        super(id, duration);
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl influenceInterface) {
        super.attachToCharacter(influenceInterface);
        Spatial spatial = influenceInterface.getSpatial();
        CharacterPhysicsControl characterPhysicsControl = spatial.getControl(CharacterPhysicsControl.class);
        Float speedMovement = spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
        characterPhysicsControl.setWalkDirection(initialDirection.multLocal(speedMovement));
        characterPhysicsControl.setViewDirection(initialDirection);
    }

    public void setInitialDirection(Vector3f initialDirection) {
        this.initialDirection = initialDirection;
    }

    @Override
    public void update(float time) {
        super.update(time);
        this.targetInterface.setCanControlMovement(false);
    }


}
