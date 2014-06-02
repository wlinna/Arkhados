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
public class IgniteInformation extends BuffInformation {
    {        
        super.setIconPath("Interface/Images/BuffIcons/ignite.png");
    }

    @Override
    public BuffEffect createBuffEffect(CharacterBuffControl buffControl, float duration) {
        final IgniteEffect effect = new IgniteEffect(duration);
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class IgniteEffect extends BuffEffect {

    private ParticleEmitter fire = null;

    public IgniteEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CharacterBuffControl buffControl) {
        this.fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 50);
        final Material materialRed = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));
        this.fire.setMaterial(materialRed);
        this.fire.setImagesX(2);
        this.fire.setImagesY(2);
        this.fire.setSelectRandomImage(true);
        this.fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 1.0f));
        this.fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
        this.fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Y.mult(9f));
        this.fire.setStartSize(6.5f);
        this.fire.setEndSize(0.5f);
        this.fire.setGravity(Vector3f.ZERO);
        this.fire.setLowLife(0.99f);
        this.fire.setHighLife(0.99f);
        this.fire.setParticlesPerSec(10);
        this.fire.getParticleInfluencer().setVelocityVariation(0.2f);
        this.fire.setRandomAngle(true);

        final Node characterNode = (Node) buffControl.getSpatial();
        characterNode.attachChild(this.fire);
        this.fire.move(0f, 15f, 0);
    }

    @Override
    public void destroy() {
        super.destroy();
        assert this.fire != null;
        this.fire.removeFromParent();
    }
}