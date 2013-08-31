/*    This file is part of JMageBattle.

 JMageBattle is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 JMageBattle is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */
package magebattle;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import magebattle.controls.CharacterPhysicsControl;
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class ClientHudManager extends AbstractAppState {

    private float deltaY = 40.0f;
    private Camera cam;
    private Node guiNode;
    private BitmapFont guiFont;
    private List<Node> characters = new ArrayList<Node>();
    private List<BitmapText> hpBars = new ArrayList<BitmapText>();

    public ClientHudManager(Camera cam, Node guiNode, BitmapFont guiFont) {
        this.cam = cam;
        this.guiNode = guiNode;
        this.guiFont = guiFont;
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

    private void createHpBar() {
        BitmapText hpBar = new BitmapText(this.guiFont);

        hpBar.setSize(this.guiFont.getCharSet().getRenderedSize());
        hpBar.setBox(new Rectangle(0f, 0f, 30f, 10f));
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
}
