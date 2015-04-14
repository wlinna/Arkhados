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
package arkhados.util;

import arkhados.Globals;
import arkhados.net.ServerSender;
import arkhados.ui.hud.ServerClientDataStrings;
import com.jme3.network.HostedConnection;
import java.util.Collection;

/**
 *
 * @author william
 */
public class ConnectionHelper {

    public static HostedConnection getSource(int playerId) {
        Collection<HostedConnection> connections = Globals.app.getStateManager()
                .getState(ServerSender.class).getServer().getConnections();

        for (HostedConnection hostedConnection : connections) {
            if (hostedConnection.getAttribute(ServerClientDataStrings.PLAYER_ID)
                    .equals(playerId)) {
                return hostedConnection;
            }
        }

        return null;
    }
}
