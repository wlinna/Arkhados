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
import arkhados.controls.CTrackLocation;
import arkhados.effects.BuffEffect;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class PurifyingFlameInfo extends BuffInfo {

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
    private static final Vector3f RELATIVE_POS = new Vector3f(0, 10f, 0);

    public FlameShield(float timeLeft) {
        super(timeLeft);
    }
    private Node node;
    private AudioNode sound;

    private Geometry createShield() {
        float radius = 12f;
        Sphere sphere = new Sphere(32, 32, radius);
        Geometry geometry = new Geometry("shield-geom", sphere);

        Material mat = new Material(assets, "MatDefs/Lava/Lava.j3md");
        mat.setFloat("Speed", 30f);

        Texture tex = assets.loadTexture("Textures/Fire6.png");
        Texture noise = assets.loadTexture("Textures/noise3.png");
        tex.setWrap(Texture.WrapMode.MirroredRepeat);
        noise.setWrap(Texture.WrapMode.MirroredRepeat);
        mat.setTexture("Color", tex);
        mat.setTexture("Noise", noise);

        mat.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Additive);

        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
        geometry.setMaterial(mat);

        return geometry;
    }

    public void addToCharacter(BuffInfoParameters params) {
        Node characterNode = (Node) params.buffControl.getSpatial();

        Geometry geometry = createShield();

        node = new Node("shield-node");
        node.attachChild(geometry);

        characterNode.getParent().attachChild(node);
        node.addControl(new CTrackLocation(characterNode, RELATIVE_POS));

        sound = new AudioNode(BuffEffect.assets,
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
