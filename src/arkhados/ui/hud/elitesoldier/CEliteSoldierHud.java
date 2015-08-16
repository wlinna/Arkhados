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
package arkhados.ui.hud.elitesoldier;

import arkhados.controls.CCharacterHud;
import arkhados.ui.hud.ClientHud;
import arkhados.util.NiftyUtils;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

public class CEliteSoldierHud extends CCharacterHud {

    private Element pelletsCounter;
    private Element plasmaCounter;
    private Element rocketsCounter;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial == null && hud != null) {
            Element panel = hud.getScreen().findElementByName("panel_right");
            NiftyUtils.removeChildren(panel);
            pelletsCounter = plasmaCounter = rocketsCounter = null;
        }
    }

    @Override
    public void setHud(ClientHud hud) {
        super.setHud(hud);

        Element parent = hud.getScreen().findElementByName("panel_right");

        new AmmoIndicatorBuilder("pellets",
                "Interface/Images/SpellIcons/shotgun.png", "#fff")
                .build(hud.getNifty(), hud.getScreen(), parent);
        new AmmoIndicatorBuilder("plasma",
                "Interface/Images/SpellIcons/plasma.png", "#fff")
                .build(hud.getNifty(), hud.getScreen(), parent);
        new AmmoIndicatorBuilder("rockets",
                "Interface/Images/SpellIcons/rocket_launcher.png", "#111")
                .build(hud.getNifty(), hud.getScreen(), parent);

        pelletsCounter = hud.getScreen().findElementByName("pellets-counter");
        plasmaCounter = hud.getScreen().findElementByName("plasma-counter");
        rocketsCounter = hud.getScreen().findElementByName("rockets-counter");
    }

    public void updateAmmo(int pellets, int plasma, int rockets) {
        if (hud == null) {
            return;
        }

        pelletsCounter.getRenderer(TextRenderer.class).setText("" + pellets);
        plasmaCounter.getRenderer(TextRenderer.class).setText("" + plasma);
        rocketsCounter.getRenderer(TextRenderer.class).setText("" + rockets);
    }
}