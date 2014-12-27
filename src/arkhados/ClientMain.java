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
import arkhados.gamemode.LastManStanding;
import arkhados.ui.hud.ClientHudManager;
import arkhados.ui.KeySetter;
import arkhados.messages.ChatMessage;
import arkhados.messages.ClientSelectHeroCommand;
import arkhados.messages.MessageUtils;
import arkhados.messages.TopicOnlyCommand;
import arkhados.net.ClientSender;
import arkhados.net.OneTrueMessage;
import arkhados.net.Receiver;
import arkhados.util.InputMappingStrings;
import arkhados.util.PlayerDataStrings;
import arkhados.util.ValueWrapper;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.network.Network;
import com.jme3.network.NetworkClient;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.BackingStoreException;

public class ClientMain extends SimpleApplication implements ScreenController {

    public final static String PREFERENCES_KEY = "arkhados";

    public static void setInputDefaultSettings(AppSettings settings) {
        setKey(settings, InputMappingStrings.M1, false, MouseInput.BUTTON_LEFT);
        setKey(settings, InputMappingStrings.M2, false, MouseInput.BUTTON_RIGHT);

        setKey(settings, InputMappingStrings.MOVE_UP, true, KeyInput.KEY_W);
        setKey(settings, InputMappingStrings.MOVE_DOWN, true, KeyInput.KEY_S);
        setKey(settings, InputMappingStrings.MOVE_LEFT, true, KeyInput.KEY_A);
        setKey(settings, InputMappingStrings.MOVE_RIGHT, true, KeyInput.KEY_D);

        setKey(settings, InputMappingStrings.Q, true, KeyInput.KEY_Q);
        setKey(settings, InputMappingStrings.E, true, KeyInput.KEY_E);
        setKey(settings, InputMappingStrings.R, true, KeyInput.KEY_R);
        setKey(settings, InputMappingStrings.SPACE, true, KeyInput.KEY_SPACE);

        if (!settings.containsKey(PlayerDataStrings.COMMAND_MOVE_INTERRUPTS)) {
            settings.putBoolean(PlayerDataStrings.COMMAND_MOVE_INTERRUPTS, false);
        }
    }

    public static void setKey(AppSettings settings, final String inputMapping, boolean isKeyboard, int code) {
        if (settings.containsKey(inputMapping)) {
            return;
        }
        String prefix = isKeyboard ? "keyboard::" : "mouse::";
        String setting = prefix + Integer.toString(code);
        settings.putString(inputMapping, setting);

    }

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.INFO);
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE);
        Logger.getLogger("com.jme3.system.lwjgl.LwjglContext").setLevel(Level.SEVERE);
        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);

        AppSettings settings = new AppSettings(true);

        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler("./Arkhados_Client_%u_gen_%g.log", 0, 10);
            fileHandler.setLevel(Level.FINE);
            fileHandler.setFormatter(new SimpleFormatter());

            Logger.getLogger("").addHandler(fileHandler);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            settings.load(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger("").log(Level.WARNING, "Could not load preferences: {0}", ex.getMessage());
        }
        setInputDefaultSettings(settings);
        settings.setFrameRate(60);
        settings.setTitle("Arkhados Client");
        ClientMain app = new ClientMain();
        app.setSettings(settings);
        ClientSettings.setAppSettings(settings);
        app.setPauseOnLostFocus(false);
        app.start();

    }
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private TextRenderer statusText;
    private ValueWrapper<NetworkClient> clientWrapper = new ValueWrapper<>();
    private ClientHudManager clientHudManager;
    private ClientSender sender;
    private RoundManager roundManager;
    private GameMode gameMode = null;

    @Override
    public void simpleInitApp() {
        Globals.assetManager = getAssetManager();
        Globals.app = this;
        setDisplayStatView(false);
        ClientSettings.initialize(this);
        ClientSettings.setAppSettings(settings);
        BulletAppState bulletState = new BulletAppState();
        bulletState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);

        inputManager.setCursorVisible(true);

        clientHudManager = new ClientHudManager(cam, guiNode, guiFont);

        ClientFogManager fogManager = new ClientFogManager();
        stateManager.attach(fogManager);

        stateManager.attach(clientHudManager);
        stateManager.attach(bulletState);
        bulletState.getPhysicsSpace().setAccuracy(1.0f / 30.0f);
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(25.0f);
        startNifty();

        SyncManager syncManager = new SyncManager(this);
        stateManager.attach(syncManager);

        EffectHandler effectHandler = new EffectHandler(this);
        WorldManager worldManager = new WorldManager(effectHandler);
        effectHandler.setWorldManager(worldManager);

        MessageUtils.registerDataClasses();
        MessageUtils.registerMessages();

        ClientNetListener listenerManager = new ClientNetListener(clientWrapper);
        stateManager.attach(listenerManager);

        stateManager.attach(worldManager);

        sender = new ClientSender();
        Receiver receiver = new Receiver();
        receiver.registerCommandHandler(effectHandler);

        UserCommandManager userCommandManager = new UserCommandManager(sender, inputManager);

        stateManager.attach(userCommandManager);

        stateManager.attach(sender);
        stateManager.attach(receiver);

        receiver.registerCommandHandler(sender);
        receiver.registerCommandHandler(syncManager);
        receiver.registerCommandHandler(listenerManager);

        MusicManager musicManager = new MusicManager(this, getInputManager(), getAssetManager());
        musicManager.setHero("EmberMage");
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

        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/ClientUI.xml", "main_menu", this,
                new KeySetter(this, inputManager), clientHudManager,
                ClientSettings.getClientSettings());
        guiViewPort.addProcessor(niftyDisplay);

        clientHudManager.setNifty(nifty);

        statusText = nifty.getScreen("join_server")
                .findElementByName("status_text")
                .getRenderer(TextRenderer.class);
    }

    public void setStatusText(final String text) {
        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                statusText.setText(text);
                return null;
            }
        });
    }

    public void connect() {
        Screen joinScreen = nifty.getScreen("join_server");
        String username = joinScreen.findNiftyControl("username_text", TextField.class)
                .getDisplayedText();

        int port = Integer.parseInt(joinScreen.findNiftyControl("server_port", TextField.class)
                .getDisplayedText());
        String ip = joinScreen.findNiftyControl("server_ip", TextField.class).getDisplayedText();

        clientWrapper.set(Network.createClient());

        ClientNetListener listenerManager = stateManager.getState(ClientNetListener.class);
        listenerManager.reset();
        clientWrapper.get().addClientStateListener(listenerManager);

        Receiver receiver = stateManager.getState(Receiver.class);

        clientWrapper.get().addMessageListener(receiver, OneTrueMessage.class);
        sender.reset();
        receiver.reset();
        sender.setClient(clientWrapper.get());

        if (username.trim().length() == 0) {
            setStatusText("Username is invalid");
            return;
        }
        listenerManager.setName(username);

        setStatusText("Connecting... " + username);
        try {
            clientWrapper.get().connectToServer(ip, port, port);
            clientWrapper.get().start();
        } catch (IOException ex) {
            setStatusText(ex.getMessage());
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setupGameMode(final String gameModeString) {
        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                switch (gameModeString) {
                    case "LastManStanding":
                        toLobby();
                        gameMode = new LastManStanding();
                        gameMode.initialize(ClientMain.this);
                        break;
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

    public void toLobby() {
        inputManager.setCursorVisible(true);
        nifty.gotoScreen("lobby");
    }

    // TODO: Change playerDatas type to something that holds all necessary data
    public void refreshPlayerData(final List<PlayerData> playerDataList) {
        PlayerData.setPlayers(playerDataList);
        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Screen screen = nifty.getScreen("lobby");
                if (screen == null) {
                    System.out.println("Screen is null");
                }
                ListBox listBox = screen.findNiftyControl("players_list", ListBox.class);
                assert listBox != null;
                listBox.clear();
                for (PlayerData playerData : playerDataList) {
                    listBox.addItem(playerData.getStringData(PlayerDataStrings.NAME));
                }

                return null;
            }
        });
    }

    public void addChat(final String name, final String message) {
        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Screen screen = nifty.getScreen("lobby");
                ListBox listBox = screen.findNiftyControl("chat_list", ListBox.class);
                listBox.addItem(String.format("<%s> %s", name, message));
                return null;
            }
        });
    }

    public void sendChat() {
        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Screen screen = nifty.getScreen("lobby");
                TextField textField = screen.findNiftyControl("chat_text", TextField.class);
                String name = stateManager.getState(ClientNetListener.class).getName();

                sender.addCommand(new ChatMessage(name, textField.getDisplayedText()));
                textField.setText("");
                return null;
            }
        });
    }

    public void selectHero(String heroName) {
        stateManager.getState(MusicManager.class).setHero(heroName);
        sender.addCommand(new ClientSelectHeroCommand(heroName));
    }

    public void sendStartGameRequest() {
        sender.addCommand(new TopicOnlyCommand(Topic.START_GAME));
    }

    public void startGame() {
        flyCam.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    enqueue(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            gameMode.setRunning(true);
                            WorldManager worldManager = stateManager.getState(WorldManager.class);
                            worldManager.preloadModels(new String[]{
                                "Models/EliteSoldier.j3o",
                                "Models/Mage.j3o", "Models/Warwolf.j3o",
                                "Models/Circle.j3o", "Models/DamagingDagger.j3o",
                                "Models/SealingBoulder.j3o",
                                "Models/SpiritStone.j3o",
                                "Scenes/LavaArenaWithFogWalls.j3o"});
                            worldManager.preloadSoundEffects(new String[]{
                                "EmberCircle.wav", "FireballExplosion.wav",
                                "Firewalk.wav", "MagmaBash.wav",
                                "MeteorBoom.wav", "PurifyingFlame.wav",
                                "Shotgun.wav", "Rend1.wav", "Rend2.wav",
                            "Rend3.wav", "RockGolemPain.wav", 
                            "VenatorDeath.wav", "VenatorPain.wav",
                            "EmberMageDeath.wav", "EmberMagePain.wav",
                            "EliteSoldierDeath.wav", "EliteSoldierPain.wav",
                            "DeepWounds.wav", "Petrify.wav", "Railgun.wav"});
                            nifty.gotoScreen("default_hud");
                            return null;
                        }
                    }).get();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public void gameEnded() {
        gameMode.cleanup();
        gameMode = null;
    }

    public void gotoMenu(final String menu) {
        enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                nifty.gotoScreen(menu);
                return null;
            }
        });
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    public void closeApplication() {
        stop();
    }

    @Override
    public void destroy() {
        final NetworkClient client = clientWrapper.get();
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
}