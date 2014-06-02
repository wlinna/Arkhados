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
package arkhados.controls;

import arkhados.effects.BuffEffect;
import arkhados.spell.buffs.buffinformation.BuffInformation;
import arkhados.ui.hud.BuffIconBuilder;
import arkhados.ui.hud.ClientHudManager;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */
public class CharacterBuffControl extends AbstractControl {

    private HashMap<Long, BuffEffect> buffs = new HashMap<>();
    private HashMap<Long, Element> buffIcons = new HashMap<>();
    private ClientHudManager hudManager = null;
    private Element buffPanel = null;

    public void addBuff(long buffId, String buffName, float duration) {
        final BuffInformation buffInfo = BuffInformation.getBuffInformation(buffName);
        if (buffInfo == null) {
            System.out.println("No buffInfo for " + buffName + " id: " + buffId);
            return;
        }
        final BuffEffect buff = buffInfo.createBuffEffect(this, duration);

        if (buff != null) {
            this.buffs.put(buffId, buff);
        }

        if (this.hudManager == null) {
            return;
        }

        String iconPath = buffInfo.getIconPath();
        if (iconPath == null) {
            iconPath = "Interface/Images/SpellIcons/placeholder.png";
        }

        final Element icon = new BuffIconBuilder("buff-" + buffId, iconPath)
                .build(hudManager.getNifty(), hudManager.getScreen(), this.buffPanel);

        this.buffIcons.put(buffId, icon);
    }

    public void removeBuff(long buffId) {
        final BuffEffect buffEffect = this.buffs.remove(buffId);
        // TODO: Investigate why buffEffect is sometimes null
        // NOTE: It seems that this happens mostly (or only) with Ignite
        if (buffEffect != null) {
            buffEffect.destroy();
        } else {
            System.out.println("buffEffect not in buffs!");
        }

        if (this.hudManager == null) {
            return;
        }

        this.hudManager.getNifty().removeElement(this.hudManager.getScreen(), this.buffIcons.get(buffId));
        this.buffIcons.remove(buffId);
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (Map.Entry<Long, BuffEffect> entry : buffs.entrySet()) {
            BuffEffect buffEffect = entry.getValue();
            buffEffect.update(tpf);

            float cooldown = buffEffect.getTimeLeft();
            Element cooldownText = this.buffIcons.get(entry.getKey()).getElements().get(0);
            if (cooldown > 99) {
            } else if (cooldown > 3) {
                cooldownText.getRenderer(TextRenderer.class).setText(String.format("%d", (int) cooldown));
            } else if (cooldown > 0) {
                cooldownText.getRenderer(TextRenderer.class).setText(String.format("%.1f", cooldown));
            } else if (cooldown < 0) {
                cooldownText.getRenderer(TextRenderer.class).setText("");
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    void setHudManager(ClientHudManager hudManager) {
        this.hudManager = hudManager;
        this.buffPanel = this.hudManager.getScreen().findElementByName("panel_right");
    }
}