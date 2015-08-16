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
import arkhados.controls.CTimedExistence;
import arkhados.spell.spells.elitesoldier.RocketLauncher;
import com.jme3.audio.AudioNode;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class RocketExplosionEffect implements WorldEffect {

    public RocketExplosionEffect() {
    }

    private ParticleEmitter createSmokePuff() {
        ParticleEmitter smokePuff = new ParticleEmitter("smoke-puff",
                ParticleMesh.Type.Triangle, 20);
        Material mat = new Material(Globals.assets,
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture",
                Globals.assets.loadTexture("Effects/flame_alpha.png"));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        smokePuff.setMaterial(mat);
        smokePuff.setImagesX(2);
        smokePuff.setImagesY(2);
        smokePuff.setSelectRandomImage(true);
        smokePuff.setStartColor(new ColorRGBA(0.3f, 0.3f, 0.3f, 0.3f));
        smokePuff.setEndColor(new ColorRGBA(0.3f, 0.3f, 0.3f, 0.1f));

        smokePuff.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(5.0f));
        smokePuff.getParticleInfluencer().setVelocityVariation(1f);

        smokePuff.setStartSize(8.0f);
        smokePuff.setEndSize(30.0f);
        smokePuff.setGravity(Vector3f.ZERO);
        smokePuff.setLowLife(1.75f);
        smokePuff.setHighLife(6f);
        smokePuff.setParticlesPerSec(0);

        smokePuff.setRandomAngle(true);

        smokePuff.setShape(new EmitterSphereShape(Vector3f.ZERO, 4.0f));

        return smokePuff;
    }

    private ParticleEmitter createFire() {
        ParticleEmitter fire = new ParticleEmitter("fire-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material material = new Material(Globals.assets,
                "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                Globals.assets.loadTexture("Effects/flame.png"));
        fire.setMaterial(material);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setGravity(Vector3f.ZERO);
        fire.setParticlesPerSec(100);
        fire.setRandomAngle(true);
        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 0.40f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.0f));
        fire.setLowLife(0.4f);
        fire.setHighLife(0.5f);
        fire.setNumParticles(120);
        fire.setStartSize(7.50f);
        fire.setEndSize(25f);
        fire.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(45.0f));
        fire.getParticleInfluencer().setVelocityVariation(1f);
        return fire;
    }

    private ParticleEmitter createShockwave() {
        ParticleEmitter wave = new ParticleEmitter("shockwave-emitter",
                ParticleMesh.Type.Triangle, 2);
        Material materialRed = new Material(Globals.assets,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                Globals.assets.loadTexture("Effects/shockwave.png"));
        wave.setMaterial(materialRed);
        wave.setImagesX(1);
        wave.setImagesY(1);

        wave.setGravity(Vector3f.ZERO);

        wave.setStartColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f));
        wave.setEndColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 0f));
        wave.setLowLife(0.5f);
        wave.setHighLife(0.5f);
        wave.setStartSize(0.50f);
        wave.setEndSize(RocketLauncher.SPLASH_RADIUS + 7f);
        wave.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        wave.getParticleInfluencer().setVelocityVariation(0f);
        wave.setParticlesPerSec(0f);

        return wave;
    }

    @Override
    public EffectHandle execute(Node root, Vector3f location, String p) {
        ParticleEmitter fire = createFire();
        ParticleEmitter smoke = createSmokePuff();
        ParticleEmitter wave = createShockwave();

        Node explosion = new Node("explosion");
        root.attachChild(explosion);
        explosion.setLocalTranslation(location);
        explosion.move(0f, 3f, 0f);

        AudioNode sound = new AudioNode(Globals.assets,
                "Effects/Sound/FireballExplosion.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(5f);

        explosion.attachChild(fire);
        explosion.attachChild(smoke);
        explosion.attachChild(wave);
        explosion.attachChild(sound);        


        
        explosion.addControl(new CTimedExistence(5f));

        fire.emitAllParticles();
        smoke.emitAllParticles();
        wave.emitAllParticles();

        fire.setParticlesPerSec(0);
        smoke.setParticlesPerSec(0);


        sound.play();
        return null;
    }
}
