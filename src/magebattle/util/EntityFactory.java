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
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import magebattle.ClientHudManager;
import magebattle.WorldManager;
import magebattle.controls.CharacterAnimationControl;
import magebattle.controls.CharacterPhysicsControl;
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
    private ClientHudManager clientHudManager = null;

    public EntityFactory(AssetManager assetManager, WorldManager worldManager, ClientHudManager clientHudManager) {
        this.assetManager = assetManager;
        this.worldManager = worldManager;
        this.clientHudManager = clientHudManager;
    }

    public Spatial createEntityById(String id) {
        Node entity = null;

        if ("Mage".equals(id)) {
            entity = (Node)this.assetManager.loadModel("Models/" + id + ".j3o");
            entity.setUserData(UserDataStrings.SPEED_MOVEMENT, 20.0f);
            entity.setUserData(UserDataStrings.SPEED_ROTATION, 0.0f);
            float radius = 5.0f;
            entity.setUserData(UserDataStrings.RADIUS, radius);
            entity.setUserData(UserDataStrings.HEALTH_CURRENT, 100.0f);

            entity.addControl(new CharacterPhysicsControl(radius, 20.0f, 75.0f));
            entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(0.2f);
//            entity.addControl(new CharacterMovementControl());
            entity.addControl(new CharacterAnimationControl());
            SpellCastControl spellCastControl = new SpellCastControl(this.worldManager);
            entity.addControl(spellCastControl);

            spellCastControl.addSpell(Spell.getSpells().get("Fireball"));
            spellCastControl.addSpell(Spell.getSpells().get("Ember Circle"));


            entity.addControl(new InfluenceInterfaceControl());


            if (worldManager.isClient()) {
                this.clientHudManager.addCharacter(entity);
            }

        } else if ("Fireball".equals(id)) {
            Spell spell = Spell.getSpells().get(id);
            entity = spell.buildNode();
        } else if ("Ember Circle".equals(id)) {
            Spell spell = Spell.getSpells().get(id);
            entity = spell.buildNode();
        }
        assert entity != null;
        return entity;
    }
}