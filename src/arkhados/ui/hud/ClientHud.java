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
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterHud;
import arkhados.util.InputMappingStrings;
import arkhados.util.NiftyUtils;
import arkhados.util.PlayerRoundStats;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.Color;
import java.util.List;
import java.util.concurrent.Callable;

public class ClientHud extends AbstractAppState
        implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Spatial playerCharacter = null;
    private GameMessageHandler messageHandler = new GameMessageHandler();
    private SpellBar spellBar = new SpellBar();
    private VisualStatistics statistics = new VisualStatistics();
    private VisualCharacterInfo characterInfo;
    private HudMenu hudMenu = new HudMenu();

    public ClientHud(Camera cam, Node guiNode, BitmapFont guiFont) {
        characterInfo = new VisualCharacterInfo(cam, guiNode, guiFont);
        guiNode.addControl(new CActionQueue());
    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;
        screen = nifty.getScreen("default_hud");
        spellBar.setNifty(nifty);
        spellBar.setScreen(screen);
        statistics.setNifty(nifty);
        statistics.setScreen(screen);

        hudMenu.initialize(nifty, screen);
        Globals.app.getInputManager().addListener(hudMenu,
                InputMappingStrings.HUD_TOGGLE_MENU);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        statistics.initialize(app);
    }

    public void newOwnCharacter(Spatial newCharacter, boolean differentEntity) {
        playerCharacter = newCharacter;

        spellBar.setPlayerCharacter(newCharacter);
        if (differentEntity) {
            spellBar.clean();
            spellBar.setPlayerCharacter(newCharacter);
            spellBar.loadIcons();
        } else {
            spellBar.setPlayerCharacter(newCharacter);
        }
    }

    @Override
    public void update(float tpf) {
        if (playerCharacter != null) {
            spellBar.updateIcons();
        }

        characterInfo.update();
    }

    public void addCharacter(Spatial character) {
        characterInfo.addCharacter(character);
    }

    public void clear() {

        characterInfo.clean();

        clearAllButCharactersInfo();
    }

    public void clearAllButCharactersInfo() {
        playerCharacter = null;

        NiftyUtils.removeChildren(screen, "panel_buffs");
        spellBar.clean();

        hideStatistics();
    }

    public void entityDisappeared(Spatial spatial) {
        if (!(spatial instanceof Node)) {
            return;
        }

        characterInfo.removeCharacter((Node) spatial);

        if (spatial == playerCharacter) {
            playerCharacter = null;
            spellBar.setPlayerCharacter(null);
        }
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        messageHandler.initialize(nifty);
        messageHandler.createRows(10);
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    public void showStatistics() {
        statistics.show();
    }

    public void hideStatistics() {
        statistics.hide();
    }

    public void endGame() {
        clear();
        nifty.gotoScreen("main_menu");
    }

    public void clearBuffIcons() {
        NiftyUtils.removeChildren(screen, "panel_buffs");
    }

    public Nifty getNifty() {
        return nifty;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setLatestStatsList(final List<PlayerRoundStats> statsList) {
        Globals.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                statistics.setLatestStatsList(statsList);
                return null;
            }
        });
    }

    public void disableCharacterHudControl() {
        if (playerCharacter == null) {
            return;
        }

        playerCharacter.getControl(CCharacterHud.class).setEnabled(false);
    }

    public void addMessage(String message) {
        messageHandler.addMessage(message, Color.WHITE);
    }

    /**
     * TODO: This is dirty temporary solution to clear messages.
     */
    public void clearMessages() {
        messageHandler.cleanup();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        Globals.app.getInputManager().removeListener(hudMenu);
        hudMenu.cleanup();
        clear();
    }

    public void continueGame() {
        screen.findElementByName("layer_settings").hide();
    }

    public void exitProgram() {
        Globals.app.stop();
    }
}