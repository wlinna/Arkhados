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

import arkhados.ui.hud.ClientHudManager;
import arkhados.WorldManager;
import arkhados.characters.EmberMage;
import arkhados.characters.Venator;
import arkhados.controls.SyncInterpolationControl;
import arkhados.controls.DebugControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.Spell;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates all game entities
 *
 * @author william
 */
public class EntityFactory {

    private AssetManager assetManager;
    private WorldManager worldManager;
    private ClientHudManager clientHudManager = null;
    private List<String> heroNames = new ArrayList<String>(1);

    {
        heroNames.add("Mage");
        heroNames.add("Venator");
    }

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
     */
    public EntityFactory(AssetManager assetManager, WorldManager worldManager, ClientHudManager clientHudManager) {
        this.assetManager = assetManager;
        this.worldManager = worldManager;
        this.clientHudManager = clientHudManager;
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
            entity = new EmberMage().build();
            entity.addControl(new DebugControl(this.assetManager));

            if (worldManager.isClient()) {
                this.clientHudManager.addCharacter(entity);
                entity.addControl(new SyncInterpolationControl());
                entity.getControl(InfluenceInterfaceControl.class).setIsServer(false);
            }

        } else if ("Venator".equals(id)) {
            entity = new Venator().build();
            entity.addControl(new DebugControl(this.assetManager));
            if (worldManager.isClient()) {
                this.clientHudManager.addCharacter(entity);
                entity.addControl(new SyncInterpolationControl());
                entity.getControl(InfluenceInterfaceControl.class).setIsServer(false);
            }
        } else if (Spell.getSpells().containsKey(id)) {
            Spell spell = Spell.getSpells().get(id);
            entity = spell.buildNode();
        }

        // TODO: Detect if entity is character
        assert entity != null;
        return entity;
    }
}