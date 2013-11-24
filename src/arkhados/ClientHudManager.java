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

import arkhados.controls.ActionQueueControl;
import arkhados.util.UserDataStrings;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
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
import java.util.List;

/**
 *
 * @author william
 */
public class ClientHudManager extends AbstractAppState implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Camera cam;
    private Node guiNode;
    private BitmapFont guiFont;
    private List<Node> characters = new ArrayList<Node>();
    private List<BitmapText> hpBars = new ArrayList<BitmapText>();
    private int currentSeconds = -1;

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
    }

    @Override
    public void update(float tpf) {
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

        // TODO: Show bottom bar
    }

    public void setSecondsLeftToStart(int seconds) {
//        Element layerCountdown = this.screen.findElementByName("layer_countdown");
//        if (!layerCountdown.isEnabled()) {
//            layerCountdown.enable();
//            layerCountdown.show();
//        }
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
        Vector3f hpBarLocation = this.cam.getScreenCoordinates(character.getLocalTranslation().add(0f, 20.0f, 0.0f)).add(-15f, 40f, 0f);
        hpBar.setLocalTranslation(hpBarLocation);
        hpBar.setText(String.format("%.0f", (Float) character.getUserData(UserDataStrings.HEALTH_CURRENT)));
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    public void bind(Nifty nifty, Screen screen) {
    }

    public void onStartScreen() {
        List<Element> layers = this.screen.getLayerElements();
        for (Element layer : layers) {
//            layer.disable();
//            layer.hideWithoutEffect();
        }
    }

    public void onEndScreen() {
    }
}
