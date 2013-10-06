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
package arkhados;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import arkhados.messages.StartGameMessage;

/**
 *
 * @author william
 */
public class ServerGameManager extends AbstractAppState {

    private SyncManager syncManager;
    private WorldManager worldManager;
    private RoundManager roundManager;
    private boolean running;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        System.out.println("Initializing ServerGameManager");
        super.initialize(stateManager, app);
        this.worldManager = app.getStateManager().getState(WorldManager.class);
        this.syncManager = this.worldManager.getSyncManager();
        this.roundManager = new RoundManager();
        this.roundManager.setEnabled(false);
        stateManager.attach(this.roundManager);
        System.out.println("Initialized ServerGameManager");
    }

    public synchronized boolean startGame() {
        if (this.running) {
            return false;
        }
        this.roundManager.setEnabled(true);

        this.worldManager.preloadModels(new String[]{"Models/Mage.j3o", "Models/Warwolf.j3o", "Models/Circle.j3o"});

        this.running = true;
        this.syncManager.getServer().broadcast(new StartGameMessage());

        this.roundManager.serverStartGame();

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
