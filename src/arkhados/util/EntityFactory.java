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
package arkhados.util;

import arkhados.ClientHudManager;
import arkhados.UserCommandManager;
import arkhados.WorldManager;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterAnimationControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.spell.Spell;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Creates all game entities
 *
 * @author william
 */
public class EntityFactory {

    private AssetManager assetManager;
    private WorldManager worldManager;
    private ClientHudManager clientHudManager = null;
    private UserCommandManager userCommandManager = null;

    /**
     * Server side EntityFactory constructor. Should be called only once
     *
     * @param assetManager
     * @param worldManager
     */
    public EntityFactory(AssetManager assetManager, WorldManager worldManager) {
        this.assetManager = assetManager;
        this.worldManager = worldManager;
    }

    /**
     * Client side EntityFactory constructor. Should be called once per game
     *
     * @param assetManager
     * @param worldManager
     * @param clientHudManager
     * @param userCommandManager
     */
    public EntityFactory(AssetManager assetManager, WorldManager worldManager, ClientHudManager clientHudManager, UserCommandManager userCommandManager) {
        this.assetManager = assetManager;
        this.worldManager = worldManager;
        this.clientHudManager = clientHudManager;
        this.userCommandManager = userCommandManager;
    }

    /**
     *
     * @param id Id of entity to create. If id is name of entity, creates that
     * entity. If id is spell's name, creates node of that spell (i.e
     * projectile)
     * @return
     */
    public Spatial createEntityById(String id) {
        Node entity = null;

        if ("Mage".equals(id)) {
            entity = (Node) this.assetManager.loadModel("Models/" + id + ".j3o");
            entity.setUserData(UserDataStrings.SPEED_MOVEMENT, 40.0f);
            entity.setUserData(UserDataStrings.SPEED_ROTATION, 0.0f);
            float radius = 5.0f;
            entity.setUserData(UserDataStrings.RADIUS, radius);
            entity.setUserData(UserDataStrings.HEALTH_CURRENT, 1700.0f);

            entity.addControl(new CharacterPhysicsControl(radius, 20.0f, 75.0f));

            /**
             * By setting physics damping to low value, we can effectively apply
             * impulses on it.
             */
            entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(0.2f);
            entity.addControl(new ActionQueueControl());

            /**
             * To add spells to entity, create SpellCastControl and call its
             * addSpell-method with name of the spell as argument.
             */
            SpellCastControl spellCastControl = new SpellCastControl(this.worldManager);
            entity.addControl(spellCastControl);
            spellCastControl.addSpell(Spell.getSpells().get("Fireball"));
            spellCastControl.addSpell(Spell.getSpells().get("Magma Bash"));
            spellCastControl.addSpell(Spell.getSpells().get("Ember Circle"));

            /**
             * Map Spell names to casting animation's name. In this case all
             * spells use same animation.
             */
            CharacterAnimationControl animControl = new CharacterAnimationControl();
            entity.addControl(animControl);
            animControl.addSpellAnimation("Fireball", "Idle");
            animControl.addSpellAnimation("Magma Bash", "Idle");
            animControl.addSpellAnimation("Ember Circle", "Idle");

            entity.addControl(new InfluenceInterfaceControl());

            if (worldManager.isClient()) {
                this.clientHudManager.addCharacter(entity);
                this.userCommandManager.addKeySpellMapping(InputMappingStrings.M1, "Fireball");
                this.userCommandManager.addKeySpellMapping(InputMappingStrings.M2, "Magma Bash");
                this.userCommandManager.addKeySpellMapping(InputMappingStrings.Q, "Ember Circle");
            }

        } else if (Spell.getSpells().containsKey(id)) {
            Spell spell = Spell.getSpells().get(id);
            entity = spell.buildNode();
        }
        assert entity != null;
        return entity;
    }
}