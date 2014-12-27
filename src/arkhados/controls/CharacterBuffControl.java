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
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author william
 */
public class CharacterBuffControl extends AbstractControl {

    private static final Logger logger = Logger.getLogger(CharacterBuffControl.class.getName());
    private HashMap<Integer, BuffEffect> buffs = new HashMap<>();
    private HashMap<Integer, Element> buffIcons = new HashMap<>();
    private ClientHudManager hudManager = null;
    private Element buffPanel = null;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        
        if (spatial == null) {
            for (BuffEffect buffEffect : buffs.values()) {
                buffEffect.destroy();
            }
        }
    }
  
    public void addBuff(int buffId, int buffTypeId, float duration) {
        final BuffInformation buffInfo = BuffInformation.getBuffInformation(buffTypeId);
        if (buffInfo == null) {
            logger.log(Level.WARNING, "No buffinfo for type {0} . BuffId is {1}", new Object[]{buffTypeId, buffId});
            return;
        }
        final BuffEffect buff = buffInfo.createBuffEffect(this, duration);

        if (buff != null) {
            buffs.put(buffId, buff);
        }

        if (hudManager == null) {
            return;
        }

        String iconPath = buffInfo.getIconPath();
        if (iconPath == null) {
            iconPath = "Interface/Images/SpellIcons/placeholder.png";
        }

        final Element icon = new BuffIconBuilder("buff-" + buffId, iconPath)
                .build(hudManager.getNifty(), hudManager.getScreen(), buffPanel);

        buffIcons.put(buffId, icon);
    }

    public void removeBuff(int buffId) {
        final BuffEffect buffEffect = buffs.remove(buffId);
        // FIXME: Investigate why buffEffect is sometimes null
        // NOTE: It seems that this happens only with Ignite
        if (buffEffect != null) {
            buffEffect.destroy();
        } else {
            // FIXME: This seems to happen often!
            logger.log(Level.WARNING, "buffEffect with id {0} NOT IN buffs or IS null!", buffId);
        }

        if (hudManager == null) {
            return;
        }

        Element buffIcon = buffIcons.get(buffId);
        // FIXME: NullPointerException here. This is only workaround.
        if (buffIcon != null) {
            hudManager.getNifty().removeElement(hudManager.getScreen(), buffIcon);
        }

        buffIcons.remove(buffId);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (hudManager == null) {
            return;
        }

        for (Map.Entry<Integer, BuffEffect> entry : buffs.entrySet()) {
            BuffEffect buffEffect = entry.getValue();
            buffEffect.update(tpf);

            float cooldown = buffEffect.getTimeLeft();
            Element cooldownText = buffIcons.get(entry.getKey()).getElements().get(0);
            if (cooldown > 99) {
            } else if (cooldown > 3) {
                cooldownText.getRenderer(TextRenderer.class)
                        .setText(String.format("%d", (int) cooldown));
            } else if (cooldown > 0) {
                cooldownText.getRenderer(TextRenderer.class)
                        .setText(String.format("%.1f", cooldown));
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
        buffPanel = hudManager.getScreen().findElementByName("panel_buffs");      
    }        
}