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
import java.util.logging.Level;
import java.util.logging.Logger;
import arkhados.messages.ChatMessage;
import arkhados.messages.MessageUtils;
import arkhados.messages.roundprotocol.NewRoundMessage;
import arkhados.messages.PlayerDataTableMessage;
import arkhados.messages.roundprotocol.RoundFinishedMessage;
import arkhados.messages.ServerLoginMessage;
import arkhados.messages.SetPlayersCharacterMessage;
import arkhados.messages.StartGameMessage;
import arkhados.messages.syncmessages.AddEntityMessage;
import arkhados.messages.syncmessages.RemoveEntityMessage;
import arkhados.messages.syncmessages.StartCastingSpellMessage;
import arkhados.messages.syncmessages.SyncCharacterMessage;
import arkhados.messages.syncmessages.SyncProjectileMessage;
import arkhados.util.InputMappingStrings;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import de.lessvoid.nifty.controls.Button;
import java.util.prefs.BackingStoreException;

public class ClientMain extends SimpleApplication implements ScreenController {

    public final static String PREFERENCES_KEY = "arkhados";

    public static void setKeySettings(AppSettings settings) {
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
        Logger.getLogger("").setLevel(Level.WARNING);
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE);
        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);
        AppSettings settings = new AppSettings(true);

        try {
            settings.load(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger("").warning("Could not load preferences");
        }
        setKeySettings(settings);
        settings.setFrameRate(60);
        settings.setTitle("Arkhados Client");
        ClientMain app = new ClientMain();
        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.start();

    }
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private TextRenderer statusText;
    private NetworkClient client;
    private WorldManager worldManager;
    private ClientNetListener listenerManager;
    private SyncManager syncManager;
    private BulletAppState bulletState;
    private UserCommandManager userCommandManager;
    private ClientHudManager clientHudManager;
    private RoundManager roundManager;

    @Override
    public void simpleInitApp() {
        this.setDisplayStatView(false);
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
        this.client = Network.createClient();

        this.syncManager = new SyncManager(this, this.client);
        this.stateManager.attach(this.syncManager);
        this.syncManager.setMessagesToListen(AddEntityMessage.class, RemoveEntityMessage.class, SyncCharacterMessage.class, SyncProjectileMessage.class, StartCastingSpellMessage.class);
        this.worldManager = new WorldManager();

        this.userCommandManager = new UserCommandManager(this.client, this.inputManager);


        this.listenerManager = new ClientNetListener(this, client, this.worldManager);
        this.client.addClientStateListener(this.listenerManager);
        this.client.addMessageListener(this.listenerManager,
                ServerLoginMessage.class, PlayerDataTableMessage.class,
                ChatMessage.class, StartGameMessage.class, SetPlayersCharacterMessage.class);

        MessageUtils.registerMessages();
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
        this.nifty.fromXml("Interface/ClientUI.xml", "main_menu", this, new KeySetter(this, this.inputManager));
        this.guiViewPort.addProcessor(this.niftyDisplay);

        this.statusText = this.nifty.getScreen("join_server")
                .findElementByName("layer").findElementByName("panel")
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
                .findElementByName("layer").findElementByName("panel")
                .findElementByName("username_text")
                .getControl(TextFieldControl.class).getText();

        final int port = Integer.parseInt(nifty.getScreen("join_server").findElementByName("layer")
                .findElementByName("panel").findElementByName("server_port").getControl(TextFieldControl.class).getText());
        final String ip = nifty.getScreen("join_server").findElementByName("layer")
                .findElementByName("panel").findElementByName("server_ip").getControl(TextFieldControl.class).getText();


        if (username.trim().length() == 0) {
            this.setStatusText("Username is invalid");
            return;
        }
        this.listenerManager.setName(username);

        System.out.println("Trying to connect");
        this.setStatusText("Connecting... " + username);
        try {
            this.client.connectToServer(ip, port, port);
            this.client.start();

            this.toLobby();
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
    public void refreshPlayerData(final List<String> playerData) {
        this.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                Screen screen = ClientMain.this.nifty.getScreen("lobby");
                if (screen == null) {
                    System.out.println("Screen is null");
                }
                ListBox listBox = screen.findNiftyControl("players_list", ListBox.class);
                listBox.clear();
                listBox.addAllItems(playerData);

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
                ClientMain.this.client.send(new ChatMessage(
                        ClientMain.this.listenerManager.getName(),
                        textField.getText()));
                textField.setText("");
                return null;
            }
        });
    }

    public void sendStartGameRequest() {
        this.client.send(new StartGameMessage());
    }

    public void startGame() {
//        this.enqueue(new Callable<Void>() {
//
//            public Void call() throws Exception {
//                ClientMain.this.nifty.gotoScreen("load_level");
//                return null;
//            }
//        });


        this.flyCam.setEnabled(false);
//        this.inputManager.setCursorVisible(false);

        new Thread(new Runnable() {
            public void run() {
                try {
                    ClientMain.this.enqueue(new Callable<Void>() {
                        public Void call() throws Exception {
                            worldManager.preloadModels(new String[]{"Models/Mage.j3o", "Models/Circle.j3o"});
                            ClientMain.this.worldManager.loadLevel();
                            ClientMain.this.nifty.gotoScreen("default_hud");
                            return null;
                        }
                    }).get();
                    ClientMain.this.enqueue(new Callable<Void>() {
                        public Void call() throws Exception {
//                            ClientMain.this.worldManager.attachLevel();
                            return null;
                        }
                    }).get();
                    ClientMain.this.enqueue(new Callable<Void>() {
                        public Void call() throws Exception {
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

    public void bind(Nifty nifty, Screen screen) {
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }

    public void closeApplication() {
        this.stop();
    }


    @Override
    public void destroy() {
        if (this.client.isConnected()) {
            this.client.close();
        }
        super.destroy();
    }

    public RoundManager getRoundManager() {
        return this.roundManager;
    }
}
