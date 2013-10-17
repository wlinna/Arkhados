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
import arkhados.spell.Spell;
import com.jme3.math.Vector3f;

/**
 *
 * @author william
 */
public class CastingSpellAction extends EntityAction {

    private float delay;
    private final Spell spell;

    public CastingSpellAction(final Spell spell) {
        this.spell = spell;
        this.delay = spell.getCastTime();
    }

    @Override
    public boolean update(float tpf) {
        this.delay -= tpf;
        if (this.delay <= 0f) {
            if (!this.spell.canMoveWhileCasting()) {
                super.spatial.getControl(CharacterPhysicsControl.class).restoreWalking();
            }
            return false;
        }
        if (!this.spell.canMoveWhileCasting()) {
            super.spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
        }

        return true;
    }

    public Spell getSpell() {
        return this.spell;
    }
}
