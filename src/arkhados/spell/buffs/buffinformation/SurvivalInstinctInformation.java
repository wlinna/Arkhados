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
package arkhados.spell.buffs.buffinformation;

import arkhados.controls.CharacterBuffControl;
import arkhados.effects.BuffEffect;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SurvivalInstinctInformation extends BuffInformation {

    @Override
    public BuffEffect createBuffEffect(CharacterBuffControl buffControl) {
        final SurvivalInstinctEffect effect = new SurvivalInstinctEffect(super.getDuration());
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class SurvivalInstinctEffect extends BuffEffect {

    private Node characterNode = null;
    private static ColorRGBA color = new ColorRGBA(0.6f, 0.1f, 0.1f, 1f);

    public SurvivalInstinctEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CharacterBuffControl buffControl) {
        this.characterNode = (Node) buffControl.getSpatial();
        for (Spatial childSpatial : this.characterNode.getChildren()) {
            if (!(childSpatial instanceof Geometry)) {
                continue;
            }

            Geometry childGeometry = (Geometry) childSpatial;
            Material material = childGeometry.getMaterial();
            // FIXME: Material parameter not defined
            material.setColor("Diffuse", color);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        for (Spatial childSpatial : this.characterNode.getChildren()) {
            if (!(childSpatial instanceof Geometry)) {
                continue;
            }

            Geometry childGeometry = (Geometry) childSpatial;
            Material material = childGeometry.getMaterial();
            // FIXME: Material parameter is not defined
            MatParam param = material.getParam("Diffuse");
            if (param != null) {
                material.setColor("Diffuse", ColorRGBA.White);
            }

        }
    }
}