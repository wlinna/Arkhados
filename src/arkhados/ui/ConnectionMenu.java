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
import java.util.concurrent.Callable;

public class ConnectionMenu extends Menu {

    private TextRenderer statusText;

    public void setStatusText(final String text) {
        Globals.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (statusText != null) {
                    statusText.setText(text);
                }
                return null;
            }
        });
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
        statusText = screen.findElementByName("status_text")
                .getRenderer(TextRenderer.class);
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
    }
}
