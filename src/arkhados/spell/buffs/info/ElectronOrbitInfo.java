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
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import java.util.List;

public class ElectronOrbitInfo extends BuffInfo {

    {
        setIconPath("Interface/Images/SpellIcons/ElectricCrystals.png");
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

        centralNode = new Node("mineral-armor-node");
        centralNode.setQueueBucket(RenderQueue.Bucket.Transparent);

        Spatial[] crystals = new Spatial[params.stacks];
        float radius = 7.5f;
        float deltaAngle = FastMath.TWO_PI / crystals.length;

        for (int i = 0; i < crystals.length; i++) {
            crystals[i] = assets.loadModel("Models/crystals.j3o");
            crystals[i].setMaterial(mat);
            centralNode.attachChild(crystals[i]);

            float x = radius * FastMath.cos(i * deltaAngle);
            float z = radius * FastMath.sin(i * deltaAngle);
            crystals[i].setLocalTranslation(x, 0f, z);
        }

        Node world = character.getParent();
        world.attachChild(centralNode);

        centralNode.addControl(
                new CTrackLocation(character, new Vector3f(0f, 10f, 0f)));
        centralNode.addControl(new CRotation(0f, 4f, 0f));
    }

    @Override
    public void setStacks(int stacks) {
        List<Spatial> children = centralNode.getChildren();
        int diff = children.size() - stacks;
        if (diff > 0) {
            int sentinel = children.size() - diff - 1;
            for (int i = children.size() - 1; i > sentinel; i--) {
                children.get(i).removeFromParent();
            }
        }        
    }

    @Override
    public void destroy() {
        super.destroy();
        centralNode.removeFromParent();
    }
}
