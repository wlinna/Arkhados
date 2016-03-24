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
package arkhados.spell.spells.shadowmancer;

import arkhados.CollisionGroups;
import arkhados.Globals;
import arkhados.World;
import arkhados.actions.cast.ACastProjectile;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CSpellCast;
import arkhados.controls.CTimedExistence;
import arkhados.effects.particle.ParticleEmitter;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.RemovalReasons;
import arkhados.util.UserData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 * Shadowmancer's ShadowOrb (M1) spell. Projectile has moderate speed and deals
 * moderate damage.
 */
public class ShadowOrb extends Spell {

    {
        iconName = "ShadowOrb.png";
    }

    public ShadowOrb(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.8f;
        final float range = 75f;
        final float castTime = 0.37f;

        final ShadowOrb spell
                = new ShadowOrb("Shadow Orb", cooldown, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f location) -> {
            ACastProjectile castProjectile
                    = new ACastProjectile(spell, Spell.world);
            return castProjectile;
        };

        spell.nodeBuilder = new OrbBuilder();

        return spell;
    }
}

class OrbBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createPurpleEmitter() {
        ParticleEmitter purple = new ParticleEmitter("purple-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material materialPurple = new Material(assets,
                "Common/MatDefs/Misc/Particle.j3md");
        materialPurple.setTexture("Texture",
                assets.loadTexture("Effects/flame.png"));
        purple.setMaterial(materialPurple);
        purple.setImagesX(2);
        purple.setImagesY(2);
        purple.setSelectRandomImage(true);
        purple.setStartColor(new ColorRGBA(0.8f, 0.015f, 0.8f, 1f));
        purple.setEndColor(new ColorRGBA(0.8f, 0f, 0.8f, 0.5f));
        purple.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        purple.setStartSize(2.5f);
        purple.setEndSize(1f);
        purple.setGravity(Vector3f.ZERO);
        purple.setLowLife(0.1f);
        purple.setHighLife(0.1f);
        purple.setParticlesPerSec(100);

        purple.setRandomAngle(true);
        return purple;
    }

    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 1.0f);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);

        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        Material material
                = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Black);
        node.setMaterial(material);

        node.setUserData(UserData.SPEED_MOVEMENT, 150f);
        node.setUserData(UserData.MASS, 0.30f);
        node.setUserData(UserData.DAMAGE, 140f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        if (world.isClient()) {
            ParticleEmitter purple = createPurpleEmitter();
            node.attachChild(purple);

            node.addControl(new CEntityEvent());

            AOrbRemoval removalAction = new AOrbRemoval();
            removalAction.setPurpleEmitter(purple);

            node.getControl(CEntityEvent.class).setOnRemoval(removalAction);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(3);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);

        /**
         * Add collision group of characters
         */
        GhostControl characterCollision = new GhostControl(collisionShape);
        characterCollision.setCollideWithGroups(CollisionGroups.CHARACTERS);
        characterCollision.setCollisionGroup(CollisionGroups.PROJECTILES);
        node.addControl(characterCollision);

        node.addControl(physicsBody);

        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();
        node.addControl(buffControl);
        buffControl.addBuff(new ReduceCooldownBuff.MyBuilder(0));

        return node;
    }
}

class ReduceCooldownBuff extends AbstractBuff {

    private ReduceCooldownBuff(float duration) {
        super(duration);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        super.attachToCharacter(targetInterface);
        CSpellCast c = getOwnerInterface().getSpatial()
                .getControl(CSpellCast.class);
        Spell spell = Spell.getSpell("Dark Energy");
        
        if (spell == null) {
            return;
        }
        
        int id = spell.getId();
        float cooldown = c.getCooldown(id);
        cooldown = FastMath.clamp(cooldown - 1f, 0, cooldown);
        c.setCooldown(id, cooldown);
    }

    public static class MyBuilder extends AbstractBuffBuilder {

        public MyBuilder(float duration) {
            super(0);
        }

        @Override
        public AbstractBuff build() {
            return set(new ReduceCooldownBuff(0));
        }

    }
}

class AOrbRemoval implements ARemovalEvent {

    private ParticleEmitter purple;
    private final AudioNode sound;

    public AOrbRemoval() {
        sound = new AudioNode(Globals.assets,
                "Effects/Sound/FireballExplosion.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
    }

    public void setPurpleEmitter(ParticleEmitter purple) {
        this.purple = purple;
    }

    @Override
    public void exec(World world, int reason) {
        if (reason == RemovalReasons.DISAPPEARED) {
            return;
        }

        Vector3f worldTranslation = purple.getParent().getLocalTranslation();

        purple.removeFromParent();
        world.getWorldRoot().attachChild(purple);
        purple.setLocalTranslation(worldTranslation);
        purple.addControl(new CTimedExistence(1f));

        purple.setStartColor(new ColorRGBA(0.3f, 0.015f, 0.3f, 0.8f));
        purple.setEndColor(new ColorRGBA(0f, 0f, 0f, 0f));
        purple.setLowLife(0.1f);
        purple.setHighLife(0.3f);
        purple.setNumParticles(100);
        purple.setStartSize(0.5f);
        purple.setEndSize(3f);
        purple.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(15f));
        purple.getParticleInfluencer().setVelocityVariation(1f);

        purple.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        purple.emitAllParticles();
        purple.setParticlesPerSec(0f);

        sound.setLocalTranslation(worldTranslation);
        sound.play();
    }
}
