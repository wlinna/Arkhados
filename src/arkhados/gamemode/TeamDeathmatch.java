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
import arkhados.PlayerData;
import arkhados.ServerClientData;
import arkhados.ServerMain;
import arkhados.Sync;
import arkhados.Topic;
import arkhados.UserCommandManager;
import arkhados.World;
import arkhados.messages.BattleStatisticsResponse;
import arkhados.messages.CmdPlayerKill;
import arkhados.messages.CmdSelectHero;
import arkhados.messages.CmdSelectTeam;
import arkhados.messages.CmdTeamAcceptance;
import arkhados.messages.CmdTeamOptions;
import arkhados.messages.CmdTopicOnly;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Receiver;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
import arkhados.settings.server.Settings;
import arkhados.ui.TeamSelectionBuilder;
import arkhados.ui.hud.ServerClientDataStrings;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.HostedConnection;
import com.jme3.util.IntMap;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeamDeathmatch extends GameMode implements CommandHandler {

    private final static Logger logger
            = Logger.getLogger(TeamDeathmatch.class.getName());

    private final DeathmatchCommon common = new DeathmatchCommon();
    private AppStateManager stateManager;
    private World world;
    private Sync sync;
    private Nifty nifty;
    private Screen screen;
    private final Map<String, Integer> teamNameId = new HashMap<>();

    {
        teamNameId.put("Blue", 0);
        teamNameId.put("Red", 1);
        teamNameId.put("Green", 2);
        teamNameId.put("Black", 3);
    }
    private final IntMap<Integer> teamKills = new IntMap<>();

    {
        teamKills.put(0, 0);
        teamKills.put(1, 0);
        teamKills.put(2, 0);
        teamKills.put(3, 0);
    }

    @Override
    public void initialize(Application app) {
        super.initialize(app);

        common.initialize(app);

        stateManager = app.getStateManager();
        sync = stateManager.getState(Sync.class);
        world = stateManager.getState(World.class);
        stateManager.getState(Receiver.class).registerCommandHandler(this);

        if (app instanceof ServerMain) {
            Settings.TeamDeathmatch settings = Settings.get().TeamDeathmatch();
            common.setKillLimit(settings.getKillLimit());
            common.setRespawnTime(settings.getRespawnTime());
        }
    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;
        common.setNifty(nifty);
        screen = nifty.getScreen("default_hud");
    }

    @Override
    public void startGame() {
        getApp().enqueue(() -> {
            world.setEnabled(true);
            world.loadLevel();
            world.attachLevel();

            Sender sender = stateManager.getState(Sender.class);

            if (sender.isClient() && !Globals.replayMode) {
                nifty.gotoScreen("default_hud");
                sender.addCommand(new CmdTopicOnly(Topic.CLIENT_WORLD_CREATED));
            } else if (sender.isServer()) {
                sync.setEnabled(true);
                sync.startListening();
                for (Map.Entry<String, Integer> entrySet :
                        teamNameId.entrySet()) {
                    CharacterInteraction.addTeam(entrySet.getValue());
                }

            }
            return null;
        });
    }

    @Override
    public void update(float tpf) {
        common.update(tpf);
    }

    @Override
    public void playerDied(int playerId, int killersPlayerId) {
        common.playerDied(playerId, killersPlayerId);

        if (killersPlayerId == -1) {
            return;
        }

        int killersTeam = PlayerData.getIntData(killersPlayerId,
                PlayerData.TEAM_ID);
        int kills;
        try { // TODO: Fix NPE that happens here.
            kills = teamKills.get(killersTeam) + 1;
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "killersTeam: {0}", killersTeam);
            logger.log(Level.SEVERE, 
                    "NullPointerException while getting kills:", e);            
            throw e;
        }

        teamKills.put(killersTeam, kills);

        if (kills >= common.getKillLimit()) {
            Sender sender = stateManager.getState(ServerSender.class);
            sender.addCommand(new CmdTopicOnly(Topic.GAME_ENDED));
        }

    }

    @Override
    public void playerJoined(final int playerId) {
        super.playerJoined(playerId);
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
        ServerSender sender = stateManager.getState(ServerSender.class);

        if (command instanceof CmdSelectHero) {
            int playerId
                    = source.getAttribute(ServerClientDataStrings.PLAYER_ID);
            common.playerChoseHero(playerId,
                    ((CmdSelectHero) command).getHeroName());
            sender.addCommandForSingle(command, source);

        } else if (command instanceof CmdSelectTeam) {
            CmdSelectTeam cmd = (CmdSelectTeam) command;
            Integer teamId = teamNameId.get(cmd.team);

            int playerId = ServerClientData.getPlayerId(
                    ((HostedConnection) source).getId());

            if (teamId == null) {
                sender.addCommandForSingle(new CmdTeamAcceptance(playerId, -2),
                        source);
                return;
            }

            PlayerData.setData(playerId, PlayerData.TEAM_ID, teamId);

            common.preparePlayer(playerId);

            sender.addCommand(new CmdTeamAcceptance(playerId, teamId));

        } else if (command instanceof CmdTopicOnly) {
            serverHandleTopicOnly(source, (CmdTopicOnly) command);
        }
    }

    private void clientReadGuaranteed(final Command command) {
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
        } else if (command instanceof CmdTeamOptions) {
            getApp().enqueue(() -> {
                List<String> options = ((CmdTeamOptions) command).teamOptions;
                Collections.sort(options);
                new TeamSelectionBuilder(options)
                        .build(nifty, screen, screen.getRootElement());
                return null;
            });

        } else if (command instanceof CmdTeamAcceptance) {
            CmdTeamAcceptance cmd = (CmdTeamAcceptance) command;
            if (cmd.getTeamId() != -1) {
                int myPlayerId = stateManager.getState(UserCommandManager.class)
                        .getPlayerId();

                PlayerData.setData(cmd.getPlayerId(), PlayerData.TEAM_ID,
                        cmd.getTeamId());
                if (cmd.getPlayerId() == myPlayerId && !Globals.replayMode) {
                    common.getHeroSelectionLayer().show();
                }
            } else {
                // FIXME: Show error message instead
                throw new RuntimeException("Not accepted to team");
            }
        }
    }

    private void serverHandleTopicOnly(HostedConnection source,
            CmdTopicOnly command) {
        ServerSender sender = stateManager.getState(ServerSender.class);
        common.serverHandleTopicOnlyCommand(source, command);
        switch (command.getTopicId()) {
            case Topic.CLIENT_WORLD_CREATED:
                List<String> teamOpts = new ArrayList<>(teamNameId.keySet());
                sender.addCommandForSingle(
                        new CmdTeamOptions(teamOpts), source);
                break;
            case Topic.TEAM_STATISTICS_REQUEST:
                sender.addCommand(BattleStatisticsResponse
                        .buildTeamStatisticsResponse());
                break;
        }
    }

    @Override
    public void readUnreliable(Object source, Command unreliable) {
    }
}
