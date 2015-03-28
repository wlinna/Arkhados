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

import arkhados.spell.buffs.AbstractBuff;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class CSpellBuff extends AbstractControl {

    private List<AbstractBuff> buffs = new ArrayList<>();
    private CInfluenceInterface ownerInterface;

    public void addBuff(AbstractBuff buff) {
        this.buffs.add(buff);
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public List<AbstractBuff> getBuffs() {
        return this.buffs;
    }

    public CInfluenceInterface getOwnerInterface() {
        return this.ownerInterface;
    }

    public void setOwnerInterface(CInfluenceInterface ownerInterface) {
        this.ownerInterface = ownerInterface;
    }
}