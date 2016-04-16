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
package arkhados.actions;

import arkhados.controls.CCharacterMovement;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class ACastingSpell extends EntityAction {

    private float delay;
    private final Spell spell;
    private Vector3f movementDirection = null;
    private boolean followedByAnotherAnimation = false;
    private CInfluenceInterface influenceInterface;
    private CSpellCast cSpellCast;

    public ACastingSpell(Spell spell) {
        this.spell = spell;
        delay = spell.getCastTime();
    }

    public ACastingSpell(Spell spell, boolean followedByAnother) {
        this(spell);
        this.followedByAnotherAnimation = followedByAnother;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        influenceInterface = spatial.getControl(CInfluenceInterface.class);
        cSpellCast = spatial.getControl(CSpellCast.class);
    }

    @Override
    public boolean update(float tpf) {
        // TODO: Refactor and / or comment logic

        delay -= tpf * cSpellCast.getCastSpeedFactor();

        if (delay <= 0f) { // Go inside when casting is over
            if (!spell.canMoveWhileCasting() && !followedByAnotherAnimation) {
                // TODO: MAKE SURE it's ok to disable this
                // spatial.getControl(UserInputControl.class).restoreWalking();
            }

            if (!followedByAnotherAnimation) {
                cSpellCast.setCasting(false);
            }
            return false;
        }
        cSpellCast.setCasting(true);
        CCharacterMovement cMovement =
                spatial.getControl(CCharacterMovement.class);

        if (!spell.canMoveWhileCasting()
                && !influenceInterface.isAbleToCastWhileMoving()) {
            cMovement.stop();
        } else {
            if (spell.moveTowardsTarget()) {
                CCharacterPhysics physics =
                        spatial.getControl(CCharacterPhysics.class);
                if (!cMovement.getWalkDirection().equals(Vector3f.ZERO)) {
                    if (movementDirection == null) {
                        movementDirection = physics.calculateTargetDirection();
                    }
                    cMovement.setWalkDirection(movementDirection);
                }
            }
        }
        return true;
    }

    public Spell getSpell() {
        return spell;
    }
}