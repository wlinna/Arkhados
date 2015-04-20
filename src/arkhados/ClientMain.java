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

import arkhados.effects.BlindManager;
import arkhados.gamemode.DeathMatch;
import arkhados.gamemode.GameMode;
import arkhados.ui.hud.ClientHudManager;
import arkhados.ui.KeySetter;
import arkhados.messages.MessageUtils;
import arkhados.net.ClientSender;
import arkhados.net.DefaultReceiver;
import arkhados.net.OneTrueMessage;
import arkhados.net.Receiver;
import arkhados.net.Sender;
import arkhados.replay.FakeSender;
import arkhados.replay.ReplayCmdData;
import arkhados.replay.ReplayData;
import arkhados.replay.ReplayHeader;
import arkhados.replay.ReplayReader;
import arkhados.ui.ConnectionMenu;
import arkhados.ui.MainMenu;
import arkhados.ui.ReplayMenu;
import arkhados.util.ValueWrapper;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.network.Network;
import com.jme3.network.NetworkClient;
import com.jme3.network.serializing.Serializer;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.ALL);
        Logger.getLogger("com.jme3.system.lwjgl.LwjglContext")
                .setLevel(Level.SEVERE);
        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.ALL);

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
    private Sender sender;
    private GameMode gameMode = null;
    private InputSettings inputSettings;
    private List<AppState> swappableStates = new ArrayList<>();

    @Override
    public void simpleInitApp() {
        File replayDir = new File("replays");
        if (!replayDir.exists()) {
            replayDir.mkdir();
        }

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

        MusicManager musicManager =
                new MusicManager(this, getInputManager(), getAssetManager());
        musicManager.setMusicCategory("Menu");
        musicManager.setPlaying(true);
        stateManager.attach(musicManager);

        MessageUtils.registerDataClasses();
        MessageUtils.registerMessages();

        Serializer.registerClass(ReplayHeader.class);
        Serializer.registerClass(ReplayCmdData.class);
        Serializer.registerClass(ReplayData.class);
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
        nifty = niftyDisplay.getNifty();

        nifty.fromXml("Interface/ClientUI.xml", "main_menu",
                new MainMenu(),
                new ConnectionMenu(),
                new ReplayMenu(),
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

        ((ClientSender) sender).setClient(clientWrapper.get());

        listenerManager.setName(username);

        try {
            clientWrapper.get().connectToServer(address, port, port);
            clientWrapper.get().start();
        } catch (IOException ex) {
            ConnectionMenu menu = (ConnectionMenu) nifty
                    .findScreenController("arkhados.ui.ConnectionMenu");
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
        UserCommandManager state =
                stateManager.getState(UserCommandManager.class);
        if (state != null) {
            state.onLoseFocus();
        }
    }

    public void cleanAppStatesAndHandlers() {
        for (AppState appState : swappableStates) {
            stateManager.detach(appState);
        }

        swappableStates.clear();

        sender = null;
    }

    public void prepareForGame() {
        sender = new ClientSender();
        prepareAppStatesAndHandlers(new DefaultReceiver());
    }

    public void prepareForReplay() {
        sender = new FakeSender();
        ReplayReader reader = new ReplayReader();
        reader.setEnabled(false);
        prepareAppStatesAndHandlers(reader);
    }

    private void prepareAppStatesAndHandlers(Receiver receiver) {
        ConnectionMenu connectionMenu = (ConnectionMenu) nifty
                .findScreenController("arkhados.ui.ConnectionMenu");
        ClientNetListener netListener = new ClientNetListener(connectionMenu);
        swappableStates.add(new BlindManager());

        swappableStates.add(netListener);
        receiver.registerCommandHandler(netListener);

        EffectHandler effectHandler = new EffectHandler(this);
        WorldManager worldManager = new WorldManager(effectHandler);
        effectHandler.setWorldManager(worldManager);
        swappableStates.add(worldManager);

        receiver.registerCommandHandler(effectHandler);

        SyncManager syncManager = new SyncManager(this);
        swappableStates.add(syncManager);
        receiver.registerCommandHandler(syncManager);

        UserCommandManager userCommandManager =
                new UserCommandManager(inputManager);

        swappableStates.add(userCommandManager);
        swappableStates.add(sender);
        swappableStates.add(receiver);

        for (AppState appState : swappableStates) {
            stateManager.attach(appState);
        }

        receiver.registerCommandHandler(sender);
    }
}