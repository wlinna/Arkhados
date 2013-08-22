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
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import magebattle.messages.usercommands.UcCastSpellMessage;
import magebattle.messages.usercommands.UcRunToMessage;

/**
 *
 * @author william
 */
public class UserCommandManager extends AbstractAppState {

    private InputManager inputManager;
    private Client client;
    private WorldManager worldManager;
    private Application app;
    private String selectedSpell = "";
    // TODO: Get character somewhere
    private Spatial character;
    private Camera cam;

    public UserCommandManager(Client client) {
        this.client = client;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        System.out.println("Initializing UserCommandManager");
        //this is called on the OpenGL thread after the AppState has been attached
        this.app = app;
        this.worldManager = stateManager.getState(WorldManager.class);
        this.inputManager = app.getInputManager();
        this.cam = app.getCamera();

        this.initUserInput();
        System.out.println("Initialized UserCommandManager");
    }

    private void initUserInput() {
        this.inputManager.addMapping("cast-or-move", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        this.inputManager.addMapping("select-spell-fireball", new KeyTrigger(KeyInput.KEY_Q));

        this.inputManager.addListener(this.actionCastOrMove, "cast-or-move");
        this.inputManager.addListener(this.actionSelectSpell, "select-spell-fireball");
    }
    private ActionListener actionCastOrMove = new ActionListener() {
        private void cast(final Vector3f contactPoint) {
            // TODO: Validate
            UserCommandManager.this.client.send(new UcCastSpellMessage("Fireball", contactPoint));
            UserCommandManager.this.selectedSpell = "";
        }

        private void move(final Vector3f contactPoint) {
            UserCommandManager.this.client.send(new UcRunToMessage(contactPoint));
        }

        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                return;
            }

            CollisionResults collisionResults = new CollisionResults();

            final Vector2f mouse2dPosition = UserCommandManager.this.inputManager.getCursorPosition();
            final Vector3f mouse3dPosition = UserCommandManager.this.cam
                    .getWorldCoordinates(mouse2dPosition, 0.0f);
            final Vector3f rayDirection = UserCommandManager.this.cam
                    .getWorldCoordinates(mouse2dPosition, 1.0f)
                    .subtractLocal(mouse3dPosition).normalizeLocal();

            Ray ray = new Ray(mouse3dPosition, rayDirection);
            UserCommandManager.this.worldManager.getWorldRoot()
                    .collideWith(ray, collisionResults);

            if (collisionResults.size() > 0) {
                Vector3f contactPoint = collisionResults
                        .getClosestCollision().getContactPoint();

                if ("".equals(UserCommandManager.this.selectedSpell)) {
                    this.move(contactPoint);
                } else {
                    this.cast(contactPoint);
                }

//                System.out.println(String.format("Clicked on %f %f %f", contactPoint.x, contactPoint.y, contactPoint.z));
            }



        }
    };
    private ActionListener actionSelectSpell = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if ("select-spell-fireball".equals(name)) {
                UserCommandManager.this.selectedSpell = "Fireball";
            }
        }
    };

    @Override
    public void update(float tpf) {
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
