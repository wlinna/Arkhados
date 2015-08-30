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

import arkhados.Globals;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class Menu implements ScreenController {

    protected Nifty nifty;
    protected Screen screen;

    public void gotoMenu(final String menu) {
        Globals.app.enqueue(() -> {
            nifty.gotoScreen(menu);
            return null;
        });
    }

    public void switchElement(String oldElement, String newElement) {
        nifty.getCurrentScreen().findElementById(oldElement).hide();
        nifty.getCurrentScreen().findElementById(newElement).show();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }
    
    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
    
    public void stop() {
        Globals.app.stop();
    }
}
