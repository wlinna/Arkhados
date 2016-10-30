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
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class ElectronChargeInfo extends BuffInfo {

    {
        setIconPath("Interface/Images/SpellIcons/MineralArmor.png");
    }

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        ElectronChargeEffect effect = new ElectronChargeEffect(params.duration);
        effect.addToCharacter(params);
        return effect;
    }
}

// TODO: Handle stacks. Without stacks, it's hard to know how many charges
// the character has.
class ElectronChargeEffect extends BuffEffect {

    private Node centralNode = null;

    public ElectronChargeEffect(float timeLeft) {
        super(timeLeft);
    }

        static ParticleEmitter createPlasmaEmitter() {
        ParticleEmitter plasma = new ParticleEmitter("plasma-emitter",
                ParticleMesh.Type.Triangle, 20);
        Material materialRed = new Material(assets,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                assets.loadTexture("Effects/plasma-particle.png"));
        plasma.setMaterial(materialRed);
        plasma.setImagesX(2);
        plasma.setImagesY(2);
        plasma.setSelectRandomImage(true);
        plasma.setStartColor(new ColorRGBA(0.2f, 0.350f, 0.9f, 1.0f));
        plasma.setEndColor(new ColorRGBA(0.20f, 0.30f, 0.9f, 0.95f));
        plasma.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        plasma.setStartSize(1.5f);
        plasma.setEndSize(1.5f);
        plasma.setGravity(Vector3f.ZERO);
        plasma.setLowLife(0.05f);
        plasma.setHighLife(0.05f);
        plasma.setParticlesPerSec(100);

        plasma.setRandomAngle(true);
        return plasma;
    }
    
    public void addToCharacter(BuffInfoParameters params) {
        Node character = (Node) params.buffControl.getSpatial();


        centralNode = new Node("mineral-armor-node");
        centralNode.setQueueBucket(RenderQueue.Bucket.Transparent);

        Spatial[] crystals = new Spatial[params.stacks];
        float radius = 7.5f;
        float deltaAngle = FastMath.TWO_PI / crystals.length;

        for (int i = 0; i < crystals.length; i++) {
    
            crystals[i] = createPlasmaEmitter();
            centralNode.attachChild(crystals[i]);

            float x = radius * FastMath.cos(i * deltaAngle);
            float z = radius * FastMath.sin(i * deltaAngle);
            crystals[i].setLocalTranslation(x, 2f, z);
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
