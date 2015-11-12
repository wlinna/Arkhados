// CC0 1.0 Universal (CC0 1.0)
// Public Domain Dedication

package arkhados.effects;

import com.jme3.effect.Particle;
import com.jme3.effect.influencers.DefaultParticleInfluencer;
import com.jme3.effect.shapes.EmitterShape;

public class ParticleInfluencerWithAngleSetting extends DefaultParticleInfluencer {
    private float angle;

    @Override
    public void influenceParticle(Particle particle, EmitterShape emitterShape) {
        super.influenceParticle(particle, emitterShape);
        particle.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}
