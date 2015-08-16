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

import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterDamage;
import arkhados.controls.CCharacterHeal;
import arkhados.controls.CCharacterMovement;
import arkhados.controls.CCharacterAnimation;
import arkhados.controls.CCharacterBuff;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CCharacterSound;
import arkhados.controls.CEliteSoldierAmmunition;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CResting;
import arkhados.controls.CSpellCast;
import arkhados.controls.CSync;
import arkhados.controls.CSyncInterpolation;
import arkhados.effects.EffectBox;
import arkhados.effects.RocketExplosionEffect;
import arkhados.effects.SimpleSoundEffect;
import arkhados.messages.sync.statedata.StateData;
import arkhados.spell.Spell;
import arkhados.ui.hud.elitesoldier.CEliteSoldierHud;
import arkhados.util.AnimationData;
import arkhados.util.InputMapping;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SkeletonControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 * Creates entity with EliteSoldiers's features.
 */
public class EliteSoldier extends AbstractNodeBuilder {

    public static final int ACTION_ROCKET_JUMP = 0;
    public static final int ACTION_SHOTGUN = 1;
    public static final int ACTION_RAILGUN = 2;
    public static final int ACTION_LIKE_A_PRO = 3;
    public static final int ACTION_PLASMAGUN = 4;
    public static final int ACTION_ROCKET_LAUNCHER = 5;

    public EliteSoldier() {
        setEffectBox(new EffectBox());
        getEffectBox().addActionEffect(ACTION_ROCKET_JUMP,
                new RocketExplosionEffect());
        getEffectBox().addActionEffect(ACTION_SHOTGUN,
                new SimpleSoundEffect("Effects/Sound/Shotgun.wav"));
        getEffectBox().addActionEffect(ACTION_RAILGUN,
                new SimpleSoundEffect("Effects/Sound/Railgun.wav"));
        getEffectBox().addActionEffect(ACTION_LIKE_A_PRO,
                new SimpleSoundEffect("Effects/Sound/LikeAPro.wav"));
        getEffectBox().addActionEffect(ACTION_PLASMAGUN,
                new SimpleSoundEffect("Effects/Sound/PlasmagunLaunch.wav"));
        getEffectBox().addActionEffect(ACTION_ROCKET_LAUNCHER,
                new SimpleSoundEffect("Effects/Sound/RocketLauncher.wav"));
    }

    @Override
    public Node build(BuildParameters params) {
        Node entity = new Node("elite-soldier");
        entity.setLocalTranslation(params.location);
        Node real = (Node) assetManager.loadModel("Models/EliteSoldier.j3o");
        entity.attachChild(real);
        real.scale(11f);

        Node attachmentsNode = real.getControl(SkeletonControl.class)
                .getAttachmentsNode("Central_Bone.001_R.004");

        Node weapon = (Node) assetManager.loadModel("Models/Weapon.j3o");

        attachmentsNode.attachChild(weapon);
        weapon.setLocalTranslation(0, 0, 0);

        // Thanks for Allexit for helping set weapons rotation correctly
        Quaternion zQuat = new Quaternion();
        zQuat.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Z);
        Quaternion yQuat = new Quaternion();
        yQuat.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
        weapon.setLocalRotation(yQuat.add(zQuat));

        float movementSpeed = 36f;
        entity.setUserData(UserData.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserData.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserData.SPEED_ROTATION, 0f);
        float radius = 5f;
        entity.setUserData(UserData.RADIUS, radius);
        float health = 1675f;
        entity.setUserData(UserData.HEALTH_MAX, health);
        entity.setUserData(UserData.HEALTH_CURRENT, health);
        if (params.age < 0f) {
            entity.setUserData(UserData.HEALTH_CURRENT, 0f);
        }
        entity.setUserData(UserData.DAMAGE_FACTOR, 1f);
        entity.setUserData(UserData.LIFE_STEAL_BASE, 0f);
        entity.setUserData(UserData.LIFE_STEAL, 0f);

        entity.addControl(new CCharacterPhysics(radius, 20f, 75f));

        /**
         * By setting physics damping to low value, we can effectively apply
         * impulses on it.
         */
        entity.getControl(CCharacterPhysics.class).setPhysicsDamping(.2f);
        entity.addControl(new CCharacterMovement());
        entity.addControl(new CActionQueue());

