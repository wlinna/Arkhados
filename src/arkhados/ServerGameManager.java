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
import arkhados.net.Receiver;
import arkhados.net.Sender;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

/**
 *
 * @author william
 */
public class ServerGameManager extends AbstractAppState {

    private WorldManager worldManager;
    private RoundManager roundManager;
    private ServerFogManager fogManager;
    private boolean running;
    private Application app;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        worldManager = app.getStateManager().getState(WorldManager.class);
        roundManager = new RoundManager();
        fogManager = new ServerFogManager();

        roundManager.setEnabled(false);
        stateManager.attach(roundManager);
        stateManager.getState(Receiver.class).registerCommandHandler(roundManager);

        stateManager.attach(fogManager);

        this.app = app;
    }

    public synchronized boolean startGame() {
        if (running) {
            return false;
        }

        Sender sender = app.getStateManager().getState(Sender.class);

        roundManager.setEnabled(true);

        worldManager.preloadModels(new String[]{"Models/Mage.j3o",
            "Models/Warwolf.j3o", "Models/Circle.j3o", "Models/DamagingDagger.j3o"});

        running = true;
        sender.addCommand(new TopicOnlyCommand(Topic.START_GAME));

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