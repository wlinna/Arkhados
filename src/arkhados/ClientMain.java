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

import arkhados.messages.BattleStatisticsResponse;
import arkhados.ui.hud.ClientHudManager;
import arkhados.ui.KeySetter;
import arkhados.messages.ChatMessage;
import arkhados.messages.ClientSelectHeroMessage;
import arkhados.messages.ConnectionEstablishedMessage;
import arkhados.messages.MessageUtils;
import arkhados.messages.PlayerDataTableMessage;
import arkhados.messages.ServerLoginMessage;
import arkhados.messages.SetPlayersCharacterMessage;
import arkhados.messages.StartGameMessage;
import arkhados.messages.UDPHandshakeAck;
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
import de.lessvoid.nifty.controls.textfield.TextFieldControl;
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

    public static void putToSettingsIfNotExists() {
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
        Logger.getLogger("").setLevel(Level.ALL);
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE);
        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);
        AppSettings settings = new AppSettings(true);

        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler();

            fileHandler.setLevel(Level.FINE);
            Logger.getLogger("").addHandler(fileHandler);
        } catch (IOException ex) {
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
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
    private WorldManager worldManager;
    private ClientNetListener listenerManager;
    private SyncManager syncManager;
    private BulletAppState bulletState;
    private UserCommandManager userCommandManager;
    private ClientHudManager clientHudManager;
    private RoundManager roundManager;
    private EffectHandler effectHandler;

    @Override
    public void simpleInitApp() {
        Globals.assetManager = this.getAssetManager();
        this.setDisplayStatView(false);
        ClientSettings.initialize(this);
        ClientSettings.setAppSettings(settings);
        this.bulletState = new BulletAppState();
        this.bulletState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);

        this.inputManager.setCursorVisible(true);

        this.clientHudManager = new ClientHudManager(this.cam, this.guiNode, this.guiFont);

        this.stateManager.attach(this.clientHudManager);
        this.stateManager.attach(this.bulletState);
        this.bulletState.getPhysicsSpace().setAccuracy(1.0f / 30.0f);
        this.flyCam.setEnabled(false);
        this.flyCam.setMoveSpeed(25.0f);
        this.startNifty();

        this.syncManager = new SyncManager(this, this.clientWrapper);
        this.stateManager.attach(this.syncManager);

        
        this.effectHandler = new EffectHandler(this);
        this.worldManager = new WorldManager(this.effectHandler);
        this.effectHandler.setWorldManager(worldManager);

        this.userCommandManager = new UserCommandManager(this.clientWrapper, this.inputManager);

        MessageUtils.registerDataClasses();
        MessageUtils.registerMessages();

        this.listenerManager = new ClientNetListener(clientWrapper);
        this.stateManager.attach(this.listenerManager);

        this.stateManager.attach(this.worldManager);

        this.roundManager = new RoundManager();
        this.stateManager.attach(this.roundManager);

        ClientMain.this.stateManager
                .attach(ClientMain.this.userCommandManager);

    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    private void startNifty() {
        this.guiNode.detachAllChildren();
        this.niftyDisplay = new NiftyJmeDisplay(this.assetManager,
                this.inputManager, this.audioRenderer, this.guiViewPort);

        this.nifty = this.niftyDisplay.getNifty();
        this.nifty.fromXml("Interface/ClientUI.xml", "main_menu", this, new KeySetter(this, this.inputManager), this.clientHudManager, ClientSettings.getClientSettings());
        this.guiViewPort.addProcessor(this.niftyDisplay);

        this.clientHudManager.setNifty(nifty);

        this.statusText = this.nifty.getScreen("join_server")
                .findElementByName("status_text")
                .getRenderer(TextRenderer.class);
    }

    public void setStatusText(final String text) {
        this.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                ClientMain.this.statusText.setText(text);
                return null;
            }
        });
    }

    public void connect() {
        // FIXME: TextFieldControl is deprecated

        final String username = nifty.getScreen("join_server")
                .findElementByName("username_text")
                .getControl(TextFieldControl.class).getText();

        final int port = Integer.parseInt(nifty.getScreen("join_server")
                .findElementByName("server_port").getControl(TextFieldControl.class).getText());
        final String ip = nifty.getScreen("join_server").
                findElementByName("server_ip").getControl(TextFieldControl.class).getText();


        this.clientWrapper.set(Network.createClient());
        this.roundManager.configureForClient();
        this.listenerManager.reset();
        this.clientWrapper.get().addClientStateListener(this.listenerManager);
        this.clientWrapper.get().addMessageListener(this.listenerManager,
                ConnectionEstablishedMessage.class, UDPHandshakeAck.class, ServerLoginMessage.class, PlayerDataTableMessage.class,
                ChatMessage.class, StartGameMessage.class, SetPlayersCharacterMessage.class, BattleStatisticsResponse.class);

        this.effectHandler.setMessagesToListen(this.clientWrapper.get());

        if (username.trim().length() == 0) {
            this.setStatusText("Username is invalid");
            return;
        }
        this.listenerManager.setName(username);

        this.syncManager.configureForClient();
        this.setStatusText("Connecting... " + username);
        try {
            this.clientWrapper.get().connectToServer(ip, port, port);
            this.clientWrapper.get().start();
        } catch (IOException ex) {
            this.setStatusText(ex.getMessage());
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void toLobby() {
        this.inputManager.setCursorVisible(true);
        this.nifty.gotoScreen("lobby");
    }

    // TODO: Change playerDatas type to something that holds all necessary data
    public void refreshPlayerData(final List<PlayerData> playerDataList) {
        PlayerData.setPlayers(playerDataList);
        this.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                Screen screen = ClientMain.this.nifty.getScreen("lobby");
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
        this.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                Screen screen = ClientMain.this.nifty.getScreen("lobby");
                ListBox listBox = screen.findNiftyControl("chat_list", ListBox.class);
                listBox.addItem(String.format("<%s> %s", name, message));
                return null;
            }
        });
    }

    public void sendChat() {
        this.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                Screen screen = ClientMain.this.nifty.getScreen("lobby");
                TextField textField = screen.findNiftyControl("chat_text", TextField.class);
                ClientMain.this.clientWrapper.get().send(new ChatMessage(
                        ClientMain.this.listenerManager.getName(),
                        textField.getText()));
                textField.setText("");
                return null;
            }
        });
    }

    public void selectHero(final String heroName) {
        this.clientWrapper.get().send(new ClientSelectHeroMessage(heroName));
    }

    public void sendStartGameRequest() {
        this.clientWrapper.get().send(new StartGameMessage());
    }

    public void startGame() {
        this.flyCam.setEnabled(false);

        new Thread(new Runnable() {
            public void run() {
                try {
                    ClientMain.this.enqueue(new Callable<Void>() {
                        public Void call() throws Exception {
                            worldManager.preloadModels(new String[]{"Models/Archer.j3o", "Models/Mage.j3o", "Models/Warwolf.j3o", "Models/Circle.j3o", "Models/DamagingDagger.j3o", "Scenes/LavaArenaWithWalls.j3o"});
                            worldManager.preloadSoundEffects(new String[]{"FireballExplosion.wav", "MeteorBoom.wav", "Shotgun.wav"});
                            ClientMain.this.nifty.gotoScreen("default_hud");
                            return null;
                        }
                    }).get();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public UserCommandManager getUserCommandManager() {
        return this.userCommandManager;
    }

    public void gotoMenu(final String menu) {
        this.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                ClientMain.this.nifty.gotoScreen(menu);
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
        this.stop();
    }

    @Override
    public void destroy() {
        final NetworkClient client = this.clientWrapper.get();
        if (client != null && client.isConnected()) {
            this.clientWrapper.get().close();
        }
        super.destroy();
    }

    public RoundManager getRoundManager() {
        return this.roundManager;
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        this.userCommandManager.onLoseFocus();
    }
}