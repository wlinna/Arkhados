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
package arkhados.spell.spells.embermage;

import arkhados.CollisionGroups;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.ACastProjectile;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.BrimstoneBuff;
import arkhados.spell.buffs.IncapacitateCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserDataStrings;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.Iterator;

/**
 * Embermage's Magma Bash (M2) spell. Fast flying projectile with no damage or
 * knockback but incapacitates enemy for specified time.
 */
public class MagmaBash extends Spell {

    {
        iconName = "magma_bash.png";
    }

    public MagmaBash(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static MagmaBash create() {
        final float cooldown = 5f;
        final float range = 120f;
        final float castTime = 0.3f;

        final MagmaBash spell = new MagmaBash("Magma Bash", cooldown, range,
                castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                ACastProjectile action =
                        new ACastProjectile(spell, worldManager);
                return action;
            }
        };

        spell.nodeBuilder = new MagmaBashBuilder();

        return spell;
    }
}

class MagmaBashBuilder extends AbstractNodeBuilder {

    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 1.0f);
        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Black);
        node.setMaterial(material);
        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 180f);
        node.setUserData(UserDataStrings.MASS, 10f);
        node.setUserData(UserDataStrings.DAMAGE, 80f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);
        node.setUserData(UserDataStrings.INCAPACITATE_LENGTH, 7.4f);

        if (AbstractNodeBuilder.worldManager.isClient()) {
            ParticleEmitter fire = new ParticleEmitter("fire-emitter",
                    ParticleMesh.Type.Triangle, 80);
            Material materialRed = new Material(assetManager,
                    "Common/MatDefs/Misc/Particle.j3md");
            materialRed.setTexture("Texture",
                    assetManager.loadTexture("Effects/flame.png"));
            fire.setMaterial(materialRed);
            fire.setImagesX(2);
            fire.setImagesY(2);
            fire.setSelectRandomImage(true);
            fire.setStartColor(new ColorRGBA(0.95f, 0.850f, 0.0f, 1.0f));
            fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.80f, 0.5f));
            fire.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
            fire.setStartSize(1.5f);
            fire.setEndSize(0.5f);
            fire.setGravity(Vector3f.ZERO);
            fire.setLowLife(0.2f);
            fire.setHighLife(0.3f);
            fire.setParticlesPerSec(60);
            fire.getParticleInfluencer().setVelocityVariation(0.2f);
            fire.setRandomAngle(true);
            node.attachChild(fire);

            AudioNode sound = new AudioNode(assetManager,
                    "Effects/Sound/MagmaBash.wav");
            node.attachChild(sound);
            sound.setPositional(true);
            sound.setReverbEnabled(false);
            sound.setVolume(1f);
            sound.play();
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(3);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserDataStrings.MASS));
        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);
        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS
                | CollisionGroups.WALLS);
        node.addControl(physicsBody);

        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();
        node.addControl(buffControl);
        buffControl.addBuff(new BrimstoneIncapacitate(1.20f, -1));

        node.getControl(RigidBodyControl.class).setGravity(Vector3f.ZERO);
        return node;
    }
}

class BrimstoneIncapacitate extends IncapacitateCC {

    public BrimstoneIncapacitate(float duration, int id) {
        super(duration, id);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface influenceInterface) {
        BrimstoneBuff brimstone = null;

        for (Iterator<AbstractBuff> it =
                influenceInterface.getBuffs().iterator(); it.hasNext();) {
            AbstractBuff buff = it.next();
            if (buff instanceof BrimstoneBuff) {
                brimstone = (BrimstoneBuff) buff;
                it.remove();
            }
        }
        if (brimstone != null) {
            duration += brimstone.getStacks() * 0.3f;
        }

        super.attachToCharacter(influenceInterface);
    }
}