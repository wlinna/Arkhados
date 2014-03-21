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

import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.controls.UserInputControl;
import arkhados.spell.Spell;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;

/**
 *
 * @author william
 */
public class CastingSpellAction extends EntityAction {

    private float delay;
    private final Spell spell;
    private Vector3f movementDirection = null;
    private boolean followedByAnotherAnimation = false;

    public CastingSpellAction(final Spell spell) {
        this.spell = spell;
        this.delay = spell.getCastTime();
    }

    public CastingSpellAction(final Spell spell, boolean followedByAnother) {
        this(spell);
        this.followedByAnotherAnimation = followedByAnother;
    }

    @Override
    public boolean update(float tpf) {
        this.delay -= tpf;
        if (this.delay <= 0f) {
            if (!this.spell.canMoveWhileCasting() && !this.followedByAnotherAnimation) {
                super.spatial.getControl(UserInputControl.class).restoreWalking();
            }

            if (!this.followedByAnotherAnimation) {
                super.spatial.getControl(SpellCastControl.class).setCasting(false);
            }
            return false;
        }
        super.spatial.getControl(SpellCastControl.class).setCasting(true);

        if (!this.spell.canMoveWhileCasting()) {
            super.spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
        } else {
            if (this.spell.moveTowardsTarget()) {
                final CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
                if (!physics.getWalkDirection().equals(Vector3f.ZERO)) {
                    if (this.movementDirection == null) {
                        final Float speedMovement = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
                        this.movementDirection = physics.calculateTargetDirection();
                        this.movementDirection.normalizeLocal().multLocal(speedMovement);
                    }
                    physics.setWalkDirection(this.movementDirection);
                }
            }
        }
        return true;
    }
    
    public Spell getSpell() {
        return this.spell;
    }
}