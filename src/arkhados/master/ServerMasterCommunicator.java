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
import arkhados.master.messages.RepGameList;
import arkhados.master.messages.ReqGameList;
import arkhados.master.messages.ReqRegisterGame;
import arkhados.master.messages.ReqUnregisterGame;
import arkhados.settings.server.Settings;
import arkhados.settings.server.SettingsGeneral;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMasterCommunicator extends Listener {

    private final Client client = new Client();
    private boolean triedConnect = false;

    public void connectToMaster() {
        if (triedConnect) {
            return;
        }
        
        triedConnect = true;
        
        init();
        new Thread("Connect") {
            @Override
            public void run() {

                try {

                    SettingsGeneral general = Settings.get().General();
                    client.start();
                    client.addListener(ServerMasterCommunicator.this);
                    client.connect(5000, general.getMasterServerAddress(),
                            general.getMasterServerPort(),
                            general.getMasterServerPort());
                    register();
                } catch (IOException ex) {
                    Logger.getLogger(ServerMain.class.getName()).
                            log(Level.WARNING, null, ex);
                }
            }
        }.start();
    }

    private void init() {
        Kryo kryo = client.getKryo();
        kryo.register(Game.class);
        kryo.register(RepGameList.class);
        kryo.register(ReqGameList.class);
        kryo.register(ReqRegisterGame.class);
        kryo.register(ReqUnregisterGame.class);
    }

    private void register() {
        client.sendTCP(new ReqRegisterGame(new Date().toString(),
                Settings.get().General().getGameMode()));
    }

    public void destroy() {
        if (client == null || !client.isConnected()) {
            return;
        }

        client.removeListener(this);
        client.close();
    }
}
