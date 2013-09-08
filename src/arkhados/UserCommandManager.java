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
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.messages.usercommands.UcCastSpellMessage;
import arkhados.messages.usercommands.UcWalkDirection;
import arkhados.util.UserDataStrings;

/**
 *
 * @author william
 */
public class UserCommandManager extends AbstractAppState {

    private InputManager inputManager;
    private Client client;
    private WorldManager worldManager;
    private Application app;
    // TODO: Get character somewhere
    private Camera cam;
    private long playerId;
    private long characterId;
    private int down = 0;
    private int right = 0;

//    private HashMap<InputListener, Boolean> inputListeners = new HashMap<InputListener, Boolean>();
    private boolean inputListenersActive = false;



    public UserCommandManager(Client client, InputManager inputManager) {
        this.client = client;
        this.inputManager = inputManager;
        this.initInputMappings();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        System.out.println("Initializing UserCommandManager");
        //this is called on the OpenGL thread after the AppState has been attached
        this.app = app;
        this.worldManager = stateManager.getState(WorldManager.class);
        this.cam = app.getCamera();
        System.out.println("Initialized UserCommandManager");
    }

    private void initInputMappings() {
        this.inputManager.addMapping("move-right", new KeyTrigger(KeyInput.KEY_D));
        this.inputManager.addMapping("move-left", new KeyTrigger(KeyInput.KEY_A));
        this.inputManager.addMapping("move-up", new KeyTrigger(KeyInput.KEY_W));
        this.inputManager.addMapping("move-down", new KeyTrigger(KeyInput.KEY_S));

        this.inputManager.addMapping("cast-spell-1", new KeyTrigger(KeyInput.KEY_Q));
        this.inputManager.addMapping("cast-fireball", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    }
    private ActionListener actionCastFireball = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (UserCommandManager.this.getCharacterInterface().isDead()) {
                return;
            }
            if (isPressed) {
                return;
            }

            Vector3f clickLocation = getClickLocation();
            if (clickLocation != null) {
                UserCommandManager.this.client.send(new UcCastSpellMessage("Fireball", clickLocation));
            }
        }
    };
    private ActionListener actionCastSpell1 = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (UserCommandManager.this.getCharacterInterface().isDead()) {
                return;
            }
            if (!isPressed) {
                Vector3f clickLocation = getClickLocation();
                if (clickLocation != null) {
                    UserCommandManager.this.client.send(new UcCastSpellMessage("Ember Circle", clickLocation));
                }
            }
        }
    };
    private ActionListener actionMoveDirection = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (getCharacterInterface().isDead()) {
                return;
            }
            if ("move-right".equals(name)) {
                UserCommandManager.this.right += isPressed ? 1 : -1;
            } else if ("move-left".equals(name)) {
                UserCommandManager.this.right += isPressed ? -1 : 1;
            } else if ("move-up".equals(name)) {
                UserCommandManager.this.down += isPressed ? -1 : 1;
            } else if ("move-down".equals(name)) {
                UserCommandManager.this.down += isPressed ? 1 : -1;
            }
            CharacterPhysicsControl characterPhysics = getCharacter().getControl(CharacterPhysicsControl.class);
            Float speedMovement = getCharacter().getUserData(UserDataStrings.SPEED_MOVEMENT);
            Vector3f walkDirection = new Vector3f(right, 0f, down).normalizeLocal().multLocal(speedMovement);
            characterPhysics.setWalkDirection(walkDirection);
            if (walkDirection.lengthSquared() > 0f) {
                characterPhysics.setViewDirection(walkDirection);
            }
            UserCommandManager.this.client.send(new UcWalkDirection(down, right));
        }
    };

    private void disableInputListeners() {
        if (this.inputListenersActive) {
            this.inputManager.removeListener(this.actionMoveDirection);
            this.inputManager.removeListener(this.actionCastFireball);
            this.inputManager.removeListener(this.actionCastSpell1);
        }
        this.inputListenersActive = false;
    }

    private void enableInputListeners() {
        if (!this.inputListenersActive) {
            // FIXME: Sometimes this throws NullPointerException when round starts
            this.inputManager.addListener(this.actionMoveDirection, "move-right", "move-left", "move-up", "move-down");
            this.inputManager.addListener(this.actionCastFireball, "cast-fireball");
            this.inputManager.addListener(this.actionCastSpell1, "cast-spell-1");
        }

        this.inputListenersActive = true;
    }

    private void moveX(int amount) {
    }

    private void moveY(int amount) {
    }

    @Override
    public void update(float tpf) {
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            this.enableInputListeners();
        } else {
            this.disableInputListeners();
        }
    }

    private Vector3f getClickLocation() {
        CollisionResults collisionResults = new CollisionResults();

        final Vector2f mouse2dPosition = this.inputManager.getCursorPosition();
        final Vector3f mouse3dPosition = this.cam
                .getWorldCoordinates(mouse2dPosition, 0.0f);


        final Vector3f rayDirection = this.cam
                .getWorldCoordinates(mouse2dPosition, 1.0f)
                .subtractLocal(mouse3dPosition).normalizeLocal();

        Ray ray = new Ray(mouse3dPosition, rayDirection);
        this.worldManager.getWorldRoot().collideWith(ray, collisionResults);

        Vector3f contactPoint = null;
        if (collisionResults.size() > 0) {
            contactPoint = collisionResults
                    .getClosestCollision().getContactPoint();

        }
        return contactPoint;
    }

    private Spatial getCharacter() {
        return this.worldManager.getEntity(this.characterId);
    }

    private InfluenceInterfaceControl getCharacterInterface() {
        return this.worldManager.getEntity(this.characterId).getControl(InfluenceInterfaceControl.class);
    }

    public long getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public void setCharacterId(long characterId) {
        this.characterId = characterId;
    }
}