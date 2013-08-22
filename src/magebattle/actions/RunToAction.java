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

package magebattle.actions;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class RunToAction extends EntityAction {

    private BetterCharacterControl physicsControl;
    private Vector3f targetLocation;
    private final float arrivalRangeSquared = FastMath.sqr(1.0f);

    public RunToAction(Vector3f targetLocation) {
        this.targetLocation = targetLocation;
    }

    @Override
    public boolean update(float tpf) {
        Vector3f direction = this.targetLocation
                .subtract(super.spatial.getLocalTranslation());
        float lengthSquared = direction.lengthSquared();
        if (lengthSquared > this.arrivalRangeSquared) {
            float ms = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
            direction.normalizeLocal().multLocal(ms);
            // TODO: Implement continuous turning
            this.physicsControl.setViewDirection(direction);
            this.physicsControl.setWalkDirection(direction);
            return true;
        } else {
//            this.physicsControl.setViewDirection(direction);
            this.physicsControl.setWalkDirection(Vector3f.ZERO);
            return false;
        }
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        this.physicsControl = spatial.getControl(BetterCharacterControl.class);
        assert this.physicsControl != null;
    }

    @Override
    public void end() {
        this.physicsControl.setWalkDirection(Vector3f.ZERO);
    }

    public void setTargetLocation(Vector3f targetLocation) {
        this.targetLocation = targetLocation;
    }
}
