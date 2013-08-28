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

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class CharacterMovementControl extends AbstractControl {

    private Vector3f currentTargetLocation = new Vector3f();
    /**
     * This is just reference to spatials location
     */
    private CharacterPhysicsControl physicsControl;
    private Spatial character;
    private final float arrivalRangeSquared = FastMath.sqr(1.0f);

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        this.character = spatial;
        this.physicsControl = this.character
                .getControl(CharacterPhysicsControl.class);
        assert this.physicsControl != null;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if ((Float)super.spatial.getUserData(UserDataStrings.HEALTH_CURRENT) == 0.0f)
        {
            this.setEnabled(false);
        }
        Vector3f direction = this.currentTargetLocation
                .subtract(this.character.getLocalTranslation());
        float lengthSquared = direction.lengthSquared();
        if (lengthSquared > this.arrivalRangeSquared) {
            float ms = this.character.getUserData(UserDataStrings.SPEED_MOVEMENT);
            direction.normalizeLocal().multLocal(ms);
            this.physicsControl.setViewDirection(direction);
            this.physicsControl.setWalkDirection(direction);
        } else {
            this.physicsControl.setWalkDirection(Vector3f.ZERO);
        }

        // TODO: Implement continuous turning. Don't turn instantaneously
//            System.out.println(String.format("Direction: %f %f %f", direction.x, direction.y, direction.z));
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        CharacterMovementControl control = new CharacterMovementControl();
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
    }

    public Vector3f getLocation() {
        return this.spatial.getLocalTranslation();
    }

    public CharacterPhysicsControl getPhysicsControl() {
        return this.physicsControl;
    }

    public void runTo(Vector3f newTargetLocation) {
        this.currentTargetLocation = newTargetLocation;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
