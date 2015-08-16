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
package arkhados.net;

import arkhados.World;
import arkhados.replay.ReplayData;
import arkhados.ui.hud.ServerClientDataStrings;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import com.jme3.network.base.MessageProtocol;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordingServerSender extends ServerSender {

    private World world;
    private ReplayData replayData = new ReplayData();
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public RecordingServerSender(Server server) {
        super(server);
    }

    @Override
    public void addCommandForSingle(Command command,
            HostedConnection connection) {
        super.addCommandForSingle(command, connection);
        saveCommand(command, connection);
    }

    private void saveCommand(Command command, HostedConnection connection) {
        if (command instanceof Ack) {
            return;
        }

        Integer playerId =
                connection.getAttribute(ServerClientDataStrings.PLAYER_ID);

        if (playerId == null) {
            return;
        }

        replayData.addCommand(command, playerId, world.getWorldTime());
    }

    public boolean saveToFile() {
        File replayDir = new File("replays");
        if (!replayDir.exists()) {
            replayDir.mkdir();
        }

        ByteBuffer target = ByteBuffer.allocate(314572800);
        MessageProtocol.messageToBuffer(replayData, target);

        FileOutputStream out = null;

        Date date = new Date();
        String name = dateFormat.format(date) + ".rep";
        String path = Paths.get(replayDir.toString(), name).toString();

        try {
            out = new FileOutputStream(path, false);
            out.getChannel().write(target);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RecordingServerSender.class.getName())
                    .log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RecordingServerSender.class.getName())
                    .log(Level.WARNING, null, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(RecordingServerSender.class.getName())
                            .log(Level.WARNING, null, ex);
                }
            }
        }

        return true;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
