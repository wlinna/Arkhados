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
package arkhados.ui;

import arkhados.ClientMain;
import arkhados.Globals;
import arkhados.master.ClientMasterCommunicator;
import arkhados.master.Game;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import java.util.List;

public final class ConnectionMenu extends Menu {

    private TextRenderer statusText;
    private final ClientMasterCommunicator masterCommunicator
            = new ClientMasterCommunicator();         

    public void setStatusText(final String text) {
        Globals.app.enqueue(() -> {
            if (statusText != null) {
                statusText.setText(text);
            }
            return null;
        });
    }
    
    public void listGames(List<Game> games) {
        Element layer = screen.findElementById("layer_games");
        ListBox listbox = layer.findNiftyControl("lb_games", ListBox.class);
        
        listbox.clear();
        listbox.addAllItems(games);
    }
    
    public void closeGameList() {
        Element layer = screen.findElementById("layer_games");
        ListBox listbox = layer.findNiftyControl("lb_games", ListBox.class);
        listbox.clear();
        
        layer.hideWithoutEffect();        
    }
    
    public void selectGame() {
        ListBox listbox = screen.findNiftyControl("lb_games", ListBox.class);
        
        if (listbox.getSelection().isEmpty()) {
            return;
        }
        
        Game selection = (Game) listbox.getSelection().get(0);
        screen.findNiftyControl("server_ip", TextField.class)
                .setText(selection.address);
        screen.findNiftyControl("server_port", TextField.class)
                .setText("" + selection.port);
        closeGameList();
    }

    public void connect() {
        String username =
                screen.findNiftyControl("username_text", TextField.class)
                .getDisplayedText();

        if (username.trim().length() == 0) {
            setStatusText("Username is invalid");
            return;
        }

        int port = Integer.parseInt(screen.findNiftyControl("server_port",
                TextField.class).getDisplayedText());
        String address = screen.findNiftyControl("server_ip",
                TextField.class).getDisplayedText();

        setStatusText("Connecting... " + username);

        ClientMain clientMain = (ClientMain) Globals.app;
        clientMain.connect(username, address, port);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        super.bind(nifty, screen);
        statusText = screen.findElementById("status_text")
                .getRenderer(TextRenderer.class);
    }

    public void showGames() {
        screen.findElementById("layer_games").showWithoutEffects();
        masterCommunicator.setConnectionMenu(this);
        masterCommunicator.connectToMaster();
        masterCommunicator.requestGameList();
    }

    @Override
    public void onStartScreen() {
        ClientMain app = (ClientMain) Globals.app;
        Globals.replayMode = false;
        app.prepareForGame();
    }

    @Override
    public void onEndScreen() {
        setStatusText("");
        ((ClientMain) Globals.app).cancelConnectionIfNotDone();
        masterCommunicator.destroy();
    }
}
