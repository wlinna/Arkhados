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
package arkhados.characters;

import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterAnimationControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.spell.Spell;
import arkhados.util.InputMappingStrings;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.scene.Node;

/**
 * Creates entity with EmberMage's features.
 *
 * @author william
 */
public class EmberMage extends NodeBuilder {

    @Override
    public  Node build() {
        Node entity = (Node) NodeBuilder.assetManager.loadModel("Models/Mage.j3o");
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT, 35.0f);
        entity.setUserData(UserDataStrings.SPEED_ROTATION, 0.0f);
        float radius = 5.0f;
        entity.setUserData(UserDataStrings.RADIUS, radius);
        entity.setUserData(UserDataStrings.HEALTH_CURRENT, 1700.0f);

        entity.addControl(new CharacterPhysicsControl(radius, 20.0f, 75.0f));

        /**
         * By setting physics damping to low value, we can effectively apply
         * impulses on it.
         */
        entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(0.2f);
        entity.addControl(new ActionQueueControl());

        /**
         * To add spells to entity, create SpellCastControl and call its
         * putSpell-method with name of the spell as argument.
         */
        SpellCastControl spellCastControl = new SpellCastControl(this.worldManager);
        entity.addControl(spellCastControl);
        spellCastControl.putSpell(Spell.getSpells().get("Fireball"), InputMappingStrings.M1);
        spellCastControl.putSpell(Spell.getSpells().get("Magma Bash"), InputMappingStrings.M2);
        spellCastControl.putSpell(Spell.getSpells().get("Ember Circle"), InputMappingStrings.Q);

        /**
         * Map Spell names to casting animation's name. In this case all spells
         * use same animation.
         */
        CharacterAnimationControl animControl = new CharacterAnimationControl();
        animControl.setDeathAnimation("Die");
        animControl.setWalkAnimation("Walk");
        entity.addControl(animControl);
        animControl.addSpellAnimation("Fireball", "Idle");
        animControl.addSpellAnimation("Magma Bash", "Idle");
        animControl.addSpellAnimation("Ember Circle", "Idle");

        entity.addControl(new InfluenceInterfaceControl());

        return entity;

    }
}
