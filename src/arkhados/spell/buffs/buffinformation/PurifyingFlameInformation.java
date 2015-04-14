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

import arkhados.effects.BuffEffect;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author william
 */
public class PurifyingFlameInformation extends BuffInformation {

    {
        setIconPath("Interface/Images/SpellIcons/purifying_flame.png");
    }

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        FlameShield flameShield = new FlameShield(params.duration);
        flameShield.addToCharacter(params);
        return flameShield;
    }
}

class FlameShield extends BuffEffect {

    public FlameShield(float timeLeft) {
        super(timeLeft);
    }
    private Node node;
    private AudioNode sound;

    public void addToCharacter(BuffInfoParameters params) {
        Node characterNode = (Node) params.buffControl.getSpatial();

        float radius = 12f;
        Sphere sphere = new Sphere(32, 32, radius);
        Geometry geometry = new Geometry("shield-geom", sphere);
        node = new Node("shield-node");
        node.attachChild(geometry);

//        final Material material = BuffEffect.assetManager.loadMaterial("Materials/PurifyingMaterial.j3m");
        Material material = new Material(BuffEffect.assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        ColorRGBA color = ColorRGBA.Orange.clone();
        color.a = 0.2f;
        material.setColor("Color", color);

        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);

        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);;
        geometry.setMaterial(material);

        characterNode.attachChild(node);
        node.move(0f, 10f, 0f);

        sound = new AudioNode(BuffEffect.assetManager,
                "Effects/Sound/PurifyingFlame.wav");
        node.attachChild(sound);
        sound.setLooping(true);
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
        sound.play();
    }

    @Override
    public void destroy() {
        sound.stop();
        node.removeFromParent();
    }
}
