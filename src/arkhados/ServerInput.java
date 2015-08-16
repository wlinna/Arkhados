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
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Stores player input states.
 *
 * TODO: Refactor later so that it's not singleton.
 */
public class ServerInput implements CommandHandler {

    private static ServerInput instance = null;
    private Map<Integer, ServerInputState> inputStates = new HashMap<>();
    private Application app;

    private ServerInput() {
    }

    public static ServerInput get() {
        if (instance == null) {
            instance = new ServerInput();
        }

        return instance;
    }

    public void addInputState(int id) {
        inputStates.put(id, new ServerInputState());
    }

    public ServerInputState getInputState(int playerId) {
        return inputStates.get(playerId);
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
        ServerInputState inputState = inputStates.get(playerId);

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