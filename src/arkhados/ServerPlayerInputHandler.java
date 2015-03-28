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

import arkhados.controls.CUserInput;
import arkhados.messages.usercommands.CmdUcMouseTarget;
import arkhados.messages.usercommands.CmdUcWalkDirection;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import com.jme3.app.Application;
import com.jme3.network.HostedConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Stores player input states.
 *
 * TODO: Refactor later so that it's not singleton.
 *
 * @author william
 */
public class ServerPlayerInputHandler implements CommandHandler {

    private static ServerPlayerInputHandler instance = null;
    private Map<Integer, ServerPlayerInputState> playerInputStates =
            new HashMap<>();
    private Application app;

    private ServerPlayerInputHandler() {
    }

    public static ServerPlayerInputHandler get() {
        if (instance == null) {
            instance = new ServerPlayerInputHandler();
        }

        return instance;
    }

    public void addPlayerInputState(int id) {
        playerInputStates.put(id, new ServerPlayerInputState());
    }

    public ServerPlayerInputState getPlayerInputState(int playerId) {
        return playerInputStates.get(playerId);
    }

    @Override
    public void readGuaranteed(Object source, Command guaranteed) {
        handleCommands((HostedConnection) source, guaranteed);
    }

    @Override
    public void readUnreliable(Object source, Command unreliables) {
        handleCommands((HostedConnection) source, unreliables);
    }

    private void handleCommands(HostedConnection source,
            final Command command) {
        final int playerId = ServerClientData.getPlayerId(source.getId());

        if (playerId != -1) {
            // TODO: Consider if this is the best place to put app.enqueue
            app.enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doMessage(playerId, command);
                    return null;
                }
            });
        } else {
            System.out.println("There is no playerId for sourceId "
                    + source.getId());
        }
    }

    private void doMessage(int playerId, Command command) {
        ServerPlayerInputState inputState = playerInputStates.get(playerId);

        if (inputState == null) {
            return;
        }

        if (command instanceof CmdUcWalkDirection) {
            CmdUcWalkDirection uc = (CmdUcWalkDirection) command;
            inputState.previousDown = uc.getDown();
            inputState.previousRight = uc.getRight();
            if (inputState.currentActiveSpatial != null) {
                inputState.currentActiveSpatial.getControl(
                        CUserInput.class).updateDirection();
            }
        } else if (command instanceof CmdUcMouseTarget) {
            CmdUcMouseTarget uc = (CmdUcMouseTarget) command;
            inputState.mouseTarget = uc.getLocation();
        }

    }

    public void setApp(Application app) {
        this.app = app;
    }
}