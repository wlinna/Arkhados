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
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.concurrent.Callable;

public class Menu implements ScreenController {

    private Nifty nifty;
    private TextRenderer statusText;

    private void setNifty(Nifty nifty) {
        this.nifty = nifty;
        statusText = nifty.getScreen("join_server")
                .findElementByName("status_text")
                .getRenderer(TextRenderer.class);
    }

    public void setStatusText(final String text) {
        Globals.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                statusText.setText(text);
                return null;
            }
        });
    }

    public void gotoMenu(final String menu) {
        Globals.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                nifty.gotoScreen(menu);
                return null;
            }
        });
    }

    public void switchElement(String oldElement, String newElement) {
        nifty.getCurrentScreen().findElementByName(oldElement).hide();
        nifty.getCurrentScreen().findElementByName(newElement).show();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        setNifty(nifty);
    }

    public void connect() {
        Screen joinScreen = nifty.getScreen("join_server");
        String username =
                joinScreen.findNiftyControl("username_text", TextField.class)
                .getDisplayedText();

        if (username.trim().length() == 0) {
            setStatusText("Username is invalid");
            return;
        }

        int port = Integer.parseInt(joinScreen.findNiftyControl("server_port",
                TextField.class).getDisplayedText());
        String address = joinScreen.findNiftyControl("server_ip",
                TextField.class).getDisplayedText();

        setStatusText("Connecting... " + username);

        ClientMain clientMain = (ClientMain) Globals.app;
        clientMain.connect(username, address, port);
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
}
