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
import arkhados.spell.Spell;
import arkhados.util.AnimationData;
import arkhados.util.InputMapping;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Creates entity with Shadowmancer's features.
 */
public class Shadowmancer extends AbstractNodeBuilder {

    public Shadowmancer() {
        setEffectBox(new EffectBox());
    }

    @Override
    public Node build(BuildParameters params) {
        Node entity = (Node) assets.loadModel("Models/Shadowmancer.j3o");
        float movementSpeed = 36.1f;
        entity.setUserData(UserData.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserData.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserData.SPEED_ROTATION, 0f);
        float radius = 4.1f;
        entity.setUserData(UserData.RADIUS, radius);
        float health = 1600f;
        entity.setUserData(UserData.HEALTH_MAX, health);

        entity.setUserData(UserData.HEALTH_CURRENT, health);
        if (params.age < 0f) {
            entity.setUserData(UserData.HEALTH_CURRENT, 0f);
        }

        entity.setUserData(UserData.DAMAGE_FACTOR, 1f);
        entity.setUserData(UserData.LIFE_STEAL_BASE, 0f);
        entity.setUserData(UserData.LIFE_STEAL, 0f);

        for (Spatial childToScale : entity.getChildren()) {
            childToScale.scale(3f);
        }

        entity.addControl(new CCharacterPhysics(radius, 17f, 55f));

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
        int M2Id = InputMapping.getId(InputMapping.M2);
        int QId = InputMapping.getId(InputMapping.Q);

        Spell orb = Spell.getSpell("Shadow Orb");
        Spell energy = Spell.getSpell("Dark Energy");
        Spell energySelf = Spell.getSpell("Dark Energy Self");
        Spell drain = Spell.getSpell("Mind Poison");
        Spell spear = Spell.getSpell("Void Spear");
        Spell shadow = Spell.getSpell("Shadow");
        Spell intoShadows = Spell.getSpell("Into the Shadows");

        spellCastControl.putSpell(orb, InputMapping.getId(InputMapping.M1));
        spellCastControl.putSpell(energy, M2Id);
        spellCastControl.putSpell(energySelf, -M2Id);
        spellCastControl.putSpell(drain, QId);
        spellCastControl.putSpell(spear, InputMapping.getId(InputMapping.E));
        spellCastControl.putSpell(shadow, InputMapping.getId(InputMapping.R));
        spellCastControl.putSpell(intoShadows,
                InputMapping.getId(InputMapping.SPACE));

        spellCastControl.putSecondaryMapping(InputMapping.SEC1, -M2Id);

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
                new AnimationData("Flee", 1f, LoopMode.DontLoop);

        characterAnimControl.setDeathAnimation(deathAnim);
        characterAnimControl.setWalkAnimation(walkAnim);
        entity.addControl(characterAnimControl);

        float orbSpeed = AnimationData.calculateSpeed(animControl,
                "Attack1", 1f, orb.getCastTime());
        float energySpeed = AnimationData.calculateSpeed(
                animControl, "Attack2", 1f, energy.getCastTime());
        float drainSpeed = AnimationData.calculateSpeed(
                animControl, "Attack2", 1f, drain.getCastTime());
        float spearSpeed = AnimationData.calculateSpeed(animControl,
                "Attack1", 1f, spear.getCastTime());
        float intoShadowsSpeed = AnimationData.calculateSpeed(
                animControl, "Dance", 0.19f, intoShadows.getCastTime());

        AnimationData orbAnim =
                new AnimationData("Attack1", orbSpeed, LoopMode.DontLoop);
        AnimationData energyAnim =
                new AnimationData("Attack2", energySpeed, LoopMode.DontLoop);
        AnimationData drainAnim =
                new AnimationData("Attack2", drainSpeed, LoopMode.DontLoop);
        AnimationData spearAnim =
                new AnimationData("Attack1", spearSpeed, LoopMode.DontLoop);        
        AnimationData intoShadowsAnim =
                new AnimationData("Dance", intoShadowsSpeed, LoopMode.DontLoop);

        characterAnimControl.addSpellAnimation("Shadow Orb", orbAnim);
        characterAnimControl.addSpellAnimation("Dark Energy", energyAnim);
        characterAnimControl.addSpellAnimation("Dark Energy Self", energyAnim);
        characterAnimControl.addSpellAnimation("Mind Poison", drainAnim);
        characterAnimControl.addSpellAnimation("Void Spear", spearAnim);
        characterAnimControl.addSpellAnimation("Shadow", null);
        characterAnimControl.addSpellAnimation("Into the Shadows", 
                intoShadowsAnim);

        entity.addControl(new CInfluenceInterface());
        entity.addControl(new CCharacterDamage());
        entity.addControl(new CCharacterHeal());
        entity.addControl(new CCharacterSync());

        if (world.isClient()) {
            CCharacterSound soundControl = new CCharacterSound();
            soundControl.addSufferSound("Effects/Sound/ShadowmancerPain1.wav");
            soundControl.addSufferSound("Effects/Sound/ShadowmancerPain2.wav");
            soundControl.addSufferSound("Effects/Sound/ShadowmancerPain3.wav");
            soundControl.addSufferSound("Effects/Sound/ShadowmancerPain4.wav");
            soundControl.setDeathSound("Effects/Sound/EmberMageDeath.wav");
            entity.addControl(soundControl);
            entity.addControl(new CCharacterBuff());
            entity.addControl(new CCharacterHud());

            entity.addControl(new CSyncInterpolation());
            entity.getControl(CInfluenceInterface.class).setIsServer(false);

            CActionPlayer actionPlayer = new CActionPlayer();
            entity.addControl(actionPlayer);
        } else {
            CResting restingControl = new CResting();
            entity.addControl(restingControl);
        }

        return entity;
    }
}