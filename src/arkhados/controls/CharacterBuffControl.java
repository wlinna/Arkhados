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
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */
public class CharacterBuffControl extends AbstractControl {

    private HashMap<Long, BuffEffect> buffs = new HashMap<Long, BuffEffect>();

    public void addBuff(final long buffId, final String buffName) {
        final BuffInformation buffInfo = BuffInformation.getBuffInformation(buffName);
        if (buffInfo == null) {
            System.out.println("No buffInfo for " + buffName + " id: " + buffId);
            return;
        }
        final BuffEffect buff = buffInfo.createBuffEffect(this);
        this.buffs.put(buffId, buff);
        System.out.println("Added buff " + buffName + " with id: " + buffId);
    }

    public void removeBuff(final long buffId) {
        final BuffEffect buffEffect = this.buffs.remove(buffId);
        // TODO: Investigate why buffEffect is sometimes null
        // NOTE: It seems that this happens mostly (or only) with Ignite
        if (buffEffect != null) {
            buffEffect.destroy();
        } else {
            System.out.println("buffEffect not in buffs!");
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (Map.Entry<Long, BuffEffect> entry : buffs.entrySet()) {
            BuffEffect buffEffect = entry.getValue();
            buffEffect.update(tpf);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        CharacterBuffControl control = new CharacterBuffControl();

        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
    }
}