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
package arkhados.controls;

import arkhados.PlayerData;
import arkhados.util.PlayerDataStrings;
import arkhados.util.UserDataStrings;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author william
 */
public class UserInputControl extends AbstractControl {

    private int previousRight = 0;
    private int previousDown = 0;

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void setUpDownDirection(int right, int down) {
        InfluenceInterfaceControl influenceInterface = super.spatial.getControl(InfluenceInterfaceControl.class);
        this.saveDirection(right, down);
        if (!influenceInterface.canControlMovement()) {
            return;
        }

        if (influenceInterface.canMove()) {
            Vector3f newWalkDirection = new Vector3f(right, 0f, down);
            Float speedMovement = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
            newWalkDirection.normalizeLocal().multLocal(speedMovement);
            super.spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(newWalkDirection);
            if (down != 0 || right != 0) {
                if (!influenceInterface.isAbleToCastWhileMoving()) {
                    Long playerId = super.spatial.getUserData(UserDataStrings.PLAYER_ID);
                    Boolean commandMoveInterrupts = PlayerData.getBooleanData(playerId, PlayerDataStrings.COMMAND_MOVE_INTERRUPTS);
                    SpellCastControl castControl = super.spatial.getControl(SpellCastControl.class);
                    if (castControl.isChanneling() || commandMoveInterrupts != null && commandMoveInterrupts) {
                        super.spatial.getControl(SpellCastControl.class).safeInterrupt();
                    }
                }
                super.spatial.getControl(CharacterPhysicsControl.class).setViewDirection(newWalkDirection);
            }
        }
    }

    private void saveDirection(int right, int down) {
        this.previousRight = right;
        this.previousDown = down;
    }

    public void restoreWalking() {
        Vector3f newWalkDirection = new Vector3f(this.previousRight, 0f, this.previousDown);
        Float speedMovement = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
        newWalkDirection.normalizeLocal().multLocal(speedMovement);
        if (!newWalkDirection.equals(Vector3f.ZERO)) {
            super.spatial.getControl(CharacterPhysicsControl.class).setViewDirection(newWalkDirection);
        }
        super.spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(newWalkDirection);
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        UserInputControl control = new UserInputControl();
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
}