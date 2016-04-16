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
import arkhados.controls.CCharacterPhysics;
import arkhados.util.UserData;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.List;

public class VisualCharacterInfo {

    private final Camera cam;
    private final Node guiNode;
    private final BitmapFont guiFont;
    private final List<Node> characters = new ArrayList<>();
    private final List<Spatial> hpBars = new ArrayList<>();
    private final List<BitmapText> playerNames = new ArrayList<>();

    static final ColorRGBA[] TEAM_COLORS = new ColorRGBA[]{
        ColorRGBA.Blue, ColorRGBA.Red, ColorRGBA.Green, ColorRGBA.Black,
        ColorRGBA.Magenta, ColorRGBA.Yellow, ColorRGBA.Orange,
        ColorRGBA.Pink, ColorRGBA.White, ColorRGBA.Brown, ColorRGBA.Gray
    };

    static final String[] TEAM_NAMES = new String[]{
        "Blue", "Red", "Green", "Black",
        "Magenta", "Yellow", "Orange",
        "Pink", "White", "Brown", "Gray"
    };

    VisualCharacterInfo(Camera cam, Node guiNode, BitmapFont guiFont) {
        this.cam = cam;
        this.guiNode = guiNode;
        this.guiFont = guiFont;
    }

    void addCharacter(Spatial character) {
        characters.add((Node) character);
        createHpBar((Node) character);

        int playerId = character.getUserData(UserData.PLAYER_ID);
        String name = PlayerData.getStringData(playerId, PlayerData.NAME);
        int team = PlayerData.getIntData(playerId, PlayerData.TEAM_ID);
        team = Math.abs(team); // If -1 for some reason

        createPlayerName(name, team);
    }

    void removeCharacter(Node node) {
        int index = characters.indexOf(node);

        if (index != -1) {
            Spatial hpBar = hpBars.get(index);
            hpBar.removeFromParent();
            hpBars.remove(index);

            BitmapText playerName = playerNames.get(index);
            playerName.removeFromParent();
            playerNames.remove(index);

            characters.remove(index);
        }
    }

    void clean() {
        characters.clear();
        clearHpBars();
        clearNames();
    }

    private void clearHpBars() {
        for (Spatial hpBar : hpBars) {
            hpBar.removeFromParent();
        }

        hpBars.clear();
    }

    private void clearNames() {
        for (BitmapText playerName : playerNames) {
            playerName.removeFromParent();
        }

        playerNames.clear();
    }

    void update() {
        for (int i = 0; i < characters.size(); ++i) {
            updateHpBar(i);
            updateText(i);
        }
    }

    private void createHpBar(Node character) {
        Quad quad = new Quad(80f, 10f);
        Geometry geom = new Geometry("hpbar", quad);
        Material mat = Globals.assets.loadMaterial("Materials/HealthBar.j3m");

        float health = character.getUserData(UserData.HEALTH_CURRENT);
        float healthMax = character.getUserData(UserData.HEALTH_MAX);
        float healthLowRecord = character
                .getUserData(UserData.HEALTH_LOW_RECORD);

        mat.setFloat("Health", health / healthMax);
        mat.setFloat("HealthLowRecord", healthLowRecord / healthMax);
        
        geom.setMaterial(mat);

        guiNode.attachChild(geom);
        geom.setQueueBucket(RenderQueue.Bucket.Gui);

        hpBars.add(geom);
    }

    private void createPlayerName(String name, int teamId) {
        BitmapText text = new BitmapText(guiFont);

        text.setSize(guiFont.getCharSet().getRenderedSize() * 0.8f);
        text.setBox(new Rectangle(0, 0, 80, 10));
        text.setText(name);
        text.setColor(TEAM_COLORS[teamId].clone());
        text.setAlignment(BitmapFont.Align.Center);
        text.center();
        guiNode.attachChild(text);
        text.setQueueBucket(RenderQueue.Bucket.Gui);
        playerNames.add(text);
    }

    private void updateHpBar(int index) {
        Node character = characters.get(index);
        Geometry hpBar = (Geometry) hpBars.get(index);
        Material mat = hpBar.getMaterial();
        float health = character.getUserData(UserData.HEALTH_CURRENT);
        float healthMax = character.getUserData(UserData.HEALTH_MAX);
        float healthLowRecord = character
                .getUserData(UserData.HEALTH_LOW_RECORD);
        if (health == 0) {
            mat.setFloat("Health", 0f);
            mat.setFloat("HealthLowRecord", 0f);
            return;
        }

        float altitude = character.getControl(CCharacterPhysics.class)
                .getCapsuleShape().getHeight();
        Vector3f hpBarLocation = cam.getScreenCoordinates(character
                .getLocalTranslation().add(0, altitude, 0)).add(-40, 40, 0);
        hpBar.setLocalTranslation(hpBarLocation);

        mat.setFloat("Health", health / healthMax);
        mat.setFloat("HealthLowRecord", healthLowRecord / healthMax);
    }

    private void updateText(int index) {
        Node character = characters.get(index);
        BitmapText name = playerNames.get(index);
        float height = name.getHeight();
        float altitude = character.getControl(CCharacterPhysics.class)
                .getCapsuleShape().getHeight();
        Vector3f textLocation = cam.getScreenCoordinates(character
                .getLocalTranslation().add(0, altitude, 0))
                .add(-40, 40 + 1.5f * height, 0);
        name.setLocalTranslation(textLocation);
    }
}
