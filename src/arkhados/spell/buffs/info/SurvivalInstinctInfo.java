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
package arkhados.spell.buffs.info;

import arkhados.Globals;
import arkhados.controls.CTimedExistence;
import arkhados.effects.BuffEffect;
import com.jme3.audio.AudioNode;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class SurvivalInstinctInfo extends BuffInfo {

    {
        setIconPath("Interface/Images/SpellIcons/survival_instinct.png");
    }

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        SurvivalInstinctEffect effect =
                new SurvivalInstinctEffect(params.duration, true);
        effect.addToCharacter(params);
        return effect;
    }
}
class SurvivalInstinctEffect extends BuffEffect {  

    private Node characterNode = null;
    private ColorRGBA color;

    public SurvivalInstinctEffect(float timeLeft, boolean primary) {
        super(timeLeft);
        float red = primary ? 0.8f : 2f;
        color = new ColorRGBA(red, 0.1f, 0.1f, 1f);
    }

    public void addToCharacter(BuffInfoParameters params) {
        characterNode = (Node) params.buffControl.getSpatial();
        for (Spatial childSpatial : characterNode.getChildren()) {
            if (!(childSpatial instanceof Geometry)) {
                continue;
            }

            Geometry childGeometry = (Geometry) childSpatial;
            Material material = childGeometry.getMaterial();
            // TODO: Investingate if there is better solution for "Material parameter not defined"
            MatParam param = material.getParam("Diffuse");
            if (param != null) {
                material.setColor("Diffuse", color);
            }
        }

        if (params.justCreated) {
            AudioNode sound = new AudioNode(Globals.assets,
                    "Effects/Sound/SurvivalInstinct.wav");

            characterNode.attachChild(sound);
            sound.setPositional(true);
            sound.addControl(
                    new CTimedExistence(sound.getAudioData().getDuration()));
            sound.setReverbEnabled(false);
            sound.setVolume(1f);
            sound.play();
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
            // TODO: Investingate if there is better solution for "Material parameter not defined"
            MatParam param = material.getParam("Diffuse");
            if (param != null) {
                material.setColor("Diffuse", ColorRGBA.White);
            }
        }
    }
}