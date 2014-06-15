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

    private ClientHudManager clientHudManager;

    public Venator(ClientHudManager clientHudManager) {
        this.clientHudManager = clientHudManager;
    }    
    
    @Override
    public Node build() {
        final Node entity = (Node) AbstractNodeBuilder.assetManager.loadModel("Models/Warwolf.j3o");
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

        // Note: This works now but later life steal is set by buffs.
        entity.setUserData(UserDataStrings.LIFE_STEAL, 0.2f);

        for (Spatial childToScale : entity.getChildren()) {
            childToScale.scale(3f);
        }

        entity.addControl(new CharacterPhysicsControl(radius, 22f, 100f));

        entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(0.2f);
        entity.addControl(new ActionQueueControl());

        final SpellCastControl spellCastControl = new SpellCastControl(Venator.worldManager);
        entity.addControl(spellCastControl);
        final Spell rend = Spell.getSpell("Rend");
        final Spell dagger = Spell.getSpell("Damaging Dagger");
        final Spell leap = Spell.getSpell("Leap");
        final Spell scream = Spell.getSpell("Feral Scream");
        final Spell deepWounds = Spell.getSpell("Deep Wounds");

        spellCastControl.putSpell(rend, InputMappingStrings.M1);
        spellCastControl.putSpell(dagger, InputMappingStrings.M2);
        spellCastControl.putSpell(leap, InputMappingStrings.SPACE);
        spellCastControl.putSpell(scream, InputMappingStrings.Q);
        spellCastControl.putSpell(deepWounds, InputMappingStrings.E);
        spellCastControl.putSpell(Spell.getSpell("Survival Instinct"), InputMappingStrings.R);

        final CharacterAnimationControl animControl = new CharacterAnimationControl();

        final AnimationData deathAnim = new AnimationData("Die-1", 1f, LoopMode.DontLoop);
        final AnimationData walkAnim = new AnimationData("Run", 0.8f, LoopMode.DontLoop);
        animControl.setDeathAnimation(deathAnim);
        animControl.setWalkAnimation(walkAnim);

        entity.addControl(animControl);

        final float swipeSpeed = AnimationData.calculateSpeedForAnimation(animControl.getAnimControl(), "Swipe-Left", 2f / 5f, rend.getCastTime());
        final float throwSpeed = AnimationData.calculateSpeedForAnimation(animControl.getAnimControl(), "Throw", 4f / 9f, dagger.getCastTime());
        final float jumpSpeed = AnimationData.calculateSpeedForAnimation(animControl.getAnimControl(), "Jump", 9f / 17f, leap.getCastTime());
        final float roarSpeed = AnimationData.calculateSpeedForAnimation(animControl.getAnimControl(), "Roar", 4f / 9f, scream.getCastTime());
        final float chargeSpeed = AnimationData.calculateSpeedForAnimation(animControl.getAnimControl(), "Charge", 2f / 5f, deepWounds.getCastTime());

        final AnimationData swipeLeftAnim = new AnimationData("Swipe-Left", swipeSpeed, LoopMode.DontLoop);
        final AnimationData throwAnim = new AnimationData("Throw", throwSpeed, LoopMode.DontLoop);
        final AnimationData jumpAnim = new AnimationData("Jump", jumpSpeed, LoopMode.DontLoop);
        final AnimationData roarAnim = new AnimationData("Roar", roarSpeed, LoopMode.DontLoop);
        final AnimationData chargeAnim = new AnimationData("Charge", chargeSpeed, LoopMode.DontLoop);

        animControl.addSpellAnimation("Rend", swipeLeftAnim);
        animControl.addSpellAnimation("Damaging Dagger", throwAnim);
        animControl.addSpellAnimation("Leap", jumpAnim);
        animControl.addSpellAnimation("Feral Scream", roarAnim);
        animControl.addSpellAnimation("Deep Wounds", chargeAnim);
        animControl.addSpellAnimation("Survival Instinct", null);

        final AnimationData landAnim = new AnimationData("Land", 1f, LoopMode.DontLoop);
        animControl.addActionAnimation(landAnim);

        final float swipeUpSpeed = AnimationData.calculateSpeedForAnimation(animControl.getAnimControl(), "Swipe-Up", 3f / 5f, 0.2f);
        final AnimationData swipeUpAnim = new AnimationData("Swipe-Up", swipeUpSpeed, LoopMode.DontLoop);
        animControl.addActionAnimation(swipeUpAnim);

        final AnimationData swipeRightAnim = new AnimationData("Swipe-Right", swipeSpeed, LoopMode.DontLoop);
        animControl.addActionAnimation(swipeRightAnim);

        animControl.addActionAnimation(swipeLeftAnim);

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