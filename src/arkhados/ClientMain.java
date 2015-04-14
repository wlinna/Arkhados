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
import arkhados.gamemode.GameMode;
import arkhados.ui.hud.ClientHudManager;
import arkhados.ui.KeySetter;
import arkhados.messages.MessageUtils;
import arkhados.net.ClientSender;
import arkhados.net.DefaultReceiver;
import arkhados.net.OneTrueMessage;
import arkhados.net.Receiver;
import arkhados.ui.Menu;
import arkhados.util.ValueWrapper;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.network.Network;
import com.jme3.network.NetworkClient;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.BackingStoreException;
import javax.imageio.ImageIO;

public class ClientMain extends SimpleApplication {

    public final static String PREFERENCES_KEY = "arkhados";

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.INFO);
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE);
        Logger.getLogger("com.jme3.system.lwjgl.LwjglContext")
                .setLevel(Level.SEVERE);
        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);

        AppSettings settings = new AppSettings(true);

        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler("./Arkhados_Client_%u_gen_%g.log", 0,
                    1, false);
            fileHandler.setLevel(Level.FINE);
            fileHandler.setFormatter(new SimpleFormatter());

            Logger.getLogger("").addHandler(fileHandler);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(ClientMain.class.getName())
                    .log(Level.WARNING, null, ex);
        }

        try {
            settings.load(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger("").log(Level.WARNING,
                    "Could not load preferences: {0}", ex.getMessage());
        }

        InputSettings.setInputDefaultSettings(settings);
        settings.setFrameRate(60);
        settings.setTitle("Arkhados");

        try {
            settings.setIcons(new BufferedImage[]{
                ImageIO.read(new File("icon32.png"))});
        } catch (IOException ex) {
            Logger.getLogger(ClientMain.class.getName())
                    .log(Level.WARNING, null, ex);
        }

        settings.setSettingsDialogImage("Interface/Images/Splash.png");
        ClientMain app = new ClientMain();
        app.setSettings(settings);
        ClientSettings.setAppSettings(settings);
        app.setPauseOnLostFocus(false);
        app.start();

    }
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private ValueWrapper<NetworkClient> clientWrapper = new ValueWrapper<>();
    private ClientHudManager clientHudManager;
    private ClientSender sender;
    private GameMode gameMode = null;
    private Menu menu;
    private InputSettings inputSettings;

    @Override
    public void simpleInitApp() {
        Globals.assetManager = getAssetManager();
        Globals.app = this;
        setDisplayStatView(false);
        ClientSettings.initialize(this);
        ClientSettings.setAppSettings(settings);
        inputSettings = new InputSettings(inputManager);
        BulletAppState bulletState = new BulletAppState();
        bulletState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);

        inputManager.setCursorVisible(true);

        clientHudManager = new ClientHudManager(cam, guiNode, guiFont);

        ClientFogManager fogManager = new ClientFogManager();
        stateManager.attach(fogManager);

        stateManager.attach(clientHudManager);
        stateManager.attach(bulletState);
        bulletState.getPhysicsSpace().setAccuracy(1f / 30f);
        flyCam.setEnabled(false);
        startNifty();

        SyncManager syncManager = new SyncManager(this);
        stateManager.attach(syncManager);

        EffectHandler effectHandler = new EffectHandler(this);
        WorldManager worldManager = new WorldManager(effectHandler);
        effectHandler.setWorldManager(worldManager);

        MessageUtils.registerDataClasses();
        MessageUtils.registerMessages();

        ClientNetListener listenerManager =
                new ClientNetListener(clientWrapper);
        stateManager.attach(listenerManager);

        stateManager.attach(worldManager);

        sender = new ClientSender();
        Receiver receiver = new DefaultReceiver();
        receiver.registerCommandHandler(effectHandler);

        UserCommandManager userCommandManager =
                new UserCommandManager(sender, inputManager);

        stateManager.attach(userCommandManager);

        stateManager.attach(sender);
        stateManager.attach(receiver);

        receiver.registerCommandHandler(sender);
        receiver.registerCommandHandler(syncManager);
        receiver.registerCommandHandler(listenerManager);

        MusicManager musicManager =
                new MusicManager(this, getInputManager(), getAssetManager());
        musicManager.setMusicCategory("Menu");
        musicManager.setPlaying(true);
        stateManager.attach(musicManager);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (gameMode != null) {
            gameMode.update(tpf);
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    private void startNifty() {
        guiNode.detachAllChildren();
        niftyDisplay = new NiftyJmeDisplay(assetManager,
                inputManager, audioRenderer, guiViewPort);

        menu = new Menu();
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/ClientUI.xml", "main_menu",
                menu,
                new KeySetter(this, inputManager, inputSettings),
                clientHudManager,
                ClientSettings.getClientSettings());
        guiViewPort.addProcessor(niftyDisplay);

        clientHudManager.setNifty(nifty);
    }

    public void connect(String username, String address, int port) {
        clientWrapper.set(Network.createClient());

        ClientNetListener listenerManager =
                stateManager.getState(ClientNetListener.class);
        listenerManager.reset();
        clientWrapper.get().addClientStateListener(listenerManager);

        Receiver receiver = stateManager.getState(Receiver.class);

        clientWrapper.get().addMessageListener(receiver, OneTrueMessage.class);
        sender.reset();
        receiver.reset();
        sender.setClient(clientWrapper.get());

        listenerManager.setName(username);

        try {
            clientWrapper.get().connectToServer(address, port, port);
            clientWrapper.get().start();
        } catch (IOException ex) {
            menu.setStatusText(ex.getMessage());
            Logger.getLogger(ClientMain.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public void setupGameMode(final String gameModeString) {
        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                switch (gameModeString) {
                    case "DeathMatch":
                        DeathMatch dm = new DeathMatch();
                        gameMode = dm;
                        gameMode.initialize(ClientMain.this);
                        dm.setNifty(nifty);
                        gameMode.startGame();
                        break;
                }
                return null;
            }
        });
    }

    public void startGame() {
        flyCam.setEnabled(false);

        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                gameMode.setRunning(true);
                Preloader.loadClient(assetManager);
                nifty.gotoScreen("default_hud");
                return null;
            }
        });
    }

    public void gameEnded() {
        gameMode.cleanup();
        gameMode = null;
    }

    @Override
    public void destroy() {
        NetworkClient client = clientWrapper.get();
        if (client != null && client.isConnected()) {
            clientWrapper.get().close();
        }
        super.destroy();
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        stateManager.getState(UserCommandManager.class).onLoseFocus();
    }

    public Menu getMenu() {
        return menu;
    }
}