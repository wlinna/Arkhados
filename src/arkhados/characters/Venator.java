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
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.spell.Spell;
import arkhados.util.AnimationData;
import arkhados.util.InputMappingStrings;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class Venator extends NodeBuilder {

    @Override
    public Node build() {
        final Node entity = (Node) NodeBuilder.assetManager.loadModel("Models/Warwolf.j3o");
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

        entity.scale(3f);

        entity.addControl(new CharacterPhysicsControl(radius, 22f, 100f));

        entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(0.2f);
        entity.addControl(new ActionQueueControl());

        final SpellCastControl spellCastControl = new SpellCastControl(Venator.worldManager);
        entity.addControl(spellCastControl);
        final Spell rend = Spell.getSpells().get("Rend");
        final Spell dagger = Spell.getSpells().get("Damaging Dagger");
        final Spell leap = Spell.getSpells().get("Leap");
        final Spell scream = Spell.getSpells().get("Feral Scream");
        final Spell deepWounds = Spell.getSpells().get("Deep Wounds");

        spellCastControl.putSpell(rend, InputMappingStrings.M1);
        spellCastControl.putSpell(dagger, InputMappingStrings.M2);
        spellCastControl.putSpell(leap, InputMappingStrings.SPACE);
        spellCastControl.putSpell(scream, InputMappingStrings.Q);
        spellCastControl.putSpell(deepWounds, InputMappingStrings.E);
        spellCastControl.putSpell(Spell.getSpells().get("Survival Instinct"), InputMappingStrings.R);

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

        final AnimationData swipeAnim = new AnimationData("Swipe-Left", swipeSpeed, LoopMode.DontLoop);
        final AnimationData throwAnim = new AnimationData("Throw", throwSpeed, LoopMode.DontLoop);
        final AnimationData jumpAnim = new AnimationData("Jump", jumpSpeed, LoopMode.DontLoop);
        final AnimationData roarAnim = new AnimationData("Roar", roarSpeed, LoopMode.DontLoop);
        final AnimationData chargeAnim = new AnimationData("Charge", chargeSpeed, LoopMode.DontLoop);

        animControl.addSpellAnimation("Rend", swipeAnim);
        animControl.addSpellAnimation("Damaging Dagger", throwAnim);
        animControl.addSpellAnimation("Leap", jumpAnim);
        animControl.addSpellAnimation("Feral Scream", roarAnim);
        animControl.addSpellAnimation("Deep Wounds", chargeAnim);
        animControl.addSpellAnimation("Survival Instinct", null);

        final AnimationData landAnim = new AnimationData("Land", 1f, LoopMode.DontLoop);
        animControl.addActionAnimation("Land", landAnim);

        final float swipeUpSpeed = AnimationData.calculateSpeedForAnimation(animControl.getAnimControl(), "Swipe-Up", 3f / 5f, 0.2f);
        final AnimationData swipeUpAnim = new AnimationData("Swipe-Up", swipeUpSpeed, LoopMode.DontLoop);
        animControl.addActionAnimation("Swipe-Up", swipeUpAnim);

        entity.addControl(new InfluenceInterfaceControl());

        if (worldManager.isClient()) {
            entity.addControl(new CharacterBuffControl());
        }


        return entity;
    }
}
