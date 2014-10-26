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
import arkhados.effects.EffectBox;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.AnimationData;
import arkhados.util.UserDataStrings;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class RockGolem extends AbstractNodeBuilder {

    private final ClientHudManager clientHudManager;

    public RockGolem(ClientHudManager clientHudManager) {
        this.clientHudManager = clientHudManager;
        setEffectBox(new EffectBox());
    }

    @Override
    public Node build() {
        Node entity = (Node) assetManager.loadModel("Models/RockGolem.j3o");
        float movementSpeed = 33f;
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_ROTATION, 0.0f);
        float radius = 5.0f;
        entity.setUserData(UserDataStrings.RADIUS, radius);
        float health = 2700f;
        entity.setUserData(UserDataStrings.HEALTH_MAX, health);
        entity.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        entity.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        entity.setUserData(UserDataStrings.LIFE_STEAL, 0f);

        entity.getChild(0).scale(3f);        

        entity.addControl(new CharacterPhysicsControl(radius, 20.0f, 300f));
        entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(0.2f);

        entity.addControl(new ActionQueueControl());
        SpellCastControl spellCastControl = new SpellCastControl();
        entity.addControl(spellCastControl);

        AnimControl animControl = entity.getControl(AnimControl.class);
        CharacterAnimationControl characterAnimControl = new CharacterAnimationControl(animControl);

        AnimationData deathAnim = new AnimationData("Death", 1f, LoopMode.DontLoop);
        AnimationData runAnim = new AnimationData("Run", 1.5f, LoopMode.DontLoop);
        characterAnimControl.setDeathAnimation(deathAnim);
        characterAnimControl.setWalkAnimation(runAnim);

        entity.addControl(characterAnimControl);

        entity.addControl(new InfluenceInterfaceControl());
        entity.addControl(new CharacterSyncControl());

        if (worldManager.isClient()) {
            entity.addControl(new CharacterBuffControl());
            entity.addControl(new CharacterHudControl());

            clientHudManager.addCharacter(entity);
            entity.addControl(new SyncInterpolationControl());
            entity.getControl(InfluenceInterfaceControl.class).setIsServer(false);
        }
        return entity;
    }
}
