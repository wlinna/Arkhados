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
package arkhados.spell.spells.elitesoldier;

import arkhados.CollisionGroups;
import arkhados.World;
import arkhados.actions.cast.ACastProjectile;
import arkhados.characters.EliteSoldier;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CTimedExistence;
import arkhados.effects.ParticleInfluencerWithAngleSetting;
import arkhados.effects.particle.ParticleEmitter;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.Spell;
import arkhados.spell.buffs.BlindCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

public class BlindingRay extends Spell {

    {
        iconName = "railgun.png";
        setMoveTowardsTarget(false);
    }

    public BlindingRay(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 9f;
        final float range = 130f;
        final float castTime = 0.45f;

        final BlindingRay spell
                = new BlindingRay("Blinding Ray", cooldown, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastProjectile action
                    = new ACastProjectile(spell, world);
            action.setTypeId(EliteSoldier.ACTION_RAILGUN);
            return action;
        };

        spell.nodeBuilder = new RayBuilder();

        return spell;
    }
}

class RayBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createTrailEmitter() {
        ParticleEmitter trail = new ParticleEmitter("trail-emitter",
                ParticleMesh.Type.Triangle, 650);
        Material mat
                = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assets.loadTexture("Effects/smoketrail.png"));
        trail.setMaterial(mat);
        trail.setImagesX(1);
        trail.setImagesY(3);
        trail.setSelectRandomImage(true);
        trail.setStartColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 0.7f));
        trail.setParticleInfluencer(new ParticleInfluencerWithAngleSetting());
        trail.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        trail.getParticleInfluencer().setVelocityVariation(0f);
        trail.setStartSize(1.7f);
        trail.setEndSize(1.7f);
        trail.setGravity(Vector3f.ZERO);
        trail.setLowLife(0.3f);
        trail.setHighLife(0.3f);
        trail.setParticlesPerSec(2000);
        trail.setFaceNormal(Vector3f.UNIT_Y);

        trail.setRandomAngle(true);
        return trail;
    }

    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 0.5f);

        Geometry projectileGeom = new Geometry("rail-geom", sphere);

        Node node = new Node("rail");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material m = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", ColorRGBA.Cyan);
        node.setMaterial(m);

        node.setUserData(UserData.SPEED_MOVEMENT, 200f);
        node.setUserData(UserData.MASS, 0.30f);
        node.setUserData(UserData.DAMAGE, 150f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        if (world.isClient()) {
            ParticleEmitter smoke = createTrailEmitter();
            node.attachChild(smoke);

            node.addControl(new CEntityEvent());
            /**
             * Here we specify what happens on client side when fireball is
             * removed. In this case we want explosion effect.
             */
            ARailgunRemoval removalAction = new ARailgunRemoval();
            removalAction.setBullet(node);
            removalAction.setSmokeTrail(smoke);

            node.getControl(CEntityEvent.class).setOnRemoval(removalAction);
            node.addControl(new RailgunBuilder.CParticleDirector(smoke));
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(2.5f);
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
        CProjectile projectileControl = new CProjectile();
        projectileControl.setIsProjectile(false);
        node.addControl(projectileControl);
        CSpellBuff buffControl = new CSpellBuff();
        node.addControl(buffControl);
        buffControl.addBuff(new BlindCC.MyBuilder(3f));

        return node;
    }
}
