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
package arkhados.spell.spells.shadowmancer;

import arkhados.actions.EntityAction;
import arkhados.actions.cast.ACastBuff;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.BuffTypeIds;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Shadow extends Spell {

    {
        iconName = "MineralArmor.png";
    }

    public Shadow(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 13f;
        final float range = 100f;
        final float castTime = 0f;

        final Shadow spell = new Shadow("Shadow", cooldown,
                range, castTime);
        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                ACastBuff action = new ACastBuff(spell, range);
                AbstractBuffBuilder armor =
                        new ShadowBuff.MyBuilder(3f);
                action.addBuff(armor);

                return action;
            }
        };

        spell.nodeBuilder = null;

        return spell;
    }
}
class ShadowBuff extends AbstractBuff {

    public ShadowBuff(float duration) {
        super(duration);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        super.attachToCharacter(targetInterface);

        Spatial spatial = targetInterface.getSpatial();

        CCharacterPhysics physics = spatial.getControl(CCharacterPhysics.class);
        physics.makeEthereal();

        Node root = spatial.getParent().getParent();

        Node fakeNode = (Node) root.getChild("fake-world-root");

        spatial.removeFromParent();
        fakeNode.attachChild(spatial);
    }

    @Override
    public void destroy() {
        super.destroy();
        
        Spatial spatial = targetInterface.getSpatial();
        CCharacterPhysics physics = spatial.getControl(CCharacterPhysics.class);
        physics.defaultCollisionGroups();
        
        Node root = spatial.getParent().getParent();
        Node worldRoot = (Node) root.getChild("world-root");
        
        spatial.removeFromParent();
        worldRoot.attachChild(spatial);
        
    }        

    static class MyBuilder extends AbstractBuffBuilder {

        MyBuilder(float duration) {
            super(duration);
            setTypeId(BuffTypeIds.SHADOW);
        }

        @Override
        public AbstractBuff build() {
            return set(new ShadowBuff(duration));
        }
    }
}