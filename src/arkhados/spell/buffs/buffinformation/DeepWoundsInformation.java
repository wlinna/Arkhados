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

import arkhados.controls.CharacterBuffControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.effects.BuffEffect;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class DeepWoundsInformation extends BuffInformation {
    {
        this.setIconPath("Interface/Images/SpellIcons/deep_wounds.png");
    }

    @Override
    public BuffEffect createBuffEffect(CharacterBuffControl buffControl, float duration) {
        final DeepWoundsEffect effect = new DeepWoundsEffect(duration);
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class DeepWoundsEffect extends BuffEffect {

    private ParticleEmitter emitter = null;
    private CharacterPhysicsControl physics = null;
    private float updateTimer = 0f;

    public DeepWoundsEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CharacterBuffControl buffControl) {
        emitter = new ParticleEmitter("blood-emitter", ParticleMesh.Type.Triangle, 100);

        final Material bloodMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        // TODO: Change blood texture!
        bloodMat.setTexture("Texture", assetManager.loadTexture("Effects/debris.png"));
        this.emitter.setMaterial(bloodMat);
        this.emitter.setImagesX(3);
        this.emitter.setImagesY(3);
        this.emitter.setSelectRandomImage(true);
        this.emitter.setStartColor(new ColorRGBA(0.3f, 0f, 0f, 1f));
        this.emitter.setStartColor(new ColorRGBA(0.3f, 0f, 0f, 1f));
        this.emitter.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Z.mult(-0.1f));
        this.emitter.getParticleInfluencer().setVelocityVariation(0.9f);
        this.emitter.setStartSize(2f);
        this.emitter.setEndSize(1f);
        this.emitter.setGravity(0f, 70f, 0f);
        this.emitter.setLowLife(2f);
        this.emitter.setHighLife(3f);
        this.emitter.setParticlesPerSec(10f);
        this.emitter.setRandomAngle(true);

        final Node characterNode = (Node) buffControl.getSpatial();
        characterNode.attachChild(this.emitter);
        this.emitter.move(0f, 7f, 0f);

        this.physics = characterNode.getControl(CharacterPhysicsControl.class);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        this.updateTimer += tpf;

        if (this.updateTimer < 0.5f) {
            return;
        }

        this.updateTimer = 0f;

        final float speed = this.physics.getWalkDirection().length();
        if (speed < 10f) {
            this.emitter.setParticlesPerSec(3f);
        } else {
            this.emitter.setParticlesPerSec(speed * 0.8f);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        assert emitter != null;
        emitter.removeFromParent();
    }
}