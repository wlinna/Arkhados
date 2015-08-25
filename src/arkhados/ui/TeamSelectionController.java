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
import arkhados.messages.CmdSelectTeam;
import arkhados.net.Sender;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;

public class TeamSelectionController implements Controller {

    private Nifty nifty;
    private Screen screen;
    private Element element;

    @Override
    public void bind(Nifty nifty, Screen screen, Element elmnt,
            Parameters prmtrs) {
                this.nifty = nifty;
        this.screen = screen;
        element = elmnt;
    }

    @Override
    public void init(Parameters prmtrs) {
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onFocus(boolean getFocus) {
    }

    @Override
    public boolean inputEvent(NiftyInputEvent inputEvent) {
        return false;
    }

    public void selectTeam(String team) {
        Globals.app.getStateManager().getState(Sender.class)
                .addCommand(new CmdSelectTeam(team));
        nifty.removeElement(screen, element);
    }
}
