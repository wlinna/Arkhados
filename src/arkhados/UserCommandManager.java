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

import arkhados.controls.CharacterHudControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.FreeCameraControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.messages.usercommands.UcCastSpellCommand;
import arkhados.messages.usercommands.UcMouseTargetCommand;
import arkhados.messages.usercommands.UcWalkDirection;
import arkhados.net.Sender;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.InputMappingStrings;
import arkhados.util.UserDataStrings;
import arkhados.util.ValueWrapper;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.Listener;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.NetworkClient;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;

/**
 *
 * @author william
 */
public class UserCommandManager extends AbstractAppState {

    private InputManager inputManager;
    private Sender sender;
    private WorldManager worldManager;
    private Application app;
    private Camera cam;
    private int playerId;
    private int characterId;
    private Node character;
    private int down = 0;
    private int right = 0;
    private boolean inputListenersActive = false;
    private float mouseTargetUpdateTimer = 0f;
    private Plane floorPlane = new Plane(Vector3f.UNIT_Y, 0f);
    private Vector3f mouseGroundPosition = new Vector3f();
    private HashMap<String, Boolean> movementKeyFlags = new HashMap<>(4);
    private Listener listener;

    public UserCommandManager(Sender sender, InputManager inputManager) {
        this.sender = sender;
        this.inputManager = inputManager;
        this.clearMovementFlags();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
        this.worldManager = stateManager.getState(WorldManager.class);
        this.listener = app.getListener();
        this.cam = app.getCamera();
    }
    private ActionListener actionCastSpell = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            InfluenceInterfaceControl influenceInterface = getCharacterInterface();
            if (influenceInterface == null || influenceInterface.isDead()) {
                return;
            }
            if (isPressed) {
                return;
            }

            calculateMouseGroundPosition();
            if (name != null) {
                sender.addCommand(
                        new UcCastSpellCommand(InputMappingStrings.getId(name),
                        mouseGroundPosition));
            }
        }
    };
    private ActionListener actionMoveDirection = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            InfluenceInterfaceControl influenceInterface = getCharacterInterface();
            if (influenceInterface == null || influenceInterface.isDead()) {
                return;
            }
            if (movementKeyFlags.get(name) == false && !isPressed) {
                return;
            }

            movementKeyFlags.put(name, isPressed);

            if ("move-right".equals(name)) {
                UserCommandManager.this.right += isPressed ? 1 : -1;
            } else if ("move-left".equals(name)) {
                UserCommandManager.this.right += isPressed ? -1 : 1;
            } else if ("move-up".equals(name)) {
                UserCommandManager.this.down += isPressed ? -1 : 1;
            } else if ("move-down".equals(name)) {
                UserCommandManager.this.down += isPressed ? 1 : -1;
            }

            sendWalkDirection();
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
                    InputMappingStrings.MOVE_RIGHT, InputMappingStrings.MOVE_LEFT,
                    InputMappingStrings.MOVE_UP, InputMappingStrings.MOVE_DOWN);
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
        this.listener.setLocation(character.getWorldTranslation());
        this.mouseTargetUpdateTimer -= tpf;
        if (this.mouseTargetUpdateTimer <= 0f) {
            this.calculateMouseGroundPosition();
            this.sender.addCommand(new UcMouseTargetCommand(this.mouseGroundPosition));
            this.mouseTargetUpdateTimer = 0.075f;
        }
    }

    public void followPlayer() {
        Node camNode = new Node("cam-node");
        this.worldManager.getWorldRoot().attachChild(camNode);
//        camNode.addControl(new FollowCharacterControl(this.character, this.cam));
        camNode.addControl(new FreeCameraControl(this.character, this.cam, this.inputManager));
        camNode.getControl(FreeCameraControl.class).setRelativePosition(new Vector3f(0f, 150f, 30f));
    }

    public void sendWalkDirection() {
        this.sender.addCommand(new UcWalkDirection(down, right));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.down = 0;
        this.right = 0;

        if (enabled) {
            this.enableInputListeners();
        } else {
            this.disableInputListeners();
            this.clearMovementFlags();
        }
    }

    private void clearMovementFlags() {
        this.movementKeyFlags.put("move-up", false);
        this.movementKeyFlags.put("move-down", false);
        this.movementKeyFlags.put("move-left", false);
        this.movementKeyFlags.put("move-right", false);
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

    public Spatial getCharacter() {
        if (this.character == null) {
            Spatial spatial = this.worldManager.getEntity(this.characterId);
            if (spatial != null) {
                this.trySetPlayersCharacter(spatial);
            }
            return spatial;
        }
        return this.character;
    }

    public void nullifyCharacter() {
        this.character = null;
    }

    private InfluenceInterfaceControl getCharacterInterface() {
        Spatial spatial = this.getCharacter();
        if (spatial == null) {
            return null;
        }
        return spatial.getControl(InfluenceInterfaceControl.class);
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    public boolean trySetPlayersCharacter(Spatial spatial) {
        // FIXME: NullPointerException
        if ((Integer) spatial.getUserData(UserDataStrings.ENTITY_ID) == this.characterId) {
            this.character = (Node) spatial;
            ClientHudManager hudManager = this.app.getStateManager().getState(ClientHudManager.class);
            this.character.getControl(CharacterHudControl.class).setHudManager(hudManager);
            this.followPlayer();

            return true;
        }
        return false;
    }

    public void onLoseFocus() {
        if (!super.isEnabled()) {
            return;
        }

        this.sender.addCommand(new UcWalkDirection(0, 0));
    }
}