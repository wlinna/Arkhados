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

import arkhados.controls.CActionPlayer;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterDamage;
import arkhados.controls.CCharacterHeal;
import arkhados.controls.CCharacterMovement;
import arkhados.controls.CCharacterAnimation;
import arkhados.controls.CCharacterBuff;
import arkhados.controls.CCharacterHud;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CCharacterSound;
import arkhados.controls.CCharacterSync;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CResting;
import arkhados.controls.CSpellCast;
import arkhados.controls.CSyncInterpolation;
import arkhados.effects.EffectBox;
import arkhados.effects.SimpleSoundEffect;
import arkhados.spell.Spell;
import arkhados.spell.spells.embermage.EtherealFlame;
import arkhados.util.AnimationData;
import arkhados.util.InputMappingStrings;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserDataStrings;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Node;

/**
 * Creates entity with EmberMage's features.
 *
 * @author william
 */
public class EmberMage extends AbstractNodeBuilder {

    public static final int ACTION_FIREWALK = 0;
    public static final int ACTION_ETHEREAL_FLAME = 1;

    public EmberMage() {
        setEffectBox(new EffectBox());
        getEffectBox().addActionEffect(ACTION_FIREWALK,
                new SimpleSoundEffect("Effects/Sound/Firewalk.wav"));
    }

    @Override
    public Node build(BuildParameters params) {
        Node entity = (Node) assetManager.loadModel("Models/Mage.j3o");
        float movementSpeed = 35.8f;
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_ROTATION, 0f);
        float radius = 5f;
        entity.setUserData(UserDataStrings.RADIUS, radius);
        float health = 1700f;
        entity.setUserData(UserDataStrings.HEALTH_MAX, health);

        entity.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        if (params.age < 0f) {
            entity.setUserData(UserDataStrings.HEALTH_CURRENT, 0f);
        }

        entity.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        entity.setUserData(UserDataStrings.LIFE_STEAL_BASE, 0f);
        entity.setUserData(UserDataStrings.LIFE_STEAL, 0f);

        entity.addControl(new CCharacterPhysics(radius, 20f, 75f));

        /**
         * By setting physics damping to low value, we can effectively apply
         * impulses on it.
         */
        entity.getControl(CCharacterPhysics.class).setPhysicsDamping(0.2f);
        entity.addControl(new CCharacterMovement());
        entity.addControl(new CActionQueue());

        /**
         * To add spells to entity, create SpellCastControl and call its
         * putSpell-method with name of the spell as argument.
         */
        CSpellCast spellCastControl = new CSpellCast();
        entity.addControl(spellCastControl);
        int M2Id = InputMappingStrings.getId(InputMappingStrings.M2);

        int RId = InputMappingStrings.getId(InputMappingStrings.R);

        spellCastControl.putSpell(Spell.getSpell("Fireball"),
                InputMappingStrings.getId(InputMappingStrings.M1));
        spellCastControl.putSpell(Spell.getSpell("Magma Bash"), M2Id);
        spellCastControl.putSpell(Spell.getSpell("Magma Release"), -M2Id);
        spellCastControl.putSpell(Spell.getSpell("Ember Circle"),
                InputMappingStrings.getId(InputMappingStrings.Q));
        spellCastControl.putSpell(Spell.getSpell("Meteor"),
                InputMappingStrings.getId(InputMappingStrings.E));
        spellCastControl.putSpell(Spell.getSpell("Purifying Flame"), RId);
        spellCastControl.putSpell(Spell.getSpell("Ethereal Flame"), -RId);
        spellCastControl.putSpell(Spell.getSpell("Firewalk"),
                InputMappingStrings.getId(InputMappingStrings.SPACE));
        spellCastControl.putSpell(Spell.getSpell("Ignite"), null);

        spellCastControl.putSecondaryMapping(InputMappingStrings.SEC1, -M2Id);
        spellCastControl.putSecondaryMapping(InputMappingStrings.SEC2, -RId);

        /**
         * Map Spell names to casting animation's name. In this case all spells
         * use same animation.
         */
        AnimControl animControl = entity.getControl(AnimControl.class);

        CCharacterAnimation characterAnimControl =
                new CCharacterAnimation(animControl);
        AnimationData deathAnim =
                new AnimationData("Die", 1f, LoopMode.DontLoop);
        AnimationData walkAnim =
                new AnimationData("Walk", 1f, LoopMode.DontLoop);

        characterAnimControl.setDeathAnimation(deathAnim);
        characterAnimControl.setWalkAnimation(walkAnim);
        entity.addControl(characterAnimControl);

        AnimationData idleAnim =
                new AnimationData("Idle", 1f, LoopMode.Loop);

        AnimationData hitAnim = new AnimationData("Hit", 1f, LoopMode.DontLoop);
        AnimationData attackAnim =
                new AnimationData("Attack", 1f, LoopMode.DontLoop);

        characterAnimControl.addSpellAnimation("Fireball", attackAnim);
        characterAnimControl.addSpellAnimation("Magma Bash", attackAnim);
        characterAnimControl.addSpellAnimation("Magma Release", attackAnim);
        characterAnimControl.addSpellAnimation("Ember Circle", idleAnim);
        characterAnimControl.addSpellAnimation("Meteor", idleAnim);
        characterAnimControl.addSpellAnimation("Purifying Flame", null);
        characterAnimControl.addSpellAnimation("Ethereal Flame", hitAnim);
        characterAnimControl.addSpellAnimation("Firewalk", idleAnim);

        entity.addControl(new CInfluenceInterface());
        entity.addControl(new CCharacterDamage());
        entity.addControl(new CCharacterHeal());

        entity.addControl(new CCharacterSync());

        if (worldManager.isClient()) {
            CCharacterSound soundControl = new CCharacterSound();
            soundControl.setSufferSound("Effects/Sound/EmberMagePain.wav");
            soundControl.setDeathSound("Effects/Sound/EmberMageDeath.wav");
            entity.addControl(soundControl);
            entity.addControl(new CCharacterBuff());
            entity.addControl(new CCharacterHud());

            entity.addControl(new CSyncInterpolation());
            entity.getControl(CInfluenceInterface.class).setIsServer(false);
            
            CActionPlayer actionPlayer = new CActionPlayer();
            actionPlayer.putEffect(ACTION_ETHEREAL_FLAME,
                    new EtherealFlame.Effect());
            entity.addControl(actionPlayer);
        } else {
            CResting restingControl = new CResting();
            entity.addControl(restingControl);
        }

        return entity;
    }
}