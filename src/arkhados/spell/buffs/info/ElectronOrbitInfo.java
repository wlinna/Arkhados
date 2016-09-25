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
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

public class ElectronOrbitInfo extends BuffInfo {

    {
        setIconPath("Interface/Images/SpellIcons/MineralArmor.png");
    }

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        ElectronOrbitEffect effect = new ElectronOrbitEffect(params.duration);
        effect.addToCharacter(params);
        return effect;
    }
}

class ElectronOrbitEffect extends BuffEffect {

    private Node centralNode = null;

    public ElectronOrbitEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(BuffInfoParameters params) {
        Node character = (Node) params.buffControl.getSpatial();
        Spatial crystals1 = assets.loadModel("Models/crystals.j3o");
        Spatial crystals2 = assets.loadModel("Models/crystals.j3o");
        Spatial crystals3 = assets.loadModel("Models/crystals.j3o");
                
        Material mat = new Material(assets, "MatDefs/Lava/Lava.j3md");
        mat.setFloat("Speed", 80f);

        Texture tex = assets.loadTexture("Textures/Plasma1.png");
        Texture noise = assets.loadTexture("Textures/noise3.png");
        tex.setWrap(Texture.WrapMode.MirroredRepeat);
        noise.setWrap(Texture.WrapMode.MirroredRepeat);
        mat.setTexture("Color", tex);
        mat.setTexture("Noise", noise);

        mat.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Additive);

        crystals1.setMaterial(mat);
        crystals2.setMaterial(mat);
        crystals3.setMaterial(mat);

        centralNode = new Node("mineral-armor-node");
        centralNode.setQueueBucket(RenderQueue.Bucket.Transparent);        

        centralNode.attachChild(crystals1);        
        centralNode.attachChild(crystals2);
        centralNode.attachChild(crystals3);

        crystals1.setLocalTranslation(0f, 0f, -7.5f);
        crystals2.setLocalTranslation(6.5f, 0f, 3.75f);
        crystals3.setLocalTranslation(-6.5f, 0f, 3.75f);

        Node world = character.getParent();
        world.attachChild(centralNode);
        
        centralNode.addControl(
                new CTrackLocation(character, new Vector3f(0f, 10f, 0f)));        
        centralNode.addControl(new CRotation(0f, 4f, 0f));
    }

    @Override
    public void destroy() {
        super.destroy();
        centralNode.removeFromParent();
    }
}
