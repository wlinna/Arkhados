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

import arkhados.Globals;
import arkhados.components.CRest;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterAnimationControl;
import arkhados.controls.CharacterBuffControl;
import arkhados.controls.CharacterHudControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.CharacterSoundControl;
import arkhados.controls.ComponentAccessor;
import arkhados.controls.EliteSoldierAmmunitionControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.controls.SyncControl;
import arkhados.controls.SyncInterpolationControl;
import arkhados.effects.EffectBox;
import arkhados.effects.RocketExplosionEffect;
import arkhados.effects.SimpleSoundEffect;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.spell.Spell;
import arkhados.systems.SRest;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.AnimationData;
import arkhados.util.InputMappingStrings;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SkeletonControl;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 * Creates entity with EliteSoldiers's features.
 *
 * @author william
 */
public class EliteSoldier extends AbstractNodeBuilder {

    public static final int ACTION_ROCKET_JUMP = 0;
    public static final int ACTION_SHOTGUN = 1;
    public static final int ACTION_RAILGUN = 2;
    public static final int ACTION_LIKE_A_PRO = 3;
    public static final int ACTION_PLASMAGUN = 4;
    public static final int ACTION_ROCKET_LAUNCHER = 5;
    private ClientHudManager clientHudManager;

    public EliteSoldier(ClientHudManager clientHudManager) {
        this.clientHudManager = clientHudManager;
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
    public Node build(Object location) {
        Node entity = new Node("elite-soldier");
        entity.setLocalTranslation((Vector3f) location);
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
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_ROTATION, 0.0f);
        float radius = 5.0f;
        entity.setUserData(UserDataStrings.RADIUS, radius);
        float health = 1675f;
        entity.setUserData(UserDataStrings.HEALTH_MAX, health);
        entity.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        entity.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        entity.setUserData(UserDataStrings.LIFE_STEAL, 0f);

        AppStateManager stateManager = Globals.app.getStateManager();

        ComponentAccessor componentAccessor = new ComponentAccessor();
        entity.addControl(componentAccessor);

        entity.addControl(new CharacterPhysicsControl(radius, 20.0f, 75.0f));

        /**
         * By setting physics damping to low value, we can effectively apply
         * impulses on it.
         */
        entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(.2f);
        entity.addControl(new ActionQueueControl());

        /**
         * To add spells to entity, create SpellCastControl and call its
         * putSpell-method with name of the spell as argument.
         */
        EliteSoldierAmmunitionControl ammunitionControl =
                new EliteSoldierAmmunitionControl();
        entity.addControl(ammunitionControl);

        SpellCastControl spellCastControl = new SpellCastControl();
        entity.addControl(spellCastControl);
        spellCastControl.addCastValidator(ammunitionControl);
        spellCastControl.addCastListeners(ammunitionControl);

        spellCastControl.putSpell(Spell.getSpell("Shotgun"),
                InputMappingStrings.getId(InputMappingStrings.M1));
        spellCastControl.putSpell(Spell.getSpell("Railgun"),
                InputMappingStrings.getId(InputMappingStrings.M2));
        spellCastControl.putSpell(Spell.getSpell("Plasmagun"),
                InputMappingStrings.getId(InputMappingStrings.Q));
        spellCastControl.putSpell(Spell.getSpell("Rocket Launcher"),
                InputMappingStrings.getId(InputMappingStrings.E));
        spellCastControl.putSpell(Spell.getSpell("Like a Pro"),
                InputMappingStrings.getId(InputMappingStrings.R));
        spellCastControl.putSpell(Spell.getSpell("Rocket Jump"),
                InputMappingStrings.getId(InputMappingStrings.SPACE));

        /**
         * Map Spell names to casting animation's name. In this case all spells
         * use same animation.
         */
        AnimControl animControl = real.getControl(AnimControl.class);
        CharacterAnimationControl characterAnimControl =
                new CharacterAnimationControl(animControl);

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
        characterAnimControl.addSpellAnimation("Plasmagun", animationData);
        characterAnimControl.addSpellAnimation("Rocket Launcher", animationData);
        characterAnimControl.addSpellAnimation("Like a Pro", null);
        characterAnimControl.addSpellAnimation("Rocket Jump", animationData);

        entity.addControl(new InfluenceInterfaceControl());
        entity.addControl(new EliteSoldierSyncControl());

        if (worldManager.isClient()) {
            CharacterSoundControl soundControl = new CharacterSoundControl();
            entity.addControl(soundControl);
            soundControl.setSufferSound("Effects/Sound/EliteSoldierPain.wav");
            soundControl.setDeathSound("Effects/Sound/EliteSoldierDeath.wav");
            entity.addControl(new CharacterBuffControl());
            entity.addControl(new CharacterHudControl());

            entity.addControl(new SyncInterpolationControl());
            entity.getControl(InfluenceInterfaceControl.class)
                    .setIsServer(false);
        } else {
            CRest cResting = new CRest();
            cResting.spatial = entity;
            componentAccessor.resting = cResting;
            stateManager.getState(SRest.class).addComponent(cResting);
        }

        return entity;
    }
}

class EliteSoldierSyncControl extends AbstractControl implements SyncControl {

    @Override
    public StateData getSyncableData(StateData stateData) {
        return new EliteSoldierSyncData(
                (int) getSpatial().getUserData(UserDataStrings.ENTITY_ID),
                getSpatial());
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}