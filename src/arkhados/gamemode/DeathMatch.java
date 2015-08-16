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
package arkhados.gamemode;

import arkhados.CharacterInteraction;
import arkhados.Globals;
import arkhados.MusicManager;
import arkhados.Sync;
import arkhados.Topic;
import arkhados.World;
import arkhados.messages.CmdSelectHero;
import arkhados.messages.CmdPlayerKill;
import arkhados.messages.CmdTopicOnly;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Receiver;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
import arkhados.settings.server.Settings;
import arkhados.ui.hud.ServerClientDataStrings;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.HostedConnection;
import de.lessvoid.nifty.Nifty;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class DeathMatch extends GameMode implements CommandHandler {

    private static final Logger logger =
            Logger.getLogger(DeathMatch.class.getName());
    private DeathmatchCommon common = new DeathmatchCommon();
    private World world;
    private AppStateManager stateManager;
    private Sync sync;
    private Nifty nifty;

    @Override
    public void initialize(Application app) {
        super.initialize(app);
        stateManager = app.getStateManager();
        world = stateManager.getState(World.class);
        sync = stateManager.getState(Sync.class);
        stateManager.getState(Receiver.class).registerCommandHandler(this);
        common.initialize(app);
        
        Settings.Deathmatch settings = Settings.get().Deathmatch();
        common.setKillLimit(settings.getKillLimit());
        common.setRespawnTime(settings.getRespawnTime());
    }

    @Override
    public void startGame() {
        getApp().enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                world.setEnabled(true);
                world.loadLevel();
                world.attachLevel();

                Sender sender = stateManager.getState(Sender.class);

                if (sender.isClient() && !Globals.replayMode) {
                    nifty.gotoScreen("default_hud");
                    common.getHeroSelectionLayer().show();
                    sender.addCommand(
                            new CmdTopicOnly(Topic.CLIENT_WORLD_CREATED));
                } else if (sender.isServer()) {
                    sync.setEnabled(true);
                    sync.startListening();
                }
                return null;
            }
        });
    }

    @Override
    public void update(float tpf) {
        common.update(tpf);
    }

    @Override
    public void playerJoined(int playerId) {
        common.preparePlayer(playerId);
    }

    @Override
    public void playerDied(int playerId, int killersPlayerId) {
        common.playerDied(playerId, killersPlayerId);

        int kills = CharacterInteraction.getCurrentRoundStats()
                .getKills(killersPlayerId);

        if (kills >= common.getKillLimit()) {
            Sender sender = stateManager.getState(ServerSender.class);
            sender.addCommand(new CmdTopicOnly(Topic.GAME_ENDED));
        }
    }

    @Override
    public void readGuaranteed(Object source, Command guaranteed) {
        if (source instanceof HostedConnection) {
            serverReadGuaranteed((HostedConnection) source, guaranteed);
        } else {
            clientReadGuaranteed(guaranteed);
        }
    }

    private void serverReadGuaranteed(HostedConnection source,
            Command command) {
        if (command instanceof CmdSelectHero) {
            int playerId =
                    source.getAttribute(ServerClientDataStrings.PLAYER_ID);
            common.playerChoseHero(playerId,
                    ((CmdSelectHero) command).getHeroName());
            stateManager.getState(ServerSender.class)
                    .addCommandForSingle(command, source);
        } else if (command instanceof CmdTopicOnly) {
            common.serverHandleTopicOnlyCommand(source, (CmdTopicOnly) command);
        }
    }

    private void clientReadGuaranteed(Command command) {
        if (command instanceof CmdPlayerKill) {
            CmdPlayerKill pkCommand = (CmdPlayerKill) command;
            common.clientPlayerDied(pkCommand.getDiedPlayerId(),
                    pkCommand.getKillerPlayerId(), pkCommand.getKillingSpree(),
                    pkCommand.getCombo(), pkCommand.getEndedSpree());
        } else if (command instanceof CmdTopicOnly) {
            common.clientHandleTopicOnlyCommand((CmdTopicOnly) command);
        } else if (command instanceof CmdSelectHero) {
            String hero = ((CmdSelectHero) command).getHeroName();
            stateManager.getState(MusicManager.class).setMusicCategory(hero);
        }
    }

    @Override
    public void readUnreliable(Object source, Command unreliable) {
    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;
        common.setNifty(nifty);
        common.getHeroSelectionLayer().show();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        stateManager.getState(Receiver.class).removeCommandHandler(this);
        stateManager.getState(MusicManager.class).setPlaying(false);
    }

    @Override
    public void gameEnded() {
        common.gameEnded();
    }
}