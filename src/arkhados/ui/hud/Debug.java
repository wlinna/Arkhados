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

import arkhados.debug.ChangeCharacaterMaterial;
import com.jme3.input.controls.ActionListener;
import com.jme3.scene.Node;

public class Debug implements ActionListener {

    private final ChangeCharacaterMaterial changeMat
            = new ChangeCharacaterMaterial();

    public void setCharacter(Node character) {
        changeMat.node = character;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            return;
        }

        if (changeMat != null) {
            changeMat.next();
        }
    }

}
