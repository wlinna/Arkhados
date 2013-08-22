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

package magebattle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import java.util.Iterator;
import magebattle.messages.StartGameMessage;

/**
 *
 * @author william
 */
public class ServerGameManager extends AbstractAppState {
    private SyncManager syncManager;
    private WorldManager worldManager;
    private boolean running;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        System.out.println("Initializing ServerGameManager");
        super.initialize(stateManager, app);
        this.worldManager = app.getStateManager().getState(WorldManager.class);
        this.syncManager = this.worldManager.getSyncManager();
        System.out.println("Initialized ServerGameManager");
    }

    public synchronized boolean startGame() {
        if (this.running) {
            return false;
        }

        this.running = true;
        this.syncManager.getServer().broadcast(new StartGameMessage());
        this.worldManager.loadLevel();
        this.worldManager.preloadModels(new String[] { "Models/Mage.j3o" });
        this.worldManager.attachLevel();

        int i = 0;
        for (Iterator<PlayerData> it = PlayerData.getPlayers().iterator(); it.hasNext();) {
            PlayerData playerData = it.next();
//            float height = this.worldManager.getSpatialsHeight("Textures/Mage_mesh.mesh.xml");
            Vector3f startingLocation = new Vector3f(WorldManager.STARTING_LOCATIONS[i++]);
            startingLocation.setY(7.0f);
            long entityId = this.worldManager.addNewEntity("Mage", startingLocation, new Quaternion());
            playerData.setData("character-entity-id", entityId);
        }
        return true;
    }

    @Override
    public void update(float tpf) {
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
