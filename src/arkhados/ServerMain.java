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

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import arkhados.messages.usercommands.UcCastSpellMessage;
import arkhados.messages.usercommands.UcMouseTargetMessage;
import arkhados.messages.usercommands.UcRunToMessage;
import arkhados.messages.usercommands.UcWalkDirection;
import java.util.logging.FileHandler;

/**
 * test
 *
 * @author normenhansen
 */
public class ServerMain extends SimpleApplication {

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.ALL);
        try {
            FileHandler fileHandler = new FileHandler();
            fileHandler.setLevel(Level.FINE);
            Logger.getLogger("").addHandler(fileHandler);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        settings.setRenderer(null);

        ServerMain app = new ServerMain();
        app.setShowSettings(false);
        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.start(JmeContext.Type.Headless);
    }
    private Server server;
    private ServerNetListener listenerManager;
    private ServerGameManager gameManager;
    private WorldManager worldManager;
    private BulletAppState physicsState;
    private SyncManager syncManager;

    @Override
    public void simpleInitApp() {
        this.worldManager = new WorldManager();
        this.gameManager = new ServerGameManager();
        this.physicsState = new BulletAppState();
        this.physicsState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        this.flyCam.setEnabled(false);

        try {
            this.server = Network.createServer(Globals.PORT, Globals.PORT);
            this.server.start();
        } catch (IOException ex) {
        }
        this.listenerManager = new ServerNetListener(this, server);
        this.syncManager = new SyncManager(this, this.server);
        this.syncManager.setMessagesToListen(
                UcRunToMessage.class, UcCastSpellMessage.class,
                UcWalkDirection.class, UcMouseTargetMessage.class);

        this.stateManager.attach(ServerMain.this.syncManager);
        this.stateManager.attach(ServerMain.this.worldManager);
        this.stateManager.attach(ServerMain.this.gameManager);
        this.stateManager.attach(ServerMain.this.physicsState);
        this.physicsState.getPhysicsSpace().setAccuracy(1.0f / 30.0f);
    }

    public void startGame() {
        this.flyCam.setEnabled(true);
        this.flyCam.setMoveSpeed(25.0f);
        this.inputManager.setCursorVisible(true);
        this.enqueue(new Callable<Void>() {
            public Void call() throws Exception {

                ServerMain.this.gameManager.startGame();
                return null;
            }
        });
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    @Override
    public void destroy() {
        this.server.close();
        super.destroy();
    }
}