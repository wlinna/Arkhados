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
import arkhados.controls.SpellCastControl;
import arkhados.util.InputMappingStrings;
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
import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.dynamic.ImageCreator;
import de.lessvoid.nifty.controls.dynamic.PanelCreator;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private HashMap<String, Element> spellIcons = new HashMap<String, Element>(6);
    private Spatial playerCharacter = null;
    private AppStateManager stateManager;

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

    private void loadSpellIcons() {
        final SpellCastControl castControl = this.playerCharacter.getControl(SpellCastControl.class);
        final Element bottomPanel = this.screen.findElementByName("panel_bottom");
        final String placeHolderPath = "Interface/Images/SpellIcons/placeholder.png";
        final String m1 = castControl.getKeySpellNameMapping(InputMappingStrings.M1).getName();
        final String m2 = castControl.getKeySpellNameMapping(InputMappingStrings.M2).getName();
        final String q = castControl.getKeySpellNameMapping(InputMappingStrings.Q).getName();
        final String e = castControl.getKeySpellNameMapping(InputMappingStrings.E).getName();
        final String r = castControl.getKeySpellNameMapping(InputMappingStrings.R).getName();
        final String space = castControl.getKeySpellNameMapping(InputMappingStrings.SPACE).getName();
        this.spellIcons.put(m1, new SpellIconBuilder(m1, placeHolderPath).build(nifty, screen, bottomPanel));
        this.spellIcons.put(m2, new SpellIconBuilder(m2, placeHolderPath).build(nifty, screen, bottomPanel));
        this.spellIcons.put(q, new SpellIconBuilder(q, placeHolderPath).build(nifty, screen, bottomPanel));
        this.spellIcons.put(e, new SpellIconBuilder(e, placeHolderPath).build(nifty, screen, bottomPanel));
        this.spellIcons.put(r, new SpellIconBuilder(r, placeHolderPath).build(nifty, screen, bottomPanel));
        this.spellIcons.put(space, new SpellIconBuilder(space, placeHolderPath).build(nifty, screen, bottomPanel));
    }

    private void updateSpellIcons() {
        final SpellCastControl castControl = this.playerCharacter.getControl(SpellCastControl.class);

        for (Map.Entry<String, Element> entry : this.spellIcons.entrySet()) {
            float cooldown = castControl.getCooldown(entry.getKey());
            Element overlay = entry.getValue().findElementByName(entry.getKey() + "-overlay");
            if (cooldown <= 0) {
                if (overlay.isVisible()) {
                    overlay.hide();
                }
            } else {
                if (!overlay.isVisible()) {
                    overlay.show();
                }
            }
        }
    }
}

class SpellIconBuilder extends ImageBuilder {

    private static Color overlayColor = new Color(0f, 0f, 0f, 0.8f);

    public SpellIconBuilder(final String id, final String path) {
        super(id);
        super.valignCenter();
        super.alignCenter();
        super.height("64px");
        super.width("64px");
        super.marginLeft("12px");
        super.filename(path);
        super.childLayoutOverlay();
        super.panel(new PanelBuilder() {
            {
                super.id(id + "-overlay");
                super.height("64px");
                super.width("64px");
                super.backgroundColor(overlayColor);

//                super.text(new TextBuilder() {
//                    {
//                        super.id(id + "-counter");
//
//                    }
//                });
            }
        });
    }
}