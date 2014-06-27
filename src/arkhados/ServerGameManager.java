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

import arkhados.messages.TopicOnlyCommand;
import arkhados.net.Sender;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

/**
 *
 * @author william
 */
public class ServerGameManager extends AbstractAppState {

    private SyncManager syncManager;
    private WorldManager worldManager;
    private RoundManager roundManager;
    private boolean running;
    private Application app;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        System.out.println("Initializing ServerGameManager");
        super.initialize(stateManager, app);
        worldManager = app.getStateManager().getState(WorldManager.class);
        syncManager = worldManager.getSyncManager();
        roundManager = new RoundManager();
        roundManager.setEnabled(false);
        stateManager.attach(roundManager);
        this.app = app;
        System.out.println("Initialized ServerGameManager");
    }

    public synchronized boolean startGame() {
        if (running) {
            return false;
        }
        
        Sender sender = app.getStateManager().getState(Sender.class);
        
        roundManager.setEnabled(true);

        worldManager.preloadModels(new String[]{"Models/Mage.j3o", "Models/Warwolf.j3o",
            "Models/Circle.j3o", "Models/DamagingDagger.j3o"});

        running = true;
        sender.addCommand(new TopicOnlyCommand(TopicOnlyCommand.START_GAME));

        roundManager.serverStartGame();

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
