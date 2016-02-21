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
package arkhados.master;

import arkhados.master.messages.KryoMessages;
import arkhados.ServerMain;
import arkhados.master.messages.RepGameList;
import arkhados.master.messages.ReqGameList;
import arkhados.ui.ConnectionMenu;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ClientMasterCommunicator extends Listener {

    private final Client client = new Client();
    private boolean triedConnect = false;
    private ConnectionMenu connectionMenu;

    public void connectToMaster() {
        if (triedConnect) {
            return;
        }

        triedConnect = true;

        init();
        client.start();
        client.addListener(this);
        try {
            client.connect(5000, "localhost", 12346, 12346);
        } catch (IOException ex) {
            Logger.getLogger(ServerMain.class.getName()).
                    log(Level.WARNING, null, ex);
        }
    }

    public void requestGameList() {
        client.sendTCP(new ReqGameList());
    }

    @Override
    public void received(Connection connection, Object object) {
        super.received(connection, object);
        if (object instanceof RepGameList) {
            handleGameList((RepGameList) object);
        }
    }

    private void handleGameList(RepGameList msg) {
        connectionMenu.listGames(msg.games);
    }

    public void destroy() {
        if (client == null || !client.isConnected()) {
            return;
        }

        client.removeListener(this);
        client.close();
    }

    private void init() {
        KryoMessages.register(client.getKryo());
    }

    public void setConnectionMenu(ConnectionMenu connectionMenu) {
        this.connectionMenu = connectionMenu;
    }

}
