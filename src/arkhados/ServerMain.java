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

import arkhados.gamemode.DeathMatch;
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
import arkhados.net.OneTrueMessage;
import arkhados.net.Receiver;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
import arkhados.spell.buffs.AbstractBuff;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class ServerMain extends SimpleApplication {

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.ALL);
        try {
            FileHandler fileHandler = new FileHandler("./Arkhados_Server_%u_gen_%g.log", 0, 10);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fileHandler);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } catch (Exception ex) {
            System.exit(-1);
        }
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);

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
        Globals.assetManager = getAssetManager();
        Globals.app = this;
        worldManager = new WorldManager();
        gameManager = new ServerGameManager(new DeathMatch());
        physicsState = new BulletAppState();
        physicsState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        flyCam.setEnabled(false);

        try {
            server = Network.createServer(Globals.PORT, Globals.PORT);
            server.start();
        } catch (IOException ex) {
        }

        receiver = new Receiver();
        server.addMessageListener(receiver, OneTrueMessage.class);

        sender = new ServerSender(server);
        AbstractBuff.setSender(sender);

        receiver.registerCommandHandler(sender);

        MessageUtils.registerDataClasses();
        MessageUtils.registerMessages();
        listenerManager = new ServerNetListener(this, server);
        syncManager = new SyncManager(this);

        ServerPlayerInputHandler serverPlayerInputHandler = ServerPlayerInputHandler.get();
        serverPlayerInputHandler.setApp(this);

        receiver.registerCommandHandler(listenerManager);
        receiver.registerCommandHandler(serverPlayerInputHandler);
        receiver.registerCommandHandler(syncManager);

        stateManager.attach(sender);
        stateManager.attach(receiver);
        stateManager.attach(syncManager);
        stateManager.attach(worldManager);
        stateManager.attach(gameManager);
        stateManager.attach(physicsState);

        physicsState.getPhysicsSpace().setAccuracy(1.0f / 30.0f);        
    }

    public void startGame() {
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(25.0f);
        inputManager.setCursorVisible(true);
        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                gameManager.startGame();
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
        server.close();
        super.destroy();
    }
}