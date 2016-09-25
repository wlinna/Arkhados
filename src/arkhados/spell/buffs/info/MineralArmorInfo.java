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

import arkhados.controls.CRotation;
import arkhados.controls.CTrackLocation;
import arkhados.effects.BuffEffect;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class MineralArmorInfo extends BuffInfo {

    {
        setIconPath("Interface/Images/SpellIcons/MineralArmor.png");
    }

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        MineralArmorEffect effect = new MineralArmorEffect(params.duration);
        effect.addToCharacter(params);
        return effect;
    }
}

class MineralArmorEffect extends BuffEffect {

    private Node centralNode = null;

    public MineralArmorEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(BuffInfoParameters params) {
        Node character = (Node) params.buffControl.getSpatial();
        Spatial crystals1 = assets.loadModel("Models/crystals.j3o");
        Spatial crystals2 = assets.loadModel("Models/crystals.j3o");
        Spatial crystals3 = assets.loadModel("Models/crystals.j3o");
        Spatial crystals4 = assets.loadModel("Models/crystals.j3o");

        centralNode = new Node("mineral-armor-node");
        centralNode.attachChild(crystals1);
        centralNode.attachChild(crystals2);
        centralNode.attachChild(crystals3);
        centralNode.attachChild(crystals4);

        crystals1.setLocalTranslation(-7.5f, 0f, 0f);
        crystals2.setLocalTranslation(7.5f, 0f, 0f);
        crystals3.setLocalTranslation(0f, 0f, -7.5f);
        crystals4.setLocalTranslation(0f, 0f, 7.5f);

        Node world = character.getParent();
        world.attachChild(centralNode);
        
        centralNode.addControl(
                new CTrackLocation(character, new Vector3f(0f, 10f, 0f)));        
        centralNode.addControl(new CRotation(0f, 2f, 0f));
    }

    @Override
    public void destroy() {
        super.destroy();
        centralNode.removeFromParent();
    }
}
