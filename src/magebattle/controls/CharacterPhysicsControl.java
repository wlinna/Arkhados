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
package magebattle.controls;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.Queue;

/**
 *
 * @author william
 */
public class CharacterPhysicsControl extends BetterCharacterControl {

    private Vector3f impulseToApply = null;

    public CharacterPhysicsControl(float radius, float height, float mass) {
        super(radius, height, mass);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        super.rigidBody.setUserObject(spatial);
    }



    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        if (impulseToApply != null) {
            this.rigidBody.applyImpulse(this.impulseToApply.setY(10.0f), Vector3f.ZERO);
            this.impulseToApply = null;
        }
        else {
            super.prePhysicsTick(space, tpf);
        }
    }

    public void applyImpulse(Vector3f impulse) {

        this.impulseToApply = impulse;
//        this.rigidBody.applyForce(impulse, location);
    }

    public void setVelocity(Vector3f velocity) {
        this.rigidBody.setLinearVelocity(velocity);
    }
}
