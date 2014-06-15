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
import arkhados.UserCommandManager;
import arkhados.WorldManager;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.SpellCastControl;
import arkhados.messages.BattleStatisticsRequest;
import arkhados.spell.Spell;
import arkhados.util.InputMappingStrings;
import arkhados.util.PlayerRoundStats;
import arkhados.util.UserDataStrings;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author william
 */
// TODO: ClientHudManager is kind of messy and fragile. Please clean it up.
public class ClientHudManager extends AbstractAppState implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Camera cam;
    private Node guiNode;
    private BitmapFont guiFont;
    private List<Node> characters = new ArrayList<>();
    private List<BitmapText> hpBars = new ArrayList<>();
    private int currentSeconds = -1;
    private HashMap<String, Element> spellIcons = new HashMap<>(6);
    private Spatial playerCharacter = null;
    private AppStateManager stateManager;
    // HACK: This is only meant for initial implementation testing. Remove this when all round statistics are accessible via GUI
    private boolean roundTableCreated = false;
    private List<Element> statisticsPanels = new ArrayList<>();

    public ClientHudManager(Camera cam, Node guiNode, BitmapFont guiFont) {
        this.cam = cam;
        this.guiNode = guiNode;
        this.guiFont = guiFont;
        this.guiNode.addControl(new ActionQueueControl());
    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;
        this.screen = this.nifty.getScreen("default_hud");
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.cam = app.getCamera();
        this.stateManager = stateManager;
    }

    @Override
    public void update(float tpf) {
        if (playerCharacter == null) {
            UserCommandManager userCommandManager = this.stateManager.getState(UserCommandManager.class);
            this.playerCharacter = userCommandManager.getCharacter();
            if (this.playerCharacter != null) {
                this.loadSpellIcons();
            }
        } else {
            this.updateSpellIcons();
        }

        for (int i = 0; i < this.characters.size(); ++i) {
            this.updateHpBar(i);
        }
    }

    public void addCharacter(Spatial character) {
        // TODO: Add some checks
        this.characters.add((Node) character);
        this.createHpBar();
    }

    public void startRound() {
        Element layerCountdown = this.screen.findElementByName("layer_countdown");
        layerCountdown.disable();
        layerCountdown.hide();

        // TODO: Create statistics panel creation to more appropriate place
        // HACK: This is only meant for initial implementation testing. Remove this "if" when all round statistics are accessible via GUI
        if (!this.roundTableCreated) {
            this.roundTableCreated = true;
        }
    }

    public void setSecondsLeftToStart(int seconds) {
        if (this.currentSeconds == -1) {
            Element layerCountdown = this.screen.findElementByName("layer_countdown");

            layerCountdown.enable();
            layerCountdown.show();
        }
        if (seconds != currentSeconds) {
            this.currentSeconds = seconds;
            Element textElement = this.screen.findElementByName("text_countdown");
            textElement.getRenderer(TextRenderer.class).setText(Integer.toString(seconds));
        }
    }

    public void clear() {
        this.characters.clear();
        for (BitmapText hpBar : this.hpBars) {
            hpBar.removeFromParent();
        }
        this.hpBars.clear();
        this.currentSeconds = -1;
        this.playerCharacter = null;

        for (Iterator<Element> it = this.screen.findElementByName("panel_bottom")
                .getElements().iterator(); it.hasNext();) {

            Element element = it.next();
            this.nifty.removeElement(this.screen, element);
        }

        for (Iterator<Element> it = this.statisticsPanels.iterator(); it.hasNext();) {
            Element element = it.next();
            this.nifty.removeElement(this.screen, element);
        }

        statisticsPanels.clear();

        this.hideRoundStatistics();

        this.spellIcons.clear();
    }

    private void createHpBar() {
        BitmapText hpBar = new BitmapText(this.guiFont);

        hpBar.setSize(this.guiFont.getCharSet().getRenderedSize());
        hpBar.setBox(new Rectangle(0f, 0f, 40f, 10f));
        hpBar.setColor(ColorRGBA.Red);
        hpBar.setAlignment(BitmapFont.Align.Center);
        hpBar.center();
        this.guiNode.attachChild(hpBar);
        hpBar.setQueueBucket(RenderQueue.Bucket.Gui);
        this.hpBars.add(hpBar);
    }

    private void updateHpBar(int index) {
        Node character = this.characters.get(index);
        BitmapText hpBar = this.hpBars.get(index);
        float health = (Float) character.getUserData(UserDataStrings.HEALTH_CURRENT);
        if (health == 0.0f) {
            hpBar.setText("");
            return;
        }
        // TODO: Implement better method to get character's head's location
        Vector3f hpBarLocation = this.cam.getScreenCoordinates(
                character.getLocalTranslation().add(0f, 20.0f, 0.0f)).add(-15f, 40f, 0f);
        hpBar.setLocalTranslation(hpBarLocation);
        hpBar.setText(String.format("%.0f", (Float) character.getUserData(UserDataStrings.HEALTH_CURRENT)));
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    private void initializePlayerStatisticsPanels() {
        final Element statisticsPanel = this.screen.findElementByName("panel_statistics");
        assert statisticsPanel != null;
        final List<PlayerData> playerDataList = PlayerData.getPlayers();
        for (PlayerData playerData : playerDataList) {
            this.statisticsPanels.add(new PlayerStatisticsPanelBuilder(
                    playerData.getId()).build(nifty, screen, statisticsPanel));
        }
    }

    public void showRoundStatistics() {
        this.initializePlayerStatisticsPanels();
        final Client client = this.stateManager.getState(WorldManager.class).getSyncManager().getClient();
        client.send(new BattleStatisticsRequest());
        final Element statisticsLayer = this.screen.findElementByName("layer_statistics");
        statisticsLayer.show();
    }

    public void hideRoundStatistics() {
        final Element statisticsLayer = this.screen.findElementByName("layer_statistics");
        statisticsLayer.hideWithoutEffect();
    }

    public void updateStatistics(final List<PlayerRoundStats> playerRoundStatsList) {
        final Element statisticsPanel = this.screen.findElementByName("panel_statistics");
        for (PlayerRoundStats playerRoundStats : playerRoundStatsList) {

            final Element damagePanel = statisticsPanel.findElementByName(
                    playerRoundStats.playerId + "-damage");
            final Element restorationPanel = statisticsPanel.findElementByName(
                    playerRoundStats.playerId + "-restoration");
            final Element killsPanel = statisticsPanel.findElementByName(
                    playerRoundStats.playerId + "-kills");

            damagePanel.getRenderer(TextRenderer.class).setText(
                    String.format("%d", (int) playerRoundStats.damageDone));
            restorationPanel.getRenderer(TextRenderer.class).setText(
                    String.format("%d", (int) playerRoundStats.healthRestored));
            killsPanel.getRenderer(TextRenderer.class).setText(
                    String.format("%d", playerRoundStats.kills));
        }
    }

    public void endGame() {
        this.clear();
        this.nifty.gotoScreen("main_menu");

        this.clear();
        this.roundTableCreated = false;
    }

    private void loadSpellIcons() {
        this.addSpellIcon(InputMappingStrings.M1);
        this.addSpellIcon(InputMappingStrings.M2);
        this.addSpellIcon(InputMappingStrings.Q);
        this.addSpellIcon(InputMappingStrings.E);
        this.addSpellIcon(InputMappingStrings.R);
        this.addSpellIcon(InputMappingStrings.SPACE);
    }

    private void addSpellIcon(final String key) {
        final Element bottomPanel = this.screen.findElementByName("panel_bottom");
        final SpellCastControl castControl = this.playerCharacter.getControl(SpellCastControl.class);
        final Spell spell = castControl.getKeySpellNameMapping(key);
        String iconPath;
        if (spell.getIconName() != null) {
            iconPath = "Interface/Images/SpellIcons/" + spell.getIconName();
        } else {
            iconPath = "Interface/Images/SpellIcons/placeholder.png";
        }
        this.spellIcons.put(spell.getName(), new SpellIconBuilder(spell.getName(),
                iconPath).build(nifty, screen, bottomPanel));
    }

    private void updateSpellIcons() {
        if (this.playerCharacter == null) {
            return;
        }
        final SpellCastControl castControl = this.playerCharacter.getControl(SpellCastControl.class);

        for (Map.Entry<String, Element> entry : this.spellIcons.entrySet()) {
            float cooldown = castControl.getCooldown(Spell.getSpell(entry.getKey()).getId());
            Element overlay = entry.getValue().findElementByName(entry.getKey() + "-overlay");
            if (cooldown <= 0) {
                if (overlay.isVisible()) {
                    overlay.hide();
                }
            } else {
                if (!overlay.isVisible()) {
                    overlay.show();
                }

                Element cooldownText = overlay.findElementByName(entry.getKey() + "-counter");

                if (cooldown > 3) {
                    cooldownText.getRenderer(TextRenderer.class).setText(String.format("%d", (int) cooldown));
                } else {
                    cooldownText.getRenderer(TextRenderer.class).setText(String.format("%.1f", cooldown));
                }
            }
        }
    }

    public Nifty getNifty() {
        return nifty;
    }

    public Screen getScreen() {
        return screen;
    }
}