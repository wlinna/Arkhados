/*    This file is part of JMageBattle.

    JMageBattle is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JMageBattle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */

package magebattle.util;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.scene.Spatial;
import magebattle.WorldManager;
import magebattle.controls.ActionQueueControl;
import magebattle.controls.CharacterAnimationControl;
import magebattle.controls.InfluenceInterfaceControl;
import magebattle.controls.SpellCastControl;
import magebattle.spells.Spell;

/**
 *
 * @author william
 */
public class EntityFactory {

    private AssetManager assetManager;
    private WorldManager worldManager;

    public EntityFactory(AssetManager assetManager, WorldManager worldManager) {
        this.assetManager = assetManager;
        this.worldManager = worldManager;
    }

    public Spatial createEntityById(String id) {
        Spatial entity = null;

        if ("Mage".equals(id)) {
            entity = this.assetManager.loadModel("Models/" + id + ".j3o");
            entity.setUserData(UserDataStrings.SPEED_MOVEMENT, 20.0f);
            entity.setUserData(UserDataStrings.SPEED_ROTATION, 0.0f);
            float radius = 5.0f;
            entity.setUserData(UserDataStrings.RADIUS, radius);
            entity.setUserData(UserDataStrings.HEALTH_CURRENT, 100.0f);

            entity.addControl(new BetterCharacterControl(radius, 20.0f, 75.0f));
//            entity.addControl(new CharacterMovementControl());
            entity.addControl(new CharacterAnimationControl());
            entity.addControl(new ActionQueueControl());
            entity.addControl(new SpellCastControl(this.worldManager));
            entity.addControl(new InfluenceInterfaceControl());

        } else if ("Fireball".equals(id)) {
            Spell spell = Spell.getSpells().get(id);
            entity = spell.buildNode();
        }
        assert entity != null;
        return entity;
    }
}