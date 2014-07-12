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

import arkhados.controls.EntityVariableControl;
import arkhados.controls.PlayerEntityAwareness;
import arkhados.messages.syncmessages.AddEntityCommand;
import arkhados.messages.syncmessages.RemoveEntityCommand;
import arkhados.net.Command;
import arkhados.net.ServerSender;
import arkhados.ui.hud.ServerClientDataStrings;
import arkhados.util.RemovalReasons;
import arkhados.util.UserDataStrings;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */
public class ServerFogManager extends AbstractAppState {

    private Application app;
    private final Map<PlayerEntityAwareness, HostedConnection> awarenessConnectionMap = new HashMap<>();
    private Node walls;
    private float checkTimer = 0;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
    }

    @Override
    public void update(float tpf) {
        checkTimer -= tpf;
        if (checkTimer > 0) {
            return;
        }

        checkTimer = Globals.DEFAULT_SYNC_FREQUENCY / 2f;

        for (PlayerEntityAwareness playerEntityAwareness : awarenessConnectionMap.keySet()) {
            playerEntityAwareness.update(tpf);
        }
    }

    public void addCommand(Spatial spatial, Command command) {
        ServerSender sender = app.getStateManager().getState(ServerSender.class);

        for (Map.Entry<PlayerEntityAwareness, HostedConnection> entry : awarenessConnectionMap.entrySet()) {
            PlayerEntityAwareness awareness = entry.getKey();
            if (awareness.isAwareOf(spatial)) {
                sender.addCommandForSingle(command, entry.getValue());
            }
        }
    }

    public void createNewEntity(Spatial spatial, Command command) {
        ServerSender sender = app.getStateManager().getState(ServerSender.class);

        PlayerEntityAwareness myAwareness = searchForAwareness(spatial);

        for (Map.Entry<PlayerEntityAwareness, HostedConnection> entry : awarenessConnectionMap.entrySet()) {
            PlayerEntityAwareness awareness = entry.getKey();
            if (awareness.testVisibility(spatial)) {
                sender.addCommandForSingle(command, entry.getValue());
            }

            if (awareness != myAwareness && myAwareness != null) {
                if (myAwareness.testVisibility(awareness.getOwnNode())) {
                    visibilityChanged(myAwareness, awareness.getOwnNode(), true);
                }
            }
        }
    }

    public void visibilityChanged(PlayerEntityAwareness awareness, Spatial target, boolean sees) {
        int entityId = target.getUserData(UserDataStrings.ENTITY_ID);
        Command command;

        if (sees) {
            int nodeBuilderId = target.getUserData(UserDataStrings.NODE_BUILDER_ID);
            int playerId = target.getUserData(UserDataStrings.PLAYER_ID);

            Vector3f location;
            Quaternion rotation;

            RigidBodyControl body = target.getControl(RigidBodyControl.class);
            if (body != null) {
                location = body.getPhysicsLocation();
                rotation = body.getPhysicsRotation();
            } else {
                location = target.getLocalTranslation();
                rotation = target.getLocalRotation();
            }
            command = new AddEntityCommand(entityId, nodeBuilderId, location, rotation, playerId);
        } else {
            command = new RemoveEntityCommand(entityId, RemovalReasons.DISAPPEARED);
        }

        ServerSender sender = app.getStateManager().getState(ServerSender.class);
        sender.addCommandForSingle(command, awarenessConnectionMap.get(awareness));
    }

    public void addPlayerListToPlayers() {
        for (PlayerEntityAwareness awareness : awarenessConnectionMap.keySet()) {
            for (PlayerEntityAwareness awareness2 : awarenessConnectionMap.keySet()) {
                awareness.addCharacter(awareness2.getOwnNode());
            }
        }
    }

    public PlayerEntityAwareness createAwarenessForPlayer(int playerId) {
        PlayerEntityAwareness playerAwareness = new PlayerEntityAwareness(playerId, walls, this);
        Collection<HostedConnection> connections = app.getStateManager()
                .getState(ServerSender.class).getServer().getConnections();

        for (HostedConnection hostedConnection : connections) {
            if (hostedConnection.getAttribute(ServerClientDataStrings.PLAYER_ID) == playerId) {
                awarenessConnectionMap.put(playerAwareness, hostedConnection);
                break;
            }
        }

        return playerAwareness;
    }

    public void registerCharacterForPlayer(int playerId, Node character) {
        for (PlayerEntityAwareness playerEntityAwareness : awarenessConnectionMap.keySet()) {
            if (playerEntityAwareness.getPlayerId() == playerId) {
                playerEntityAwareness.setOwnNode(character);
                character.getControl(EntityVariableControl.class).setAwareness(playerEntityAwareness);
                break;
            }
        }
    }

    public void setWalls(Node walls) {
        this.walls = walls;
    }

    private PlayerEntityAwareness searchForAwareness(Spatial spatial) {
        for (PlayerEntityAwareness playerEntityAwareness : awarenessConnectionMap.keySet()) {
            if (playerEntityAwareness.getOwnNode() == spatial) {
                return playerEntityAwareness;
            }
        }

        return null;
    }

    public void clearAwareness() {
        for (PlayerEntityAwareness playerEntityAwareness : awarenessConnectionMap.keySet()) {
            Spatial spatial = playerEntityAwareness.getOwnNode();
            if (spatial != null) {
                spatial.getControl(EntityVariableControl.class).setAwareness(null);
            }
        }

        awarenessConnectionMap.clear();
    }
}
