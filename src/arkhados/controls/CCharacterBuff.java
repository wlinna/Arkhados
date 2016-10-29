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
import arkhados.messages.sync.CmdBuff;
import arkhados.spell.buffs.info.BuffInfoParameters;
import arkhados.spell.buffs.info.BuffInfo;
import arkhados.spell.buffs.info.FakeBuff;
import arkhados.ui.hud.BuffIconBuilder;
import arkhados.ui.hud.ClientHud;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.IntMap;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CCharacterBuff extends AbstractControl {

    private static final Logger logger
            = Logger.getLogger(CCharacterBuff.class.getName());
    private final IntMap<BuffEffect> effects = new IntMap<>();
    private final IntMap<Element> buffIcons = new IntMap<>();
    private final IntMap<FakeBuff> buffs = new IntMap<>();
    private ClientHud hud = null;
    private Element buffPanel = null;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial == null) {
            clear();
        }
    }

    public void addBuff(CmdBuff cBuff) {
        int buffId = cBuff.buffId;
        int buffTypeId = cBuff.buffTypeId;
        float duration = cBuff.duration;
        int stacks = cBuff.stacks;
        boolean justCreated = cBuff.getJustCreated();

        buffs.put(buffId, new FakeBuff(buffTypeId));

        BuffInfo buffInfo = BuffInfo.getBuffInfo(buffTypeId);
        if (buffInfo == null) {
            logger.log(Level.FINE, "No buffinfo for type {0} . BuffId is {1}",
                    new Object[]{buffTypeId, buffId});
            return;
        }

        BuffInfoParameters infoParams
                = new BuffInfoParameters(this, duration, justCreated, stacks);

        BuffEffect buff = buffInfo.createBuffEffect(infoParams);

        if (buff != null) {
            BuffEffect get = effects.get(buffId);
            if (get != null) {
                logger.log(Level.WARNING, "Buffs already has buff with id {0}",
                        buffId);
            }

            effects.put(buffId, buff);
        }

        if (hud == null) {
            return;
        }

        String iconPath = buffInfo.getIconPath();
        if (iconPath == null) {
            iconPath = "Interface/Images/SpellIcons/placeholder.png";
        }

        Element icon = new BuffIconBuilder("buff-" + buffId, iconPath).build(
                hud.getNifty(), hud.getScreen(), buffPanel);

        buffIcons.put(buffId, icon);
    }

    public void changeStacks(int buffId, int stacks) {
        FakeBuff buff = buffs.get(buffId);
        if (buff != null) {
            buff.stacks = stacks;
        }

        BuffEffect fx = effects.get(buffId);
        if (fx != null) {
            fx.setStacks(stacks);
        }
    }

    public void removeBuff(int buffId) {
        buffs.remove(buffId);
        BuffEffect buffEffect = effects.remove(buffId);
        // FIXME: Investigate why buffEffect is sometimes null
        // NOTE: This seems to happen at least with Slow and Ignite
        if (buffEffect != null) {
            buffEffect.destroy();
        } else {
            // FIXME: This seems to happen often!
            logger.log(Level.WARNING,
                    "buffEffect with id {0} NOT IN buffs or IS null!", buffId);
        }

        if (hud == null) {
            return;
        }

        Element buffIcon = buffIcons.get(buffId);
        // FIXME: NullPointerException here. This is only workaround.
        if (buffIcon != null) {
            buffIcon.markForRemoval();
        }

        buffIcons.remove(buffId);
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (IntMap.Entry<BuffEffect> entry : effects) {
            BuffEffect buffEffect = entry.getValue();
            buffEffect.update(tpf);

            if (hud == null) {
                continue;
            }

            float cooldown = buffEffect.getTimeLeft();
            // FIXME: java.lang.IndexOutOfBoundsException
            // Workaroud:
            List<Element> cooldownChildren
                    = buffIcons.get(entry.getKey()).getChildren();
            if (cooldownChildren.isEmpty()) {
                logger.warning("cooldown element is empty");
                continue;
            }

            Element cooldownText = cooldownChildren.get(0);
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

    void setHud(ClientHud hud) {
        this.hud = hud;
        buffPanel = hud.getScreen().findElementByName("panel_buffs");
    }

    public boolean hasBuff(int typeId) {
        for (IntMap.Entry<FakeBuff> fakeBuff : buffs) {
            if (fakeBuff.getValue().typeId == typeId) {
                return true;
            }
        }

        return false;
    }

    public IntMap<FakeBuff> getBuffs() {
        return buffs;
    }

    private void clear() {
        for (IntMap.Entry<BuffEffect> effect : effects) {
            effect.getValue().destroy();
        }

        effects.clear();
        buffIcons.clear();
        buffs.clear();
    }
}
