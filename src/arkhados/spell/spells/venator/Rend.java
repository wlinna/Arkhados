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
package arkhados.spell.spells.venator;

import arkhados.actions.ACastingSpell;
import arkhados.actions.EntityAction;
import arkhados.actions.cast.AMeleeAttack;
import arkhados.characters.Venator;
import arkhados.controls.CActionQueue;
import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Rend extends Spell {

    {
        iconName = "rend.png";
        multipart = true;
    }

    public Rend(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.5f;
        final float range = 13.5f;
        final float castTime = 0.17f;

        final Rend spell = new Rend("Rend", cooldown, range, castTime);
        spell.setCanMoveWhileCasting(true);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ADoubleMeleeAttack action
                    = new ADoubleMeleeAttack(spell);
            action.setTypeId(Venator.ANIM_SWIPE_LEFT);
            return action;
        };

        spell.nodeBuilder = null;
        return spell;
    }
}

class ADoubleMeleeAttack extends EntityAction {

    private static final class AEnd extends EntityAction {

        @Override
        public boolean update(float tpf) {
            // HACK: This should happen automatically
            spatial.getControl(CSpellCast.class).setCasting(false);
                // TODO: MAKE SURE it's okay to disable this
            // spatial.getControl(UserInputControl.class).restoreWalking();
            return false;
        }
    }

    private final Spell spell;

    public ADoubleMeleeAttack(Spell spell) {
        this.spell = spell;
    }

    @Override
    public boolean update(float tpf) {
        // TODO: Make an attack start with different animation than previous one
        float range = spell.getRange();
        CActionQueue queue = spatial.getControl(CActionQueue.class);
        final AMeleeAttack action1 = new AMeleeAttack(75f, range);
        ACastingSpell action2Anim = new ACastingSpell(spell, true);
        AMeleeAttack action2 = new AMeleeAttack(85f, range);

        // action1 already has the default spell casting animation
        action2Anim.setTypeId(Venator.ANIM_SWIPE_RIGHT);
        queue.enqueueAction(action1);
        queue.enqueueAction(action2Anim);
        queue.enqueueAction(action2);

        queue.enqueueAction(new AEnd());

        return false;
    }
}
