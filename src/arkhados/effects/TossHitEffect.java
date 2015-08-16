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
import arkhados.spell.spells.rockgolem.Toss;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.RadialParticleInfluencer;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class TossHitEffect implements WorldEffect {

    private ParticleEmitter createParticleEffect() {
        ParticleEmitter dust = new ParticleEmitter("smoke-puff",
                ParticleMesh.Type.Triangle, 80);
        Material material = new Material(Globals.assets,
                "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                Globals.assets.loadTexture("Effects/flame_alpha.png"));
        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);
        dust.setMaterial(material);
        dust.setImagesX(2);
        dust.setImagesY(2);
        dust.setSelectRandomImage(true);
        dust.setStartColor(new ColorRGBA(0.3f, 0.15f, 0.03f, 0.3f));
        dust.setEndColor(new ColorRGBA(0.3f, 0.15f, 0.03f, 0.01f));

        RadialParticleInfluencer influencer = new RadialParticleInfluencer();
        influencer.setOrigin(Vector3f.ZERO);
        influencer.setHorizontal(false);
        influencer.setRadialVelocity(Toss.SPLASH_RADIUS / 0.5f);
        
        dust.setParticleInfluencer(influencer);

        dust.setStartSize(15.0f);
        dust.setEndSize(30.0f);
        dust.setGravity(Vector3f.ZERO);
        dust.setLowLife(0.5f);
        dust.setHighLife(0.5f);
        dust.setParticlesPerSec(0);

        dust.setRandomAngle(true);

        dust.setShape(new EmitterCircleShape(Vector3f.ZERO, 2.0f));

        return dust;
    }

    @Override
    public EffectHandle execute(Node root, Vector3f location, String p) {
        SimpleSoundEffect earthQuake =
                new SimpleSoundEffect("Effects/Sound/EarthQuake.wav");
        earthQuake.setVolume(1.5f);
        earthQuake.execute(root, location, null);

        ParticleEmitter emitter = createParticleEffect();
        root.attachChild(emitter);
        emitter.setLocalTranslation(location.add(0, 1f, 0));
        emitter.emitAllParticles();
        emitter.addControl(new CTimedExistence(1f));
        return null;
    }
}