        /**
         * To add spells to entity, create SpellCastControl and call its
         * putSpell-method with name of the spell as argument.
         */
        CEliteSoldierAmmunition ammunitionControl =
                new CEliteSoldierAmmunition();
        entity.addControl(ammunitionControl);

        CSpellCast spellCastControl = new CSpellCast();
        entity.addControl(spellCastControl);
        spellCastControl.addCastValidator(ammunitionControl);
        spellCastControl.addCastListeners(ammunitionControl);

        int M2id = InputMapping.getId(InputMapping.M2);
        int Qid = InputMapping.getId(InputMapping.Q);

        spellCastControl.putSpell(Spell.getSpell("Shotgun"),
                InputMapping.getId(InputMapping.M1));
        spellCastControl.putSpell(Spell.getSpell("Railgun"), M2id);
        spellCastControl.putSpell(Spell.getSpell("Blinding Ray"), -M2id);
        spellCastControl.putSpell(Spell.getSpell("Plasmagun"), Qid);
        spellCastControl.putSpell(Spell.getSpell("Plasma Grenades"), -Qid);
        spellCastControl.putSpell(Spell.getSpell("Rocket Launcher"),
                InputMapping.getId(InputMapping.E));
        spellCastControl.putSpell(Spell.getSpell("Like a Pro"),
                InputMapping.getId(InputMapping.R));
        spellCastControl.putSpell(Spell.getSpell("Rocket Jump"),
                InputMapping.getId(InputMapping.SPACE));

        spellCastControl.putSecondaryMapping(InputMapping.SEC1, -M2id);
        spellCastControl.putSecondaryMapping(InputMapping.SEC2, -Qid);

        /**
         * Map Spell names to casting animation's name. In this case all spells
         * use same animation.
         */
        AnimControl animControl = real.getControl(AnimControl.class);
        CCharacterAnimation characterAnimControl =
                new CCharacterAnimation(animControl);

        AnimationData deathAnim =
                new AnimationData("Die", 1f, LoopMode.DontLoop);
        AnimationData walkAnim = new AnimationData("Run", 1f, LoopMode.Cycle);

        characterAnimControl.setDeathAnimation(deathAnim);
        characterAnimControl.setWalkAnimation(walkAnim);
        entity.addControl(characterAnimControl);

        AnimationData animationData =
                new AnimationData("Shoot", 1f, LoopMode.DontLoop);

        characterAnimControl.addSpellAnimation("Shotgun", animationData);
        characterAnimControl.addSpellAnimation("Railgun", animationData);
        characterAnimControl.addSpellAnimation("Blinding Ray", animationData);
        characterAnimControl.addSpellAnimation("Plasmagun", animationData);
        characterAnimControl
                .addSpellAnimation("Plasma Grenades", animationData);
        characterAnimControl
                .addSpellAnimation("Rocket Launcher", animationData);
        characterAnimControl.addSpellAnimation("Like a Pro", null);
        characterAnimControl.addSpellAnimation("Rocket Jump", animationData);

        entity.addControl(new CInfluenceInterface());
        entity.addControl(new CCharacterDamage());
        entity.addControl(new CCharacterHeal());

        entity.addControl(new CEliteSoldierSync());

        if (world.isClient()) {
            CCharacterSound soundControl = new CCharacterSound();
            entity.addControl(soundControl);
            soundControl.setSufferSound("Effects/Sound/EliteSoldierPain.wav");
            soundControl.setDeathSound("Effects/Sound/EliteSoldierDeath.wav");
            entity.addControl(new CCharacterBuff());
            entity.addControl(new CEliteSoldierHud());

            entity.addControl(new CSyncInterpolation());
            entity.getControl(CInfluenceInterface.class)
                    .setIsServer(false);
        } else {
            CResting restingControl = new CResting();
            entity.addControl(restingControl);
        }

        return entity;
    }
}

class CEliteSoldierSync extends AbstractControl implements CSync {

    @Override
    public StateData getSyncableData(StateData stateData) {
        return new EliteSoldierSyncData(
                (int) getSpatial().getUserData(UserData.ENTITY_ID),
                getSpatial());
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}