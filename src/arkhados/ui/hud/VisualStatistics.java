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
package arkhados.ui.hud;

import arkhados.Globals;
import arkhados.PlayerData;
import arkhados.Topic;
import arkhados.messages.CmdTopicOnly;
import arkhados.net.Sender;
import arkhados.util.InputMapping;
import arkhados.util.PlayerRoundStats;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.Color;
import java.util.ArrayList;
import java.util.List;

public class VisualStatistics implements ActionListener {

    static enum State {

        TEAM, INDIVIDUAL, NONE
    }

    private Nifty nifty;
    private Screen screen;
    private AppStateManager stateManager;
    private final List<Element> statisticsPanels = new ArrayList<>();
    private List<PlayerRoundStats> latestStatsList = null;
    private State currentState;
    private boolean teamGame;

    void initialize(Application app) {
        stateManager = app.getStateManager();
        app.getInputManager().addMapping(InputMapping.TOGGLE_STATS,
                new KeyTrigger(KeyInput.KEY_TAB));
        app.getInputManager().addListener(this,
                InputMapping.TOGGLE_STATS);
    }

    void setLatestStatsList(List<PlayerRoundStats> latestStatsList,
            boolean teamStats) {
        this.latestStatsList = latestStatsList;
        Element statisticsLayer = screen.findElementById("layer_statistics");

        if (statisticsLayer.isVisible()) {
            update(teamStats);
        }
    }

    private void show(State type) {
        Sender sender = stateManager.getState(Sender.class);
        switch (type) {
            case TEAM:
                sender.addCommand(
                        new CmdTopicOnly(Topic.TEAM_STATISTICS_REQUEST));
                break;
            case INDIVIDUAL:
                sender.addCommand(
                        new CmdTopicOnly(Topic.PLAYER_STATISTICS_REQUEST));
                break;
        }

        currentState = type;
        screen.findElementById("layer_statistics").show();
    }

    void show() {
        if (teamGame) {
            show(State.TEAM);
        } else {
            show(State.INDIVIDUAL);
        }
    }

    void hide() {
        screen.findElementById("layer_statistics").hideWithoutEffect();
    }

    private void initializeStatisticsPanels() {
        Element statisticsPanel = screen.findElementById("panel_statistics");
        
        int i;
        for (i = 0; i < latestStatsList.size(); ++i) {
            if (i < statisticsPanels.size()) {
                statisticsPanels.get(i).show();
            } else {
                statisticsPanels.add(new PlayerStatisticsPanelBuilder(i)
                        .build(nifty, screen, statisticsPanel));
            }
        }

        for (; i < statisticsPanels.size(); i++) {
            statisticsPanels.get(i).hideWithoutEffect();

        }
    }

    private void update(boolean teamStats) {
        if (latestStatsList == null) {
            return;
        }

        initializeStatisticsPanels();

        Element root = screen.findElementById("panel_statistics");
        for (int i = 0; i < latestStatsList.size(); i++) {
            PlayerRoundStats stats = latestStatsList.get(i);                          
            insertStats(root, i, stats, teamStats);
        }
    }

    private void insertStats(Element root, int index, PlayerRoundStats stats,
            boolean team) {

        String name;
        int playerId = stats.playerId;
        int teamId;
        if (team) {
            teamId = playerId;
            name = VisualCharacterInfo.TEAM_NAMES[teamId];
        } else {
            teamId = PlayerData.getIntData(playerId, PlayerData.TEAM_ID);
            name = PlayerData.getStringData(playerId, PlayerData.NAME);
        }

        ColorRGBA rgba = VisualCharacterInfo.TEAM_COLORS[teamId];

        Color teamColor = new Color(rgba.r, rgba.g, rgba.b, rgba.a);

        Element eName = root.findElementById(index + "-name");
        Element eDamage = root.findElementById(index + "-damage");
        Element eRestoration = root.findElementById(index + "-restoration");
        Element eKills = root.findElementById(index + "-kills");

        // FIXME: NullPointerError happens here
        // Possibly related problem is that after this nifty complains
        // about possibly conflicting ids.
        TextRenderer rName = eName.getRenderer(TextRenderer.class);
        rName.setText(name);
        rName.setColor(teamColor);
        
        TextRenderer rDamage = eDamage.getRenderer(TextRenderer.class);
        rDamage.setText(String.format("%d", (int) stats.damageDone));
        rDamage.setColor(teamColor);
        
        TextRenderer rResto = eRestoration.getRenderer(TextRenderer.class);
        rResto.setText(String.format("%d", (int) stats.healthRestored));
        rResto.setColor(teamColor);
        
        TextRenderer rKills = eKills.getRenderer(TextRenderer.class);
        rKills.setText(String.format("%d", stats.kills));
        rKills.setColor(teamColor);        
    }

    void clean() {
        for (Element element : statisticsPanels) {
            element.markForRemoval();
        }
        statisticsPanels.clear();
    }

    void setNifty(Nifty nifty) {
        this.nifty = nifty;
    }

    void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed || !isEnabled()) {
            return;
        }

        Globals.app.enqueue(() -> {
            Element layer = screen.findElementById("layer_statistics");
            if (!layer.isVisible() && teamGame) {
                show(State.TEAM);
            } else if ((!layer.isVisible() && !teamGame)
                    || currentState == State.TEAM) {
                show(State.INDIVIDUAL);
            } else {
                currentState = State.NONE;
                hide();
            }
            return null;
        });
    }

    void setTeamGame(boolean teamGame) {
        this.teamGame = teamGame;
    }

    private boolean isEnabled() {
        return nifty.getCurrentScreen() == screen;
    }
}
