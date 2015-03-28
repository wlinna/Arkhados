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

import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import arkhados.util.InputMappingStrings;
import arkhados.util.NiftyUtils;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import java.util.HashMap;
import java.util.Map;

public class SpellBar {

    private Nifty nifty;
    private Screen screen;
    private final Map<String, Element> icons = new HashMap<>(6);
    private Spatial playerCharacter;

    void loadIcons() {
        addIcon(InputMappingStrings.getId(InputMappingStrings.M1));
        addIcon(InputMappingStrings.getId(InputMappingStrings.M2));
        addIcon(InputMappingStrings.getId(InputMappingStrings.Q));
        addIcon(InputMappingStrings.getId(InputMappingStrings.E));
        addIcon(InputMappingStrings.getId(InputMappingStrings.R));
        addIcon(InputMappingStrings.getId(InputMappingStrings.SPACE));
    }

    void addIcon(int key) {
        Element bottomPanel = screen.findElementByName("panel_spells");
        CSpellCast castControl =
                playerCharacter.getControl(CSpellCast.class);
        Spell spell = castControl.getKeySpellNameMapping(key);

        if (spell == null) {
            return;
        }

        String iconPath;
        if (spell.getIconName() != null) {
            iconPath = "Interface/Images/SpellIcons/" + spell.getIconName();
        } else {
            iconPath = "Interface/Images/SpellIcons/placeholder.png";
        }
        icons.put(spell.getName(), new SpellIconBuilder(spell.getName(),
                iconPath).build(nifty, screen, bottomPanel));
    }

    void updateIcons() {
        if (playerCharacter == null) {
            return;
        }
        CSpellCast castControl =
                playerCharacter.getControl(CSpellCast.class);

        for (Map.Entry<String, Element> entry : icons.entrySet()) {
            float cooldown = castControl.getCooldown(Spell
                    .getSpell(entry.getKey()).getId());
            Element overlay = entry.getValue()
                    .findElementByName(entry.getKey() + "-overlay");
            if (cooldown <= 0) {
                if (overlay.isVisible()) {
                    overlay.hide();
                }
            } else {
                if (!overlay.isVisible()) {
                    overlay.show();
                }

                Element cooldownText = overlay
                        .findElementByName(entry.getKey() + "-counter");

                if (cooldown > 3) {
                    cooldownText.getRenderer(TextRenderer.class)
                            .setText(String.format("%d", (int) cooldown));
                } else {
                    cooldownText.getRenderer(TextRenderer.class)
                            .setText(String.format("%.1f", cooldown));
                }
            }
        }
    }

    void clean() {
        icons.clear();
        NiftyUtils.removeChildren(screen, "panel_spells");        
        setPlayerCharacter(null);
    }

    void setNifty(Nifty nifty) {
        this.nifty = nifty;
    }

    void setScreen(Screen screen) {
        this.screen = screen;
    }

    void setPlayerCharacter(Spatial playerCharacter) {
        this.playerCharacter = playerCharacter;
    }
}
