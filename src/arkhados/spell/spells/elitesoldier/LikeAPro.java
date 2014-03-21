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
package arkhados.spell.spells.elitesoldier;

import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastSelfBuffAction;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;


/**
 * Elite Soldiers buff spell that gives armor, ability to move while casting and
 * gives little bit ammo.
 * @author william
 */
public class LikeAPro extends Spell {

    {
        this.iconName = "survival_instinct.png";
    }

    public LikeAPro(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static LikeAPro create() {
        final float cooldown = 12f;
        final float range = 0f;
        final float castTime = 0f;

        final LikeAPro spell = new LikeAPro("Like a Pro", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f vec) {
                CastSelfBuffAction buffAction = new CastSelfBuffAction();
                buffAction.addBuff(new AbleToCastWhileMovingBuff(-1, 5f));
                return buffAction;
            }
        };

        spell.nodeBuilder = null;
        return spell;
    }
}

class AbleToCastWhileMovingBuff extends AbstractBuff {

    private boolean set = false;


    public AbleToCastWhileMovingBuff(long buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl influenceInterface) {
        super.attachToCharacter(influenceInterface);        
    }

    @Override
    public void update(float time) {
        super.update(time);
        if (this.set) {
            return;
        }
        this.set = true;       
    }

    @Override
    public void destroy() {
        super.destroy();
    }        
}