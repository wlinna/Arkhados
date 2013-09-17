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

import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.FollowCharacterControl;
import arkhados.controls.FreeCameraControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.messages.usercommands.UcCastSpellMessage;
import arkhados.messages.usercommands.UcMouseTargetMessage;
import arkhados.messages.usercommands.UcWalkDirection;
import arkhados.util.InputMappingStrings;
import arkhados.util.UserDataStrings;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;

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
    private Node character;
    private int down = 0;
    private int right = 0;
    private HashMap<String, String> keySpellMappings = new HashMap<String, String>(6);
    private boolean inputListenersActive = false;
    private float mouseTargetUpdateTimer = 0f;
    private Plane floorPlane = new Plane(Vector3f.UNIT_Y, 0f);
    private Vector3f mouseGroundPosition = new Vector3f();

    public UserCommandManager(Client client, InputManager inputManager) {
        this.client = client;
        this.inputManager = inputManager;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        System.out.println("Initializing UserCommandManager");
        this.app = app;
        this.worldManager = stateManager.getState(WorldManager.class);
        this.cam = app.getCamera();
        System.out.println("Initialized UserCommandManager");
    }

    public void addKeySpellMapping(String key, String spellName) {
        this.keySpellMappings.put(key, spellName);
    }
    private ActionListener actionCastSpell = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (UserCommandManager.this.getCharacterInterface().isDead()) {
                return;
            }
            if (isPressed) {
                return;
            }

//            Vector3f clickLocation = getClickLocation();
            calculateMouseGroundPosition();
            String spellName = keySpellMappings.get(name);
            if (spellName != null) {
                UserCommandManager.this.client.send(
                        new UcCastSpellMessage(spellName, UserCommandManager.this.mouseGroundPosition));
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
            this.inputManager.removeListener(this.actionCastSpell);
        }
        this.inputListenersActive = false;
    }

    private void enableInputListeners() {
        if (!this.inputListenersActive) {
            this.inputManager.addListener(this.actionMoveDirection,
                    InputMappingStrings.MOVE_RIGHT, InputMappingStrings.MOVE_LEFT, InputMappingStrings.MOVE_UP, InputMappingStrings.MOVE_DOWN);
            this.inputManager.addListener(this.actionCastSpell,
                    InputMappingStrings.M1, InputMappingStrings.M2,
                    InputMappingStrings.Q, InputMappingStrings.E,
                    InputMappingStrings.R, InputMappingStrings.SPACE);
        }

        this.inputListenersActive = true;
    }

    @Override
    public void update(float tpf) {
        if (!this.inputListenersActive) {
            return;
        }
        Spatial character = this.getCharacter();
        if (character == null) {
            return;
        }
        if (character.getControl(CharacterPhysicsControl.class).getWalkDirection().equals(Vector3f.ZERO)) {
            this.mouseTargetUpdateTimer -= tpf;
            if (this.mouseTargetUpdateTimer <= 0f) {
                this.calculateMouseGroundPosition();
                this.client.send(new UcMouseTargetMessage(this.mouseGroundPosition));
                this.mouseTargetUpdateTimer = 0.1f;
            }
        }
    }

    public void followPlayer() {
        Node camNode = new Node("cam-node");
        this.worldManager.getWorldRoot().attachChild(camNode);
//        camNode.addControl(new FollowCharacterControl(this.character, this.cam));
        camNode.addControl(new FreeCameraControl(this.character, this.cam, this.inputManager));
        camNode.getControl(FreeCameraControl.class).setRelativePosition(new Vector3f(0f, 150f, 30f));
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

    private void calculateMouseGroundPosition() {
        final Vector2f mouse2dPosition = this.inputManager.getCursorPosition();
        final Vector3f mouse3dPosition = this.cam
                .getWorldCoordinates(mouse2dPosition, 0.0f);


        final Vector3f rayDirection = this.cam
                .getWorldCoordinates(mouse2dPosition, 1.0f)
                .subtractLocal(mouse3dPosition).normalizeLocal();

        Ray ray = new Ray(mouse3dPosition, rayDirection);
        boolean intersects = ray.intersectsWherePlane(this.floorPlane, this.mouseGroundPosition);
    }

    private Spatial getCharacter() {
        if (this.character == null) {
            Spatial spatial = this.worldManager.getEntity(this.characterId);
            if (spatial != null) {
                this.trySetPlayersCharacter(spatial);
            }
            return spatial;
        }
        return this.character;
    }

    private InfluenceInterfaceControl getCharacterInterface() {
        // FIXME: Sometimes NullPointerException occurs here
        return this.getCharacter().getControl(InfluenceInterfaceControl.class);
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

    public boolean trySetPlayersCharacter(Spatial spatial) {
        // FIXME: NullPointerException
        if ((Long) spatial.getUserData(UserDataStrings.ENTITY_ID) == this.characterId) {
            this.character = (Node) spatial;
            this.followPlayer();

            return true;
        }
        return false;
    }

    public void onLoseFocus() {
        if (!super.isEnabled()) {
            return;
        }
//        this.down = 0;
//        this.right = 0;
        if (this.client != null && this.client.isConnected()) {
            this.client.send(new UcWalkDirection(0, 0));
        }
    }
}