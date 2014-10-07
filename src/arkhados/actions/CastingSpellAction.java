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
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class CastingSpellAction extends EntityAction {

    private float delay;
    private final Spell spell;
    private Vector3f movementDirection = null;
    private boolean followedByAnotherAnimation = false;
    private InfluenceInterfaceControl influenceInterface;

    public CastingSpellAction(Spell spell) {
        this.spell = spell;
        delay = spell.getCastTime();
    }

    public CastingSpellAction(Spell spell, boolean followedByAnother) {
        this(spell);
        this.followedByAnotherAnimation = followedByAnother;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        influenceInterface = spatial.getControl(InfluenceInterfaceControl.class);        
    }
        
    @Override
    public boolean update(float tpf) {
        // TODO: Refactor and / or comment logic
        
        delay -= tpf;
        
        if (delay <= 0f) { // Go inside when casting is over
            if (!spell.canMoveWhileCasting() && !followedByAnotherAnimation) {
                spatial.getControl(UserInputControl.class).restoreWalking();
            }

            if (!followedByAnotherAnimation) {
                spatial.getControl(SpellCastControl.class).setCasting(false);
            }
            return false;
        }
        spatial.getControl(SpellCastControl.class).setCasting(true);

        if (!spell.canMoveWhileCasting() && !influenceInterface.isAbleToCastWhileMoving()) {
            spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
        } else {
            if (spell.moveTowardsTarget()) {
                CharacterPhysicsControl physics = spatial.getControl(CharacterPhysicsControl.class);
                if (!physics.getWalkDirection().equals(Vector3f.ZERO)) {
                    if (movementDirection == null) {
                        Float speedMovement = spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
                        movementDirection = physics.calculateTargetDirection();
                        movementDirection.normalizeLocal().multLocal(speedMovement);
                    }
                    physics.setWalkDirection(movementDirection);
                }
            }
        }
        return true;
    }
    
    public Spell getSpell() {
        return spell;
    }
}