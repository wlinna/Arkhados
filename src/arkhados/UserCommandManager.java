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

import arkhados.controls.CCharacterHud;
import arkhados.controls.CFreeCamera;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.effects.DeathManager;
import arkhados.messages.usercommands.CmdUcCastSpell;
import arkhados.messages.usercommands.CmdUcMouseTarget;
import arkhados.messages.usercommands.CmdUcWalkDirection;
import arkhados.net.Sender;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.InputMappingStrings;
import arkhados.util.UserDataStrings;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.Listener;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
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
    private boolean characterChanged = false;
    private Listener listener;
    private boolean modifierFlag = false;

    public UserCommandManager(InputManager inputManager) {
        this.inputManager = inputManager;
        clearMovementFlags();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
        worldManager = stateManager.getState(WorldManager.class);
        listener = app.getListener();
        cam = app.getCamera();

        inputManager.addListener(actionMoveDirection,
                InputMappingStrings.MOVE_RIGHT, InputMappingStrings.MOVE_LEFT,
                InputMappingStrings.MOVE_UP, InputMappingStrings.MOVE_DOWN);

        inputManager
                .addListener(modifierListener, InputMappingStrings.MODIFIER);
    }

    public void createCameraControl() {
        Node camNode = new Node("cam-node");
        worldManager.getWorldRoot().attachChild(camNode);
        CFreeCamera cameraControl =
                new CFreeCamera(cam, inputManager);
        camNode.addControl(cameraControl);
        cameraControl.setRelativePosition(new Vector3f(0f, 150f, 30f));
    }
    private ActionListener modifierListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            modifierFlag = isPressed;
        }
    };
    private ActionListener actionCastSpell = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            CInfluenceInterface influenceInterface =
                    getCharacterInterface();
            if (influenceInterface == null || influenceInterface.isDead()) {
                return;
            }
            if (isPressed) {
                return;
            }

            calculateMouseGroundPosition();

            if (InputMappingStrings.SEC1.equals(name)
                    || InputMappingStrings.SEC2.equals(name)) {
                Integer input = getCharacter().getControl(CSpellCast.class)
                        .getInput(name);
                if (input != null) {
                    // In this case we just ignore the modifier
                    CmdUcCastSpell uc = new CmdUcCastSpell(input, false,
                            mouseGroundPosition);
                    app.getStateManager().getState(Sender.class).addCommand(uc);
                }
            }

            if (name != null) {
                CmdUcCastSpell uc =
                        new CmdUcCastSpell(InputMappingStrings.getId(name),
                        modifierFlag, mouseGroundPosition);
                app.getStateManager().getState(Sender.class).addCommand(uc);
            }
        }
    };
    private ActionListener actionMoveDirection = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (movementKeyFlags.get(name) == false && !isPressed) {
                return;
            }

            movementKeyFlags.put(name, isPressed);

            if ("move-right".equals(name)) {
                right += isPressed ? 1 : -1;
            } else if ("move-left".equals(name)) {
                right += isPressed ? -1 : 1;
            } else if ("move-up".equals(name)) {
                down += isPressed ? -1 : 1;
            } else if ("move-down".equals(name)) {
                down += isPressed ? 1 : -1;
            }

            sendWalkDirection();
        }
    };

    private void disableInputListeners() {
        if (inputListenersActive) {
            inputManager.removeListener(actionCastSpell);
        }
        inputListenersActive = false;
    }

    private void enableInputListeners() {
        if (!inputListenersActive) {
            inputManager.addListener(actionCastSpell,
                    InputMappingStrings.M1, InputMappingStrings.M2,
                    InputMappingStrings.Q, InputMappingStrings.E,
                    InputMappingStrings.R, InputMappingStrings.SPACE,
                    InputMappingStrings.SEC1, InputMappingStrings.SEC2);
        }

        inputListenersActive = true;
    }

    @Override
    public void update(float tpf) {
        if (!inputListenersActive) {
            return;
        }

        Spatial localCharacter = getCharacter();
        if (localCharacter == null) {
            return;
        }

        listener.setLocation(localCharacter.getWorldTranslation());
        mouseTargetUpdateTimer -= tpf;
        if (mouseTargetUpdateTimer <= 0f) {
            calculateMouseGroundPosition();
            app.getStateManager().getState(Sender.class).addCommand(
                    new CmdUcMouseTarget(mouseGroundPosition));
            mouseTargetUpdateTimer = 0.075f;
        }
    }

    public void followPlayer() {
        worldManager.getWorldRoot().getChild("cam-node")
                .getControl(CFreeCamera.class).setCharacter(character);
    }

    public void followSpatial(Spatial spatial) {
        worldManager.getWorldRoot().getChild("cam-node")
                .getControl(CFreeCamera.class).setCharacter(spatial);
    }

    private void sendWalkDirection() {
        app.getStateManager().getState(Sender.class)
                .addCommand(new CmdUcWalkDirection(down, right));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            enableInputListeners();
        } else {
            disableInputListeners();
        }
    }

    private void clearMovementFlags() {
        movementKeyFlags.put("move-up", false);
        movementKeyFlags.put("move-down", false);
        movementKeyFlags.put("move-left", false);
        movementKeyFlags.put("move-right", false);
    }

    private void calculateMouseGroundPosition() {
        Vector2f mouse2dPosition = inputManager.getCursorPosition();
        Vector3f mouse3dPosition = cam
                .getWorldCoordinates(mouse2dPosition, 0.0f);

        Vector3f rayDirection = cam
                .getWorldCoordinates(mouse2dPosition, 1.0f)
                .subtractLocal(mouse3dPosition).normalizeLocal();

        Ray ray = new Ray(mouse3dPosition, rayDirection);
        ray.intersectsWherePlane(floorPlane, mouseGroundPosition);
    }

    public Spatial getCharacter() {
        if (character == null) {
            Spatial spatial = worldManager.getEntity(characterId);
            if (spatial != null) {
                trySetPlayersCharacter(spatial);
            }
            return spatial;
        }
        return character;
    }

    public void nullifyCharacter() {
        character = null;
    }

    private CInfluenceInterface getCharacterInterface() {
        Spatial spatial = getCharacter();
        if (spatial == null) {
            return null;
        }
        return spatial.getControl(CInfluenceInterface.class);
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getCharacterId() {
        return characterId;
    }

    public void setCharacterId(int characterId) {
        this.characterId = characterId;
        characterChanged = true;
    }

    public boolean trySetPlayersCharacter(Spatial spatial) {
        if (!spatial.getUserData(UserDataStrings.ENTITY_ID)
                .equals(characterId)) {
            return false;
        }

        app.getStateManager().getState(DeathManager.class).revive();
        character = (Node) spatial;

        if (characterChanged) {
            character.getControl(CSpellCast.class).thisIsOwnedByClient();
        } else {
            character.getControl(CSpellCast.class).restoreClientCooldowns();
        }

        ClientHudManager hudManager = app.getStateManager()
                .getState(ClientHudManager.class);
        hudManager.newOwnCharacter(spatial, characterChanged);
        hudManager.clearBuffIcons();
        hudManager.hideStatistics();

        character.getControl(CCharacterHud.class).setHudManager(hudManager);
        followPlayer();
        characterChanged = false;
        return true;
    }

    public void onLoseFocus() {
        if (!isEnabled()) {
            return;
        }

        app.getStateManager().getState(Sender.class)
                .addCommand(new CmdUcWalkDirection(down, right));
    }
}