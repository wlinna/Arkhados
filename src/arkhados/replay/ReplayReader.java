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
package arkhados.replay;

import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Receiver;
import com.jme3.app.state.AbstractAppState;
import com.jme3.math.FastMath;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReplayReader extends AbstractAppState implements Receiver {

    private List<CommandHandler> handlers = new ArrayList<>();
    private ReplayData data;
    private float time = 0f;

    public void loadReplay(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(2);
        data = (ReplayData) Serializer.readClassAndObject(buffer);
    }

    public void selectPlayer(int playerId) {
        List<ReplayCmdData> commands = data.getCommands();
        for (Iterator<ReplayCmdData> it = commands.iterator(); it.hasNext();) {
            ReplayCmdData cmdData = it.next();
            if (cmdData.getPlayerId() != playerId) {
                it.remove();
            }
        }

        if (!commands.isEmpty()) {
            time = FastMath.clamp(commands.get(0).getTime() - 1, 0f,
                    Float.MAX_VALUE);
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        time += tpf;

        for (Iterator<ReplayCmdData> it = data.getCommands().iterator();
                it.hasNext();) {
            ReplayCmdData cmdData = it.next();
            if (cmdData.getTime() <= time) {
                // TODO: Simulate
                handleCommand(cmdData.getCommand());
                it.remove();
            } else {
                break;
            }
        }
    }

    @Override
    public void registerCommandHandler(CommandHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Null CommandHandlers are not"
                    + " accepted");
        }
        handlers.add(handler);
    }

    @Override
    public boolean removeCommandHandler(CommandHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Null CommandHandlers are not"
                    + " accepted");
        }

        return handlers.remove(handler);
    }

    private void handleCommand(Command command) {
        for (CommandHandler commandHandler : handlers) {
            if (command.isGuaranteed()) {
                commandHandler.readGuaranteed(null, command);
            } else {
                commandHandler.readUnreliable(null, command);
            }
        }
    }

    @Override
    public void addConnection(HostedConnection connection) {
    }

    @Override
    public void reset() {
    }

    @Override
    public void messageReceived(Object source, Message m) {
    }

    public ReplayData getData() {
        return data;
    }
}
