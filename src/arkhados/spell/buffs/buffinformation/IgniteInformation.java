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

import arkhados.controls.CCharacterBuff;
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
        setIconPath("Interface/Images/BuffIcons/ignite.png");
    }

    @Override
    public BuffEffect createBuffEffect(CCharacterBuff buffControl, float duration) {
        IgniteEffect effect = new IgniteEffect(duration);
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class IgniteEffect extends BuffEffect {

    private ParticleEmitter fire = null;

    public IgniteEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CCharacterBuff buffControl) {
        fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 50);
        Material materialRed = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));
        fire.setMaterial(materialRed);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 1.0f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
        fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Y.mult(9f));
        fire.setStartSize(6.5f);
        fire.setEndSize(0.5f);
        fire.setGravity(Vector3f.ZERO);
        fire.setLowLife(0.99f);
        fire.setHighLife(0.99f);
        fire.setParticlesPerSec(10);
        fire.getParticleInfluencer().setVelocityVariation(0.2f);
        fire.setRandomAngle(true);

        Node characterNode = (Node) buffControl.getSpatial();
        characterNode.attachChild(fire);
        fire.move(0f, 15f, 0);
    }

    @Override
    public void destroy() {
        super.destroy();
        assert fire != null;
        fire.removeFromParent();
    }
}