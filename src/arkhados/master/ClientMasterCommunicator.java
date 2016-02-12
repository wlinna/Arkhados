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

import arkhados.ServerMain;
import arkhados.master.messages.ReqGameList;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientMasterCommunicator extends Listener {

    private final Client client = new Client();

    public void connectToMaster() {
        client.start();
        client.addListener(this);
        try {
            client.connect(5000, "localhost", 12346, 12346);
            client.sendTCP(new ReqGameList());
        } catch (IOException ex) {
            Logger.getLogger(ServerMain.class.getName()).
                    log(Level.WARNING, null, ex);
        }
    }

    public void destroy() {
        if (client == null || !client.isConnected()) {
            return;
        }

        client.removeListener(this);
        client.close();
    }
}
