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

import arkhados.Globals;
import arkhados.controls.CCharacterBuff;
import arkhados.controls.CCharacterPhysics;
import arkhados.effects.BuffEffect;
import com.jme3.audio.AudioNode;
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
        setIconPath("Interface/Images/SpellIcons/deep_wounds.png");
    }

    @Override
    public BuffEffect createBuffEffect(CCharacterBuff buffControl, float duration) {
        DeepWoundsEffect effect = new DeepWoundsEffect(duration);
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class DeepWoundsEffect extends BuffEffect {

    private ParticleEmitter emitter = null;
    private CCharacterPhysics physics = null;
    private float updateTimer = 0f;

    public DeepWoundsEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CCharacterBuff buffControl) {
        emitter = new ParticleEmitter("blood-emitter", ParticleMesh.Type.Triangle, 100);

        Material bloodMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        // TODO: Change blood texture!
        bloodMat.setTexture("Texture", assetManager.loadTexture("Effects/debris.png"));
        emitter.setMaterial(bloodMat);
        emitter.setImagesX(3);
        emitter.setImagesY(3);
        emitter.setSelectRandomImage(true);
        emitter.setStartColor(new ColorRGBA(0.3f, 0f, 0f, 1f));
        emitter.setStartColor(new ColorRGBA(0.3f, 0f, 0f, 1f));
        emitter.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Z.mult(-0.1f));
        emitter.getParticleInfluencer().setVelocityVariation(0.9f);
        emitter.setStartSize(2f);
        emitter.setEndSize(1f);
        emitter.setGravity(0f, 70f, 0f);
        emitter.setLowLife(2f);
        emitter.setHighLife(3f);
        emitter.setParticlesPerSec(10f);
        emitter.setRandomAngle(true);

        Node characterNode = (Node) buffControl.getSpatial();
        characterNode.attachChild(emitter);
        emitter.move(0f, 7f, 0f);

        physics = characterNode.getControl(CCharacterPhysics.class);

        AudioNode sound = new AudioNode(Globals.assetManager,
                "Effects/Sound/DeepWounds.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
        characterNode.attachChild(sound);
        sound.play();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        updateTimer += tpf;

        if (updateTimer < 0.5f) {
            return;
        }

        updateTimer = 0f;

        float speed = physics.getWalkDirection().length();
        if (speed < 10f) {
            emitter.setParticlesPerSec(3f);
        } else {
            emitter.setParticlesPerSec(speed * 0.8f);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        assert emitter != null;
        emitter.removeFromParent();
    }
}