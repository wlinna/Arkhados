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
import arkhados.controls.RotationControl;
import arkhados.effects.BuffEffect;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author william
 */
public class IncapacitateInformation extends BuffInformation {

    @Override
    public BuffEffect createBuffEffect(CharacterBuffControl buffControl, float duration) {
        IncapacitateEffect effect = new IncapacitateEffect(duration);
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class IncapacitateEffect extends BuffEffect {

    private Node centralNode = null;

    public IncapacitateEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CharacterBuffControl buffControl) {
        Sphere sphere = new Sphere(8, 8, 0.7f);

        Geometry geom1 = new Geometry("sphere", sphere);

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.White);
        geom1.setMaterial(material);

        Geometry geom2 = geom1.clone();

        centralNode = new Node("incapacitate-node");
        centralNode.attachChild(geom1);
        centralNode.attachChild(geom2);

        geom1.setLocalTranslation(-2.5f, 0f, 0f);
        geom2.setLocalTranslation(2.5f, 0f, 0f);

        Node characterNode = (Node) buffControl.getSpatial();
        characterNode.attachChild(centralNode);

        centralNode.setLocalTranslation(0f, 20f, 0f);        
        centralNode.addControl(new RotationControl(0f, 2f, 0f));
    }

    @Override
    public void destroy() {
        super.destroy();
        assert centralNode != null;
        centralNode.removeFromParent();
    }
}