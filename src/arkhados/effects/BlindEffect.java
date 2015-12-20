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
package arkhados.effects;

import arkhados.Globals;
import arkhados.controls.CRotation;
import arkhados.effects.particle.ParticleEmitter;
import arkhados.spell.buffs.info.BuffInfoParameters;
import arkhados.util.UserData;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

public class BlindEffect extends BuffEffect {

    public int entityId = -1;

    private Node centralNode;
    private Vector3f vec = Vector3f.UNIT_XYZ.mult(14f);

    public BlindEffect(float timeLeft) {
        super(timeLeft);
    }

    private ParticleEmitter createEmitter() {
        ParticleEmitter spark = new ParticleEmitter("sparkle-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));
        spark.setMaterial(mat);
        spark.setImagesX(2);
        spark.setImagesY(2);
        spark.setSelectRandomImage(true);
        spark.setStartColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        spark.setEndColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1f));
        spark.getParticleInfluencer().setInitialVelocity(vec);
        spark.getParticleInfluencer().setVelocityVariation(0.8f);
        spark.setStartSize(1f);
        spark.setEndSize(1.0f);
        spark.setGravity(Vector3f.ZERO);
        spark.setLowLife(0.1f);
        spark.setHighLife(0.1f);
        spark.setParticlesPerSec(100);

        spark.setRandomAngle(true);
        return spark;
    }

    public void addToCharacter(BuffInfoParameters params) {
        ClientBlind blind = Globals.app.getStateManager()
                .getState(ClientBlind.class);
        blind.addBlindIfSelf(this, params);
        entityId = params.buffControl.getSpatial()
                .getUserData(UserData.ENTITY_ID);

        Sphere sphere = new Sphere(8, 8, 0.7f);
        Geometry geom = new Geometry("sphere", sphere);

        centralNode = new Node("blind-node");

        ParticleEmitter emitter = createEmitter();
        centralNode.attachChild(emitter);
//        centralNode.attachChild(geom);

        emitter.setLocalTranslation(-2.5f, 0f, 0f);
        
        geom.setLocalTranslation(-2.5f, 0f, 0f);

        Node characterNode = (Node) params.buffControl.getSpatial();

        characterNode.attachChild(centralNode);

        centralNode.setLocalTranslation(0f, 20f, 0f);
        centralNode.addControl(new CRotation(0f, 12f, 0f));
    }

    @Override
    public void destroy() {
        super.destroy();
        ClientBlind blindManager
                = Globals.app.getStateManager().getState(ClientBlind.class
                );
        blindManager.removeBuffIfSelf(
                this);

        centralNode.removeFromParent();
    }
}
