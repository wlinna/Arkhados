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
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class Venator extends NodeBuilder {

    @Override
    public Node build() {
        Node entity = (Node) NodeBuilder.assetManager.loadModel("Models/Warwolf.j3o");
        final float movementSpeed = 37f;
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_ROTATION, 0f);
        final float radius = 4f;
        entity.setUserData(UserDataStrings.RADIUS, radius);
        final float health = 2100f;
        entity.setUserData(UserDataStrings.HEALTH_MAX, health);
        entity.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        entity.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);

        entity.scale(3f);

        entity.addControl(new CharacterPhysicsControl(radius, 22f, 100f));

        entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(0.2f);
        entity.addControl(new ActionQueueControl());

        SpellCastControl spellCastControl = new SpellCastControl(this.worldManager);
        entity.addControl(spellCastControl);
        spellCastControl.putSpell(Spell.getSpells().get("Rend"), InputMappingStrings.M1);
        spellCastControl.putSpell(Spell.getSpells().get("Damaging Dagger"), InputMappingStrings.M2);
        spellCastControl.putSpell(Spell.getSpells().get("Leap"), InputMappingStrings.SPACE);
        spellCastControl.putSpell(Spell.getSpells().get("Deep Wounds"), InputMappingStrings.E);
        spellCastControl.putSpell(Spell.getSpells().get("Survival Instinct"), InputMappingStrings.R);

        CharacterAnimationControl animControl = new CharacterAnimationControl();
        animControl.setDeathAnimation("Die-1");
        animControl.setWalkAnimation("Run");
        animControl.addSpellAnimation("Rend", "Swipe-Left");
        animControl.addSpellAnimation("Damaging Dagger", "Throw");
        animControl.addSpellAnimation("Leap", "Jump");
        animControl.addSpellAnimation("Deep Wounds", "Charge");
        animControl.addSpellAnimation("Survival Instinct", "");
        entity.addControl(animControl);

        entity.addControl(new InfluenceInterfaceControl());

        return entity;
    }
}
