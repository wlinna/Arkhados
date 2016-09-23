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
import arkhados.controls.CCharacterPhysics;
import arkhados.effects.BuffEffect;
import arkhados.effects.Lightning;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

public class ElectroChargeInfo extends BuffInfo {

    {
        setIconPath("Interface/Images/SpellIcons/survival_instinct.png");
    }

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        ElectroChargeEffect effect
                = new ElectroChargeEffect(params.duration, true);
        effect.addToCharacter(params);
        return effect;
    }
}

class ElectroChargeEffect extends BuffEffect {

    private Node characterNode = null;
    private final ColorRGBA color;
    private final List<Spatial> electricities = new ArrayList<>();
    private final List<Geometry> geometries = new ArrayList<>();

    public ElectroChargeEffect(float timeLeft, boolean primary) {
        super(timeLeft);
        float red = primary ? 0.8f : 2f;
        color = new ColorRGBA(1f, 1f, 1f, 1f);
    }

    public void addToCharacter(BuffInfoParameters params) {
        characterNode = (Node) params.buffControl.getSpatial();

        float height = characterNode.getControl(CCharacterPhysics.class)
                .getCapsuleShape().getHeight();
        Material material
                = Globals.assets.loadMaterial("Materials/ZapLightning.j3m");
        material.setFloat("LateralFactor", 0.3f);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
        

        int bolts = 20;
        for (int i = 0; i < bolts; i++) {
            Mesh mesh = Lightning.createGeometry(0.0f, 0.1f, 0.1f);
            Geometry geom = new Geometry("electricity-" + i, mesh);
            geom.setQueueBucket(RenderQueue.Bucket.Translucent);

            Node node = new Node("electricity-node-" + i);
            node.attachChild(geom);

            float factor = i / (float) bolts;
            geom.move(0f, 0f, 0.5f);
            geom.scale(7f);
            node.rotate(
                    FastMath.rand.nextFloat()
                    * FastMath.HALF_PI, factor * FastMath.TWO_PI, 0);

            node.setLocalTranslation(0f, height, 0);

            geom.setMaterial(material);
            electricities.add(node);
            characterNode.attachChild(node);
        }

        SceneGraphVisitor visitor = new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geom) {
                Material material = geom.getMaterial();
                MatParam param = material.getParam("Diffuse");
                if (param != null) {
                    geometries.add(geom);
//                    material.setColor("Diffuse", color);
                    material.setColor("Specular", color);
                    material.setFloat("Shininess", 60f);
                }
            }
        };

        
        characterNode.depthFirstTraversal(visitor);

         // TODO: Add sound effect
//        if (params.justCreated) {
//            AudioNode sound = new AudioNode(Globals.assets,
//                    "Effects/Sound/SurvivalInstinct.wav");
//
//            characterNode.attachChild(sound);
//            sound.setPositional(true);
//            sound.addControl(
//                    new CTimedExistence(sound.getAudioData().getDuration()));
//            sound.setReverbEnabled(false);
//            sound.setVolume(1f);
//            sound.play();
//        }
    }

    @Override
    public void destroy() {
        super.destroy();
        for (Spatial electricity : electricities) {
            electricity.removeFromParent();
        }

        for (Geometry geometry : geometries) {
            Material material = geometry.getMaterial();
            material.setColor("Diffuse", ColorRGBA.White);
            material.setColor("Specular", ColorRGBA.Black);
        }
    }
}
