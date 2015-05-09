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

import arkhados.actions.ADelay;
import arkhados.actions.EntityAction;
import arkhados.controls.CActionQueue;
import arkhados.controls.CTimedExistence;
import arkhados.effects.BuffEffect;
import arkhados.spell.buffs.MagmaReleaseBuff;
import arkhados.util.BuffTypeIds;
import com.jme3.audio.AudioNode;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class MagmaReleaseInformation extends BuffInformation {

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        int count = 0;

        for (FakeBuff buff : params.buffControl.getBuffs().values()) {
            if (buff.typeId == BuffTypeIds.BRIMSTONE) {
                count = buff.stacks;
                break;
            }
        }

        MagmaReleaseEffect effect =
                new MagmaReleaseEffect(params.duration, count);
        effect.addToCharacter(params);
        return effect;
    }
}

class MagmaReleaseEffect extends BuffEffect {

    private int explosionCount;
    private Node character;

    public MagmaReleaseEffect(float timeLeft, int explosionCount) {
        super(timeLeft);
        this.explosionCount = explosionCount;
    }

    public void addToCharacter(BuffInfoParameters params) {
        character = (Node) params.buffControl.getSpatial();

        for (int i = 0; i < explosionCount; i++) {
            final Node node = new Node();
            character.attachChild(node);
            
            final ParticleEmitter fire = createFireEmitter();
            node.attachChild(fire);

            CActionQueue actionQueue = new CActionQueue();
            node.addControl(actionQueue);
            float delay = MagmaReleaseBuff.TICK_LENGTH * i;
            actionQueue.enqueueAction(new ADelay(delay));
            actionQueue.enqueueAction(new EntityAction() {
                @Override
                public boolean update(float tpf) {
                    fire.emitAllParticles();
                    AudioNode sound = new AudioNode(assetManager,
                            "Effects/Sound/FireballExplosion.wav");
                    sound.setVolume(0.8f);
                    sound.setPositional(true);
                    sound.setReverbEnabled(false);
                    sound.setVolume(1f);
                    node.attachChild(sound);
                    sound.play();
                    return false;
                }
            });
            node.addControl(new CTimedExistence(15f));
            character.attachChild(node);
        }
    }

    private ParticleEmitter createFireEmitter() {
        ParticleEmitter fire = new ParticleEmitter("fire-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material materialRed = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                assetManager.loadTexture("Effects/flame.png"));
        fire.setMaterial(materialRed);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setRandomAngle(true);


        fire.setGravity(Vector3f.ZERO);

        fire.setStartColor(new ColorRGBA(0.95f, 0.15f, 0f, 0.4f));
        fire.setEndColor(new ColorRGBA(1f, 1f, 0f, 0f));
        fire.setLowLife(0.1f);
        fire.setHighLife(0.3f);
        fire.setNumParticles(100);
        fire.setStartSize(0.50f);
        fire.setEndSize(30f);
        fire.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(15f));
        fire.getParticleInfluencer().setVelocityVariation(1f);

        fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));

        fire.setParticlesPerSec(0f);

        return fire;
    }
}
