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
package arkhados.ui.hud;

import com.jme3.input.controls.ActionListener;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;

import de.lessvoid.nifty.screen.Screen;

public class HudMenu implements ActionListener {

    private Nifty nifty;
    private Screen screen;

    void initialize(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed || nifty.getCurrentScreen() != screen) {
            return;
        }

        Element layer = screen.findElementByName("layer_settings");
        layer.setVisible(!layer.isVisible());
    }
    
    void cleanup()  {
        screen.findElementByName("layer_settings").hide();
    }
}
