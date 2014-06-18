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

import arkhados.EffectHandler;
import arkhados.ui.hud.ClientHudManager;
import arkhados.WorldManager;
import arkhados.characters.EliteSoldier;
import arkhados.characters.EmberMage;
import arkhados.characters.Venator;
import arkhados.effects.EffectBox;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import java.util.ArrayList;

/**
 * Creates all game entities
 *
 * @author william
 */
public class EntityFactory {

    private AssetManager assetManager;
    private WorldManager worldManager;
    private ClientHudManager clientHudManager = null;
    private int runningId = -1;
    private ArrayList<NodeBuilder> nodeBuilders = new ArrayList<>(40);
    private EffectHandler effectHandler;

    /**
     * Server side EntityFactory constructor. Should be called only once
     *
     * @param assetManager
     * @param worldManager
     */
    public EntityFactory(AssetManager assetManager, WorldManager worldManager) {
        this.assetManager = assetManager;
        this.worldManager = worldManager;
        this.addNodeBuilders();
    }

    /**
     * Client side EntityFactory constructor. Should be called once per game
     *
     * @param assetManager
     * @param worldManager
     * @param clientHudManager
     */
    public EntityFactory(AssetManager assetManager, WorldManager worldManager,
            ClientHudManager clientHudManager, EffectHandler effectHandler) {
        this.assetManager = assetManager;
        this.worldManager = worldManager;
        this.clientHudManager = clientHudManager;
        this.effectHandler = effectHandler;
        this.addNodeBuilders();
    }

    public Node createEntityById(int id) {
        if (this.nodeBuilders.size() <= id) {
            return null;
        }
        Node node = this.nodeBuilders.get(id).build();
        node.setUserData(UserDataStrings.NODE_BUILDER_ID, id);
        return node;
    }

    private void addNodeBuilders() {
        int mageId = this.addNodeBuilder(new EmberMage(clientHudManager));
        int venatorId = this.addNodeBuilder(new Venator(clientHudManager));        
        int soldierId = this.addNodeBuilder(new EliteSoldier(clientHudManager));


        NodeBuilderIdHeroNameMatcherSingleton.get().addMapping("Mage", mageId);
        NodeBuilderIdHeroNameMatcherSingleton.get().addMapping("Venator", venatorId);
        NodeBuilderIdHeroNameMatcherSingleton.get().addMapping("Elite Soldier", soldierId);


    }

    private int newNodeBuilderId() {
        return ++this.runningId;
    }

    public int addNodeBuilder(NodeBuilder builder) {
        this.nodeBuilders.add(builder);
        int nodeBuilderId = this.newNodeBuilderId();
        if (this.effectHandler != null && builder != null) {
            EffectBox effectBox = builder.getEffectBox();
            if (effectBox != null) {
                this.effectHandler.addEffectBox(nodeBuilderId, effectBox);
            }
        }
        return nodeBuilderId;
    }
}