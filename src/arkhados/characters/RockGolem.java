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

import arkhados.EffectHandler;
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
import arkhados.effects.EarthQuakeEffect;
import arkhados.effects.EffectBox;
import arkhados.effects.TossHitEffect;
import arkhados.effects.WorldEffect;
import arkhados.spell.Spell;
import arkhados.spell.spells.rockgolem.TossValidator;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.AnimationData;
import arkhados.util.BuildParameters;
import arkhados.util.InputMapping;
import arkhados.util.UserData;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

public class RockGolem extends AbstractNodeBuilder {

    public static final int ACTION_EARTHQUAKE = 0;
    public static int WORLDEFFECT_TOSS_HIT;

    public RockGolem() {
        setEffectBox(new EffectBox());
        WorldEffect earthQuake =
                new EarthQuakeEffect();

        getEffectBox().addActionEffect(ACTION_EARTHQUAKE, earthQuake);

        TossHitEffect tossHitEffect = new TossHitEffect();
        WORLDEFFECT_TOSS_HIT = EffectHandler.addWorldEffect(tossHitEffect);
    }

    @Override
    public Node build(BuildParameters params) {
        Node entity = (Node) assetManager.loadModel("Models/RockGolem.j3o");
        float movementSpeed = 34.7f;
        entity.setUserData(UserData.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserData.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserData.SPEED_ROTATION, 0f);
        float radius = 5f;
        entity.setUserData(UserData.RADIUS, radius);
        float health = 2100f;
        entity.setUserData(UserData.HEALTH_MAX, health);
        entity.setUserData(UserData.HEALTH_CURRENT, health);
        if (params.age < 0f) {
            entity.setUserData(UserData.HEALTH_CURRENT, 0f);
        }
        entity.setUserData(UserData.DAMAGE_FACTOR, 1f);
        entity.setUserData(UserData.LIFE_STEAL_BASE, 0f);
        entity.setUserData(UserData.LIFE_STEAL, 0f);

        entity.getChild(0).scale(3f);

        entity.addControl(new CCharacterPhysics(radius, 20f, 300f));
        entity.getControl(CCharacterPhysics.class).setPhysicsDamping(0.2f);
        entity.addControl(new CCharacterMovement());
        entity.addControl(new CActionQueue());

        CSpellCast spellCastControl = new CSpellCast();
        entity.addControl(spellCastControl);

        Spell stoneFist = Spell.getSpell("StoneFist");
        Spell seal = Spell.getSpell("SealingBoulder");
        Spell spirit = Spell.getSpell("Spirit Stone");
        Spell angrySpirit = Spell.getSpell("Angry Spirit Stone");
        Spell toss = Spell.getSpell("Toss");
        Spell mineral = Spell.getSpell("MineralArmor");
        Spell bedrock = Spell.getSpell("Bedrock");
        Spell quake = Spell.getSpell("EarthQuake");

        int RId = InputMapping.getId(InputMapping.R);
        int QId = InputMapping.getId(InputMapping.Q);
        
        spellCastControl.putSpell(stoneFist,
                InputMapping.getId(InputMapping.M1));
        spellCastControl.putSpell(seal,
                InputMapping.getId(InputMapping.M2));
        spellCastControl.putSpell(spirit, QId);
        spellCastControl.putSpell(angrySpirit, -QId);
        spellCastControl.putSpell(toss,
                InputMapping.getId(InputMapping.E));
        spellCastControl.putSpell(mineral, RId);
        spellCastControl.putSpell(bedrock, -RId);
        spellCastControl.putSpell(quake,
                InputMapping.getId(InputMapping.SPACE));
        
        spellCastControl.putSecondaryMapping(InputMapping.SEC1, -QId);
        spellCastControl.putSecondaryMapping(InputMapping.SEC2, -RId);
        
        TossValidator tossValidator = new TossValidator(toss);
        spellCastControl.addCastValidator(tossValidator);

        AnimControl animControl = entity.getControl(AnimControl.class);
        CCharacterAnimation characterAnimControl = new CCharacterAnimation(animControl);

        AnimationData deathAnim = new AnimationData("Death", 1f, LoopMode.DontLoop);
        AnimationData runAnim = new AnimationData("Run", 1.5f, LoopMode.DontLoop);
        characterAnimControl.setDeathAnimation(deathAnim);
        characterAnimControl.setWalkAnimation(runAnim);

        entity.addControl(characterAnimControl);

        float fistSpeed = AnimationData.calculateSpeedForAnimation(animControl, "Attack1",
                27f / 85f, stoneFist.getCastTime());
        AlternatingAnimation fistAnim = new AlternatingAnimation("Attack1", fistSpeed);
        fistAnim.addAnimation("Attack2");

        AnimationData chargeAnim = new AnimationData("Run", 10f, LoopMode.Loop);

        float boulderThrowSpeed = AnimationData.calculateSpeedForAnimation(animControl,
                "Throw_Rock2", 56f / 130f, seal.getCastTime());
        AnimationData boulderThrowAnim =
                new AnimationData("Throw_Rock2", boulderThrowSpeed, LoopMode.Loop);

        AnimationData spiritStoneAnim =
                new AnimationData("Throw_Rock", boulderThrowSpeed, LoopMode.Loop);

        characterAnimControl.addSpellAnimation("StoneFist", fistAnim);
        characterAnimControl.addSpellAnimation("SealingBoulder", boulderThrowAnim);
        characterAnimControl.addSpellAnimation("SpiritSTone", spiritStoneAnim);
        characterAnimControl.addSpellAnimation("Toss", fistAnim);
        characterAnimControl.addSpellAnimation("EarthQuake", chargeAnim);

        entity.addControl(new CInfluenceInterface());
        entity.addControl(new CCharacterDamage());
        entity.addControl(new CCharacterHeal());
        entity.addControl(new CCharacterSync());

        if (world.isClient()) {
            CCharacterSound soundControl = new CCharacterSound();
            entity.addControl(soundControl);
            soundControl.setSufferSound("Effects/Sound/RockGolemPain.wav");

            entity.addControl(new CCharacterBuff());
            entity.addControl(new CCharacterHud());

            entity.addControl(new CSyncInterpolation());
            entity.getControl(CInfluenceInterface.class).setIsServer(false);
        } else {
            CResting restingControl = new CResting();
            entity.addControl(restingControl);
        }
        return entity;
    }
}

class AlternatingAnimation extends AnimationData {

    private int currentIndex = 0;
    private List<String> animations = new ArrayList<>(2);

    public AlternatingAnimation(String name, float speed) {
        super(name, speed, LoopMode.DontLoop);
        animations.add(name);
    }

    public void addAnimation(String name) {
        animations.add(name);
    }

    @Override
    public String getName() {
        currentIndex = (currentIndex + 1) % animations.size();
        return animations.get(currentIndex);
    }
}