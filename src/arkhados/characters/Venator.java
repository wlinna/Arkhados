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
import arkhados.effects.RandomChoiceEffect;
import arkhados.effects.SimpleSoundEffect;
import arkhados.effects.WorldEffect;
import arkhados.spell.Spell;
import arkhados.spell.spells.venator.FeralScream;
import arkhados.spell.spells.venator.Rend;
import arkhados.util.AnimationData;
import arkhados.util.InputMapping;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Venator extends AbstractNodeBuilder {

    public static final int ANIM_LAND = 0;
    public static final int ANIM_SWIPE_UP = 1;
    public static final int ANIM_SWIPE_RIGHT = 2;
    public static final int ANIM_SWIPE_LEFT = 3;
    public static final int ACTION_FERALSCREAM = 4;
    public static final int ACTION_REND_HIT = 5;
    private final WorldEffect rendEffect;
    private final WorldEffect feralScreamEffect = new FeralScream.Effect();

    public Venator() {
        setEffectBox(new EffectBox());
        RandomChoiceEffect randomChoiceEffect = new RandomChoiceEffect();
        randomChoiceEffect.add(
                new SimpleSoundEffect("Effects/Sound/Rend1.wav"));
        randomChoiceEffect.add(
                new SimpleSoundEffect("Effects/Sound/Rend2.wav"));
        randomChoiceEffect.add(
                new SimpleSoundEffect("Effects/Sound/Rend3.wav"));
        rendEffect = randomChoiceEffect;
    }

    @Override
    public Node build(BuildParameters params) {
        Node entity = (Node) assets.loadModel("Models/Warwolf.j3o");
        float movementSpeed = 37f;
        entity.setUserData(UserData.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserData.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserData.SPEED_ROTATION, 0f);
        float radius = 4f;
        entity.setUserData(UserData.RADIUS, radius);
        float health = 1900f;
        entity.setUserData(UserData.HEALTH_MAX, health);
        entity.setUserData(UserData.HEALTH_CURRENT, health);
        if (params.age < 0f) {
            entity.setUserData(UserData.HEALTH_CURRENT, 0f);
        }
        entity.setUserData(UserData.DAMAGE_FACTOR, 1f);

        float lifesteal = 0.3f;
        entity.setUserData(UserData.LIFE_STEAL_BASE, lifesteal);
        entity.setUserData(UserData.LIFE_STEAL, lifesteal);

        for (Spatial childToScale : entity.getChildren()) {
            childToScale.scale(3f);
        }

        entity.addControl(new CCharacterPhysics(radius, 22f, 80f));
        entity.getControl(CCharacterPhysics.class).setPhysicsDamping(.2f);
        entity.addControl(new CCharacterMovement());
        entity.addControl(new CActionQueue());

        CSpellCast spellCastControl = new CSpellCast();
        entity.addControl(spellCastControl);
        Rend rend = (Rend) Spell.getSpell("Rend");
        Spell dagger = Spell.getSpell("Damaging Dagger");
        Spell numb = Spell.getSpell("Numbing Dagger");
        Spell leap = Spell.getSpell("Leap");
        Spell scream = Spell.getSpell("Feral Scream");
        Spell deepWounds = Spell.getSpell("Deep Wounds");
        Spell survivalInstinct = Spell.getSpell("Survival Instinct");
        Spell bloodFrenzy = Spell.getSpell("Blood Frenzy");

        int M2Id = InputMapping.getId(InputMapping.M2);
        int RId = InputMapping.getId(InputMapping.R);

        spellCastControl.putSpell(rend,
                InputMapping.getId(InputMapping.M1));
        spellCastControl.putSpell(dagger, M2Id);
        spellCastControl.putSpell(numb, -M2Id);
        spellCastControl.putSpell(leap,
                InputMapping.getId(InputMapping.SPACE));
        spellCastControl.putSpell(scream,
                InputMapping.getId(InputMapping.Q));
        spellCastControl.putSpell(deepWounds,
                InputMapping.getId(InputMapping.E));
        spellCastControl.putSpell(survivalInstinct, RId);
        spellCastControl.putSpell(bloodFrenzy, -RId);

        spellCastControl.putSecondaryMapping(InputMapping.SEC1, -M2Id);
        spellCastControl.putSecondaryMapping(InputMapping.SEC2, -RId);

        AnimControl animControl = entity.getControl(AnimControl.class);
        CCharacterAnimation characterAnimControl
                = new CCharacterAnimation(animControl);

        AnimationData deathAnim
                = new AnimationData("Die-1", 1f, LoopMode.DontLoop);
        AnimationData walkAnim
                = new AnimationData("Run", 0.8f, LoopMode.DontLoop);
        characterAnimControl.setDeathAnimation(deathAnim);
        characterAnimControl.setWalkAnimation(walkAnim);

        entity.addControl(characterAnimControl);

        float swipeSpeed = AnimationData.calculateSpeed(
                characterAnimControl.getAnimControl(), "Swipe-Left", 2f / 5f,
                rend.getCastTime());
        float throwSpeed = AnimationData.calculateSpeed(
                characterAnimControl.getAnimControl(), "Throw", 4f / 9f,
                dagger.getCastTime());
        float jumpSpeed = AnimationData.calculateSpeed(
                characterAnimControl.getAnimControl(), "Jump", 9f / 17f,
                leap.getCastTime());
        float roarSpeed = AnimationData.calculateSpeed(
                characterAnimControl.getAnimControl(), "Roar", 4f / 9f,
                scream.getCastTime());
        float chargeSpeed = AnimationData.calculateSpeed(
                characterAnimControl.getAnimControl(), "Charge", 2f / 5f,
                deepWounds.getCastTime());

        AnimationData swipeLeftAnim
                = new AnimationData("Swipe-Left", swipeSpeed, LoopMode.DontLoop);
        AnimationData throwAnim
                = new AnimationData("Throw", throwSpeed, LoopMode.DontLoop);
        AnimationData jumpAnim
                = new AnimationData("Jump", jumpSpeed, LoopMode.DontLoop);
        AnimationData roarAnim
                = new AnimationData("Roar", roarSpeed, LoopMode.DontLoop);
        AnimationData chargeAnim
                = new AnimationData("Charge", chargeSpeed, LoopMode.DontLoop);

        characterAnimControl.addSpellAnimation("Rend", swipeLeftAnim);
        characterAnimControl.addSpellAnimation("Damaging Dagger", throwAnim);
        characterAnimControl.addSpellAnimation("Numbing Dagger", throwAnim);
        characterAnimControl.addSpellAnimation("Leap", jumpAnim);
        characterAnimControl.addSpellAnimation("Feral Scream", roarAnim);
        characterAnimControl.addSpellAnimation("Deep Wounds", chargeAnim);
        characterAnimControl.addSpellAnimation("Survival Instinct", null);
        characterAnimControl.addSpellAnimation("Blood Frenzy", null);

        AnimationData landAnim
                = new AnimationData("Land", 1f, LoopMode.DontLoop);
        characterAnimControl.addActionAnimation(landAnim);

        float swipeUpSpeed = AnimationData.calculateSpeed(
                characterAnimControl.getAnimControl(), "Swipe-Up", 3f / 5f,
                0.2f);
        AnimationData swipeUpAnim
                = new AnimationData("Swipe-Up", swipeUpSpeed, LoopMode.DontLoop);
        characterAnimControl.addActionAnimation(swipeUpAnim);

        AnimationData swipeRightAnim = new AnimationData("Swipe-Right",
                swipeSpeed, LoopMode.DontLoop);
        characterAnimControl.addActionAnimation(swipeRightAnim);

        characterAnimControl.addActionAnimation(swipeLeftAnim);

        entity.addControl(new CInfluenceInterface());
        entity.addControl(new CCharacterDamage());
        entity.addControl(new CCharacterHeal());
        entity.addControl(new CCharacterSync());

        if (world.isClient()) {
            CCharacterSound soundControl = new CCharacterSound();
            soundControl.setSufferSound("Effects/Sound/VenatorPain.wav");
            soundControl.setDeathSound("Effects/Sound/VenatorDeath.wav");
            soundControl.addCastSound(rend.getId(), rendEffect);
            entity.addControl(soundControl);
            entity.addControl(new CCharacterBuff());
            entity.addControl(new CCharacterHud());

            entity.addControl(new CSyncInterpolation());
            entity.getControl(CInfluenceInterface.class)
                    .setIsServer(false);

            CActionPlayer actionPlayer = new CActionPlayer();
            actionPlayer.putEffect(ACTION_FERALSCREAM, feralScreamEffect);
            actionPlayer.putEffect(ACTION_REND_HIT, rend.castEffect);
            entity.addControl(actionPlayer);
        } else {
            CResting restingControl = new CResting();
            entity.addControl(restingControl);
        }

        return entity;
    }
}
