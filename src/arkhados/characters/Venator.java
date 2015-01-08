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
import arkhados.controls.CharacterSoundControl;
import arkhados.controls.CharacterSyncControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.RestingControl;
import arkhados.controls.SpellCastControl;
import arkhados.controls.SyncInterpolationControl;
import arkhados.effects.EffectBox;
import arkhados.effects.RandomChoiceEffect;
import arkhados.effects.SimpleSoundEffect;
import arkhados.effects.WorldEffect;
import arkhados.spell.Spell;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.AnimationData;
import arkhados.util.InputMappingStrings;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class Venator extends AbstractNodeBuilder {

    public static final int ANIM_LAND = 0;
    public static final int ANIM_SWIPE_UP = 1;
    public static final int ANIM_SWIPE_RIGHT = 2;
    public static final int ANIM_SWIPE_LEFT = 3;
    public static final int ACTION_FERALSCREAM = 4;
    private ClientHudManager clientHudManager;
    private WorldEffect rendEffect;

    public Venator(ClientHudManager clientHudManager) {
        this.clientHudManager = clientHudManager;
        setEffectBox(new EffectBox());
        getEffectBox().addActionEffect(ACTION_FERALSCREAM,
                new SimpleSoundEffect("Effects/Sound/FeralScream.wav"));
                
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
    public Node build(Object irrelevant) {
        Node entity = (Node) assetManager.loadModel("Models/Warwolf.j3o");
        float movementSpeed = 37f;
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_ROTATION, 0f);
        float radius = 4f;
        entity.setUserData(UserDataStrings.RADIUS, radius);
        float health = 2100f;
        entity.setUserData(UserDataStrings.HEALTH_MAX, health);
        entity.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        entity.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);

        // Note: This works now but later life steal is set by buffs.
        entity.setUserData(UserDataStrings.LIFE_STEAL, 0.33f);

        for (Spatial childToScale : entity.getChildren()) {
            childToScale.scale(3f);
        }

        entity.addControl(new CharacterPhysicsControl(radius, 22f, 100f));

        entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(.2f);
        entity.addControl(new ActionQueueControl());

        SpellCastControl spellCastControl = new SpellCastControl();
        entity.addControl(spellCastControl);
        Spell rend = Spell.getSpell("Rend");
        Spell dagger = Spell.getSpell("Damaging Dagger");
        Spell leap = Spell.getSpell("Leap");
        Spell scream = Spell.getSpell("Feral Scream");
        Spell deepWounds = Spell.getSpell("Deep Wounds");
        Spell survivalInstinct = Spell.getSpell("Survival Instinct");

        spellCastControl.putSpell(rend,
                InputMappingStrings.getId(InputMappingStrings.M1));
        spellCastControl.putSpell(dagger,
                InputMappingStrings.getId(InputMappingStrings.M2));
        spellCastControl.putSpell(leap,
                InputMappingStrings.getId(InputMappingStrings.SPACE));
        spellCastControl.putSpell(scream,
                InputMappingStrings.getId(InputMappingStrings.Q));
        spellCastControl.putSpell(deepWounds,
                InputMappingStrings.getId(InputMappingStrings.E));
        spellCastControl.putSpell(survivalInstinct,
                InputMappingStrings.getId(InputMappingStrings.R));

        AnimControl animControl = entity.getControl(AnimControl.class);
        CharacterAnimationControl characterAnimControl =
                new CharacterAnimationControl(animControl);

        AnimationData deathAnim = 
                new AnimationData("Die-1", 1f, LoopMode.DontLoop);
        AnimationData walkAnim =
                new AnimationData("Run", 0.8f, LoopMode.DontLoop);
        characterAnimControl.setDeathAnimation(deathAnim);
        characterAnimControl.setWalkAnimation(walkAnim);

        entity.addControl(characterAnimControl);

        float swipeSpeed = AnimationData.calculateSpeedForAnimation(
                characterAnimControl.getAnimControl(), "Swipe-Left", 2f / 5f,
                rend.getCastTime());
        float throwSpeed = AnimationData.calculateSpeedForAnimation(
                characterAnimControl.getAnimControl(), "Throw", 4f / 9f,
                dagger.getCastTime());
        float jumpSpeed = AnimationData.calculateSpeedForAnimation(
                characterAnimControl.getAnimControl(), "Jump", 9f / 17f,
                leap.getCastTime());
        float roarSpeed = AnimationData.calculateSpeedForAnimation(
                characterAnimControl.getAnimControl(), "Roar", 4f / 9f,
                scream.getCastTime());
        float chargeSpeed = AnimationData.calculateSpeedForAnimation(
                characterAnimControl.getAnimControl(), "Charge", 2f / 5f,
                deepWounds.getCastTime());

        AnimationData swipeLeftAnim =
                new AnimationData("Swipe-Left", swipeSpeed, LoopMode.DontLoop);
        AnimationData throwAnim =
                new AnimationData("Throw", throwSpeed, LoopMode.DontLoop);
        AnimationData jumpAnim =
                new AnimationData("Jump", jumpSpeed, LoopMode.DontLoop);
        AnimationData roarAnim =
                new AnimationData("Roar", roarSpeed, LoopMode.DontLoop);
        AnimationData chargeAnim =
                new AnimationData("Charge", chargeSpeed, LoopMode.DontLoop);

        characterAnimControl.addSpellAnimation("Rend", swipeLeftAnim);
        characterAnimControl.addSpellAnimation("Damaging Dagger", throwAnim);
        characterAnimControl.addSpellAnimation("Leap", jumpAnim);
        characterAnimControl.addSpellAnimation("Feral Scream", roarAnim);
        characterAnimControl.addSpellAnimation("Deep Wounds", chargeAnim);
        characterAnimControl.addSpellAnimation("Survival Instinct", null);

        AnimationData landAnim =
                new AnimationData("Land", 1f, LoopMode.DontLoop);
        characterAnimControl.addActionAnimation(landAnim);

        float swipeUpSpeed = AnimationData.calculateSpeedForAnimation(
                characterAnimControl.getAnimControl(), "Swipe-Up", 3f / 5f,
                0.2f);
        AnimationData swipeUpAnim =
                new AnimationData("Swipe-Up", swipeUpSpeed, LoopMode.DontLoop);
        characterAnimControl.addActionAnimation(swipeUpAnim);

        AnimationData swipeRightAnim = new AnimationData("Swipe-Right",
                swipeSpeed, LoopMode.DontLoop);
        characterAnimControl.addActionAnimation(swipeRightAnim);

        characterAnimControl.addActionAnimation(swipeLeftAnim);

        entity.addControl(new InfluenceInterfaceControl());
        entity.addControl(new CharacterSyncControl());

        if (worldManager.isClient()) {
            CharacterSoundControl soundControl = new CharacterSoundControl();
            soundControl.setSufferSound("Effects/Sound/VenatorPain.wav");
            soundControl.setDeathSound("Effects/Sound/VenatorDeath.wav");
            soundControl.addCastSound(rend.getId(), rendEffect);
            entity.addControl(soundControl);
            entity.addControl(new CharacterBuffControl());
            entity.addControl(new CharacterHudControl());

            entity.addControl(new SyncInterpolationControl());
            entity.getControl(InfluenceInterfaceControl.class)
                    .setIsServer(false);
        } else {
            RestingControl restingControl = new RestingControl();
            entity.addControl(restingControl);
        }

        return entity;
    }
}