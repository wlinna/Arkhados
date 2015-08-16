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

import arkhados.gamemode.DeathMatch;
import arkhados.gamemode.GameMode;
import arkhados.gamemode.TeamDeathmatch;
import arkhados.messages.CmdTopicOnly;
import arkhados.net.Sender;
import arkhados.settings.server.Settings;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

public class ServerGameManager extends AbstractAppState {

    private World world;
    private ServerFog fog;
    private boolean running = false;
    private Application app;
    private GameMode gameMode;

    public ServerGameManager() {
        
        switch (Settings.get().General().getGameMode()) {
            case "Deathmatch":
                gameMode = new DeathMatch();
                break;
            case "TeamDeathmatch":
                gameMode = new TeamDeathmatch();
                break;
                
        }

        CharacterInteraction.gameMode = gameMode;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        gameMode.initialize(app);
        world = app.getStateManager().getState(World.class);
        fog = new ServerFog();

        stateManager.attach(fog);

        this.app = app;

        ServerMain serverApp = (ServerMain) app;
        serverApp.startGame();
    }

    public synchronized boolean startGame() {
        if (running) {
            return false;
        }

        Sender sender = app.getStateManager().getState(Sender.class);

        Preloader.loadServer(Globals.assetManager);
        app.getStateManager().getState(Sync.class).addObject(-1, world);

        running = true;
        sender.addCommand(new CmdTopicOnly(Topic.START_GAME));

        gameMode.startGame();

        return true;
    }

    @Override
    public void update(float tpf) {
        if (!running) {
            return;
        }

        gameMode.update(tpf);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void playerJoined(int playerId) {
        gameMode.playerJoined(playerId);
    }
}