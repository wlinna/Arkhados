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

import arkhados.util.UserData;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class CCharacterMovement extends AbstractControl {

    private boolean speedConstant = false;
    private CCharacterPhysics cPhysics;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            cPhysics = spatial.getControl(CCharacterPhysics.class);
        }

    }

    public void setWalkDirection(Vector3f direction) {
        CInfluenceInterface cInfluence
                = spatial.getControl(CInfluenceInterface.class);

        if (cPhysics.isMotionControlled() || !cInfluence.canMove()) {
            return;
        } else if (!cPhysics.getDictatedDirection().equals(Vector3f.ZERO)) {
            return;
        }

        float speed = spatial.getUserData(UserData.SPEED_MOVEMENT);
        Vector3f scaledDirection = direction.normalize().multLocal(speed);

        cPhysics.setWalkDirection(scaledDirection);
    }

    public void recalculateWalkVelocity() {
        if (isSpeedConstant()) {
            return;
        }

        float speed = spatial.getUserData(UserData.SPEED_MOVEMENT);

        Vector3f scaledDirection
                = getWalkDirection().normalize().multLocal(speed);

        cPhysics.setWalkDirection(scaledDirection);
    }

    public void setWalkVelocity(Vector3f v) {
    }

    public void stop() {
        spatial.getControl(CCharacterPhysics.class)
                .setWalkDirection(Vector3f.ZERO);
    }

    public void setSpeedToBase() {
        if (!isSpeedConstant()) {
            float msBase
                    = spatial.getUserData(UserData.SPEED_MOVEMENT_BASE);
            spatial.setUserData(UserData.SPEED_MOVEMENT, msBase);
        }
    }

    public void updateMovement(float tpf) {

        CInfluenceInterface cInfluence
                = spatial.getControl(CInfluenceInterface.class);
        CSpellCast cSpell
                = spatial.getControl(CSpellCast.class);

        if (!cPhysics.getDictatedDirection().equals(Vector3f.ZERO)
                || cPhysics.isMotionControlled() || cInfluence.isDead()) {
            return;
        }

        CUserInput cInput = spatial.getControl(CUserInput.class);

        if (cInfluence.canMove() && (cInfluence.isAbleToCastWhileMoving()
                || (!cSpell.isCasting() && !cSpell.isChanneling()))) {
            if (cInfluence.canControlMovement()) {
                Vector3f direction = cInput.giveInputDirection();
                if (!direction.equals(Vector3f.ZERO) && cPhysics.isEnabled()) {
                    spatial.getControl(CCharacterPhysics.class)
                            .setViewDirection(direction);
                }

                setWalkDirection(direction);

            } else {
                if (!isSpeedConstant() && cPhysics
                        .getDictatedDirection().equals(Vector3f.ZERO)) {
                    recalculateWalkVelocity();
                }
            }
        }

        if (cSpell.isCasting() || cSpell.isChanneling()) {
            Vector3f targetDir = cInput.giveTargetDirection();
            spatial.getControl(CCharacterPhysics.class)
                    .setViewDirection(targetDir);
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public boolean isSpeedConstant() {
        return speedConstant;
    }

    public void setSpeedConstant(boolean speedConstant) {
        this.speedConstant = speedConstant;
    }

    public Vector3f getWalkDirection() {
        return cPhysics.getWalkDirection();
    }
}
