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

import arkhados.messages.MessageUtils;
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
import arkhados.messages.usercommands.UcCastSpellCommand;
import arkhados.messages.usercommands.UcMouseTargetCommand;
import arkhados.messages.usercommands.UcWalkDirection;
import arkhados.net.OneTrueMessage;
import arkhados.net.Receiver;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
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
//        settings.setRenderer(null);

        ServerMain app = new ServerMain();
        app.setShowSettings(false);
        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.start(JmeContext.Type.Headless);
//        app.start();
    }
    private Server server;
    private ServerNetListener listenerManager;
    private ServerGameManager gameManager;
    private WorldManager worldManager;
    private BulletAppState physicsState;
    private SyncManager syncManager;
    private Sender sender;
    private Receiver receiver;

    @Override
    public void simpleInitApp() {
        Globals.assetManager = this.getAssetManager();
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
        
        this.receiver = new Receiver();
        this.server.addMessageListener(this.receiver, OneTrueMessage.class);
        
        this.sender = new ServerSender(this.server);
        
        this.receiver.registerCommandHandler(sender);
        
        MessageUtils.registerDataClasses();
        MessageUtils.registerMessages();
        this.listenerManager = new ServerNetListener(this, server);
        this.syncManager = new SyncManager(this, this.server);
        this.receiver.registerCommandHandler(this.syncManager);

        this.stateManager.attach(this.sender);
        this.stateManager.attach(this.receiver);
        this.stateManager.attach(this.syncManager);
        this.stateManager.attach(this.worldManager);
        this.stateManager.attach(this.gameManager);
        this.stateManager.attach(this.physicsState);
        
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