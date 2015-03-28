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

import arkhados.Globals;
import arkhados.controls.CCharacterBuff;
import arkhados.effects.BuffEffect;
import com.jme3.audio.AudioNode;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.SceneGraphVisitorAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class PetrifyInformation extends BuffInformation {
    {
        // TODO: Find / make unique icon for Petrify
        setIconPath("Interface/Images/SpellIcons/SealingBoulder.png");
    }

    @Override
    public BuffEffect createBuffEffect(
            CCharacterBuff buffControl, float duration) {
        PetrifyEffect effect = new PetrifyEffect(duration);
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class PetrifyEffect extends BuffEffect {

    private Node characterNode = null;
    private List<Geometry> geometries = new ArrayList<>();
    private static ColorRGBA color = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);

    public PetrifyEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CCharacterBuff buffControl) {
        characterNode = (Node) buffControl.getSpatial();

        SceneGraphVisitor visitor = new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geom) {
                Material material = geom.getMaterial();
                MatParam param = material.getParam("Diffuse");
                if (param != null) {
                    geometries.add(geom);
                    material.setColor("Diffuse", color);
                }
            }
        };

        characterNode.depthFirstTraversal(visitor);

        AudioNode sound = new AudioNode(Globals.assetManager,
                "Effects/Sound/Petrify.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
        characterNode.attachChild(sound);
        sound.play();
    }

    @Override
    public void destroy() {
        super.destroy();

        for (Geometry geometry : geometries) {
            Material material = geometry.getMaterial();
            material.setColor("Diffuse", ColorRGBA.White);
        }
    }
}