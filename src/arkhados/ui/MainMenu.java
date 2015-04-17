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
import de.lessvoid.nifty.screen.Screen;


public class MainMenu extends Menu {

    @Override
    public void onStartScreen() {
        super.onStartScreen();
        ((ClientMain)Globals.app).cleanAppStatesAndHandlers();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        super.bind(nifty, screen);
    }        
}
