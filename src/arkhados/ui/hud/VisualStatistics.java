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

import arkhados.PlayerData;
import arkhados.Topic;
import arkhados.messages.TopicOnlyCommand;
import arkhados.net.Sender;
import arkhados.util.InputMappingStrings;
import arkhados.util.PlayerRoundStats;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VisualStatistics implements ActionListener {

    private Nifty nifty;
    private Screen screen;
    private AppStateManager stateManager;
    private List<Element> statisticsPanels = new ArrayList<>();
    private List<PlayerRoundStats> latestRoundStatsList = null;

    void initialize(Application app) {
        stateManager = app.getStateManager();
        app.getInputManager().addMapping(InputMappingStrings.TOGGLE_STATS,
                new KeyTrigger(KeyInput.KEY_TAB));
        app.getInputManager().addListener(this,
                InputMappingStrings.TOGGLE_STATS);
    }

    void setLatestRoundStatsList(
            List<PlayerRoundStats> latestRoundStatsList) {
        this.latestRoundStatsList = latestRoundStatsList;
        Element statisticsLayer = screen.findElementByName("layer_statistics");

        if (statisticsLayer.isVisible()) {
            updateStatistics();
        }
    }

    void showRoundStatistics() {
        initializePlayerStatisticsPanels();
        Sender sender = stateManager.getState(Sender.class);
        sender.addCommand(
                new TopicOnlyCommand(Topic.BATTLE_STATISTICS_REQUEST));
        Element statisticsLayer = screen.findElementByName("layer_statistics");
        statisticsLayer.show();
    }

    void hideRoundStatistics() {
        Element statisticsLayer = screen.findElementByName("layer_statistics");
        statisticsLayer.hideWithoutEffect();
    }

    private void initializePlayerStatisticsPanels() {
        Element statisticsPanel = screen.findElementByName("panel_statistics");
        List<PlayerData> playerDataList = PlayerData.getPlayers();
        for (PlayerData playerData : playerDataList) {
            statisticsPanels.add(new PlayerStatisticsPanelBuilder(
                    playerData.getId()).build(nifty, screen, statisticsPanel));
        }
    }

    private void updateStatistics() {
        if (latestRoundStatsList == null) {
            return;
        }

        Element statisticsPanel = screen.findElementByName("panel_statistics");
        for (PlayerRoundStats playerRoundStats : latestRoundStatsList) {

            Element damagePanel = statisticsPanel.findElementByName(
                    playerRoundStats.playerId + "-damage");
            Element restorationPanel = statisticsPanel.findElementByName(
                    playerRoundStats.playerId + "-restoration");
            Element killsPanel = statisticsPanel.findElementByName(
                    playerRoundStats.playerId + "-kills");

            damagePanel.getRenderer(TextRenderer.class).setText(
                    String.format("%d", (int) playerRoundStats.damageDone));
            restorationPanel.getRenderer(TextRenderer.class).setText(
                    String.format("%d", (int) playerRoundStats.healthRestored));
            killsPanel.getRenderer(TextRenderer.class).setText(
                    String.format("%d", playerRoundStats.kills));
        }
    }

    void clean() {
        for (Iterator<Element> it = statisticsPanels.iterator();
                it.hasNext();) {
            Element element = it.next();
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

        Element layer = screen.findElementByName("layer_statistics");

        if (!layer.isVisible()) {
            showRoundStatistics();
        } else {
            hideRoundStatistics();
            clean();
        }
    }

    private boolean isEnabled() {
        return nifty.getCurrentScreen() == screen;
    }

}
