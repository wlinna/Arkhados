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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author william
 */
public class ServerFogManager extends AbstractAppState {

    private Application app;
    // Node is player's node. No other nodes are saved
    private final Map<Node, List<Command>> enqueuedGuaranteed = new HashMap<>();
    private final Map<Node, List<Command>> enqueuedUnreliables = new HashMap<>();
    private final Map<Node, HostedConnection> nodeConnectionMap = new HashMap<>();

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
    }

    @Override
    public void update(float tpf) {
    }

    public void addCommand(Command command) {
        ServerSender sender = app.getStateManager().getState(ServerSender.class);
        sender.addCommand(command);
    }

    public void visibilityChanged(Spatial one, Spatial another, boolean sees) {
        Node player = (Node) one;
        int entityId = another.getUserData(UserDataStrings.ENTITY_ID);
        Command command;

        if (sees) {
            int nodeBuilderId = another.getUserData(UserDataStrings.NODE_BUILDER_ID);
            int playerId = another.getUserData(UserDataStrings.PLAYER_ID);

            Vector3f location;
            Quaternion rotation;

            RigidBodyControl body = another.getControl(RigidBodyControl.class);
            if (body != null) {
                location = body.getPhysicsLocation();
                rotation = body.getPhysicsRotation();
            } else {
                location = another.getLocalTranslation();
                rotation = another.getLocalRotation();
            }
            command = new AddEntityCommand(entityId, nodeBuilderId, location, rotation, playerId);
        } else {
            command = new RemoveEntityCommand(entityId, RemovalReasons.DISAPPEARED);
        }

        ServerSender sender = app.getStateManager().getState(ServerSender.class);
        sender.addCommand(command);
    }

    public void addPlayer(int entityId, int playerId) {
        Node entity = (Node) app.getStateManager().getState(WorldManager.class).getEntity(entityId);
        Collection<HostedConnection> connections = app.getStateManager()
                .getState(ServerSender.class).getServer().getConnections();
        
        for (HostedConnection hostedConnection : connections) {
            
            if (hostedConnection.getAttribute(ServerClientDataStrings.PLAYER_ID) == playerId) {
                nodeConnectionMap.put(entity, hostedConnection);
                break;
            }                
        }
        
        enqueuedGuaranteed.put(entity, new ArrayList<Command>());
        enqueuedUnreliables.put(entity, new ArrayList<Command>());
    }
}
