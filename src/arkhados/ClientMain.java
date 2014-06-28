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
    
    private ClientSender sender;
    private Receiver receiver;

    @Override
    public void simpleInitApp() {
        Globals.assetManager = getAssetManager();
        setDisplayStatView(false);
        ClientSettings.initialize(this);
        ClientSettings.setAppSettings(settings);
        bulletState = new BulletAppState();
        bulletState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);

        inputManager.setCursorVisible(true);

        clientHudManager = new ClientHudManager(cam, guiNode, guiFont);

        stateManager.attach(clientHudManager);
        stateManager.attach(bulletState);
        bulletState.getPhysicsSpace().setAccuracy(1.0f / 30.0f);
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(25.0f);
        startNifty();

        syncManager = new SyncManager(this, clientWrapper);
        stateManager.attach(syncManager);

        effectHandler = new EffectHandler(this);
        worldManager = new WorldManager(effectHandler);
        effectHandler.setWorldManager(worldManager);

        MessageUtils.registerDataClasses();
        MessageUtils.registerMessages();

        listenerManager = new ClientNetListener(clientWrapper);
        stateManager.attach(listenerManager);

        stateManager.attach(worldManager);

        roundManager = new RoundManager();
        stateManager.attach(roundManager);
        
        sender = new ClientSender();
        receiver = new Receiver();
        receiver.registerCommandHandler(effectHandler);               
        
        userCommandManager = new UserCommandManager(sender, inputManager);
        
        stateManager.attach(userCommandManager);
      
        stateManager.attach(sender);
        stateManager.attach(receiver);

        receiver.registerCommandHandler(sender);
        receiver.registerCommandHandler(syncManager);
        receiver.registerCommandHandler(listenerManager);
        receiver.registerCommandHandler(roundManager);
    }

    @Override
    public void simpleUpdate(float tpf) {
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
            public Void call() throws Exception {
                statusText.setText(text);
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


        clientWrapper.set(Network.createClient());       
        listenerManager.reset();
        clientWrapper.get().addClientStateListener(listenerManager);
        
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
            public Void call() throws Exception {
                Screen screen = nifty.getScreen("lobby");
                TextField textField = screen.findNiftyControl("chat_text", TextField.class);
                sender.addCommand(new ChatMessage(
                        listenerManager.getName(),
                        textField.getText()));
                textField.setText("");
                return null;
            }
        });
    }

    public void selectHero(final String heroName) {
        sender.addCommand(new ClientSelectHeroCommand(heroName));
    }

    public void sendStartGameRequest() {
        sender.addCommand(new TopicOnlyCommand(Topic.START_GAME));
    }

    public void startGame() {
        flyCam.setEnabled(false);

        new Thread(new Runnable() {
            public void run() {
                try {
                    enqueue(new Callable<Void>() {
                        public Void call() throws Exception {
                            worldManager.preloadModels(new String[]{"Models/Archer.j3o",
                                "Models/Mage.j3o", "Models/Warwolf.j3o",
                                "Models/Circle.j3o", "Models/DamagingDagger.j3o",
                                "Scenes/LavaArenaWithWalls.j3o"});
                            worldManager.preloadSoundEffects(new String[]{"FireballExplosion.wav",
                                "MeteorBoom.wav", "Shotgun.wav"});
                            nifty.gotoScreen("default_hud");
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
        return userCommandManager;
    }

    public void gotoMenu(final String menu) {
        enqueue(new Callable<Void>() {
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

    public RoundManager getRoundManager() {
        return roundManager;
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        userCommandManager.onLoseFocus();
    }
}