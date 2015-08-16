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

import arkhados.Globals;
import arkhados.PlayerData;
import arkhados.ServerInputState;
import arkhados.util.UserData;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CUserInput extends AbstractControl {

    private ServerInputState inputState;

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void updateDirection() {
        CInfluenceInterface influenceInterface =
                spatial.getControl(CInfluenceInterface.class);
        CCharacterPhysics physics =
                spatial.getControl(CCharacterPhysics.class);

        if (!influenceInterface.canControlMovement()
                || !influenceInterface.canMove()
                || physics.isMotionControlled()
                || influenceInterface.isDead()
                || !Globals.worldRunning) {
            return;
        }

        if (inputState.previousRight == 0 && inputState.previousDown == 0) {
            return;
        }

        if (!influenceInterface.isAbleToCastWhileMoving()) {
            int playerId = spatial.getUserData(UserData.PLAYER_ID);

            Boolean commandMoveInterruptsBoolean =
                    PlayerData.getBooleanData(playerId,
                    PlayerData.COMMAND_MOVE_INTERRUPTS);
            boolean commandMoveInterrupts = commandMoveInterruptsBoolean != null
                    && commandMoveInterruptsBoolean;

            CSpellCast castControl =
                    spatial.getControl(CSpellCast.class);
            if (castControl.isChanneling() || commandMoveInterrupts) {
                castControl.safeInterrupt();
            }
        }
    }

    public Vector3f giveInputDirection() {
        return new Vector3f(inputState.previousRight,
                0, inputState.previousDown);
    }

    public ServerInputState getInputState() {
        return inputState;
    }

    public void setInputState(ServerInputState inputState) {
        this.inputState = inputState;
    }
}