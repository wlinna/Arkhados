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
import arkhados.controls.CharacterBuffControl;
import arkhados.controls.CharacterHudControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.CharacterSyncControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.controls.SyncInterpolationControl;
import arkhados.spell.Spell;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.AnimationData;
import arkhados.util.InputMappingStrings;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Node;

/**
 * Creates entity with EmberMage's features.
 *
 * @author william
 */
public class EmberMage extends AbstractNodeBuilder {

    private ClientHudManager clientHudManager;

    public EmberMage(ClientHudManager clientHudManager) {
        this.clientHudManager = clientHudManager;
    }

    @Override
    public Node build() {
        Node entity = (Node) AbstractNodeBuilder.assetManager.loadModel("Models/Mage.j3o");
        final float movementSpeed = 35f;
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_ROTATION, 0.0f);
        final float radius = 5.0f;
        entity.setUserData(UserDataStrings.RADIUS, radius);
        final float health = 1700f;
        entity.setUserData(UserDataStrings.HEALTH_MAX, health);
        entity.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        entity.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        entity.setUserData(UserDataStrings.LIFE_STEAL, 0f);

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
        spellCastControl.putSpell(Spell.getSpell("Fireball"), InputMappingStrings.M1);
        spellCastControl.putSpell(Spell.getSpell("Magma Bash"), InputMappingStrings.M2);
        spellCastControl.putSpell(Spell.getSpell("Ember Circle"), InputMappingStrings.Q);
        spellCastControl.putSpell(Spell.getSpell("Meteor"), InputMappingStrings.E);
        spellCastControl.putSpell(Spell.getSpell("Purifying Flame"), InputMappingStrings.R);
        spellCastControl.putSpell(Spell.getSpell("Firewalk"), InputMappingStrings.SPACE);
        spellCastControl.putSpell(Spell.getSpell("Ignite"), null);

        /**
         * Map Spell names to casting animation's name. In this case all spells
         * use same animation.
         */
        CharacterAnimationControl animControl = new CharacterAnimationControl();
        final AnimationData deathAnim = new AnimationData("Die", 1f, LoopMode.DontLoop);
        final AnimationData walkAnim = new AnimationData("Walk", 1f, LoopMode.DontLoop);

        animControl.setDeathAnimation(deathAnim);
        animControl.setWalkAnimation(walkAnim);
        entity.addControl(animControl);

        final AnimationData animationData = new AnimationData("Idle", 1f, LoopMode.Loop);
        animControl.addActionAnimation("Cast", animationData);

        animControl.addSpellAnimation("Fireball", animationData);
        animControl.addSpellAnimation("Magma Bash", animationData);
        animControl.addSpellAnimation("Ember Circle", animationData);
        animControl.addSpellAnimation("Meteor", animationData);
        animControl.addSpellAnimation("Purifying Flame", null);
        animControl.addSpellAnimation("Firewalk", animationData);

        entity.addControl(new InfluenceInterfaceControl());

        entity.addControl(new CharacterSyncControl());

        if (worldManager.isClient()) {
            entity.addControl(new CharacterBuffControl());
            entity.addControl(new CharacterHudControl());

            this.clientHudManager.addCharacter(entity);
            entity.addControl(new SyncInterpolationControl());
            entity.getControl(InfluenceInterfaceControl.class).setIsServer(false);
        }

        return entity;
    }
}