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
package arkhados.spell;

import arkhados.CollisionGroups;
import arkhados.World;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CTimedExistence;
import arkhados.entityevents.ARemovalEvent;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

public class PelletBuilder extends AbstractNodeBuilder {

    private float damage;

    public PelletBuilder(float damage) {
        this.damage = damage;
    }
    
    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(8, 8, 0.3f);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);
        Material material = new Material(assets,
                "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);
        node.setUserData(UserData.SPEED_MOVEMENT, 220f);
        node.setUserData(UserData.MASS, 0.30f);
        node.setUserData(UserData.DAMAGE, damage);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);
        if (world.isClient()) {
            node.addControl(new CEntityEvent());
            /**
             * Here we specify what happens on client side when pellet is
             * removed. In this case we want explosion effect.
             */
            APelletRemoval removalAction =
                    new APelletRemoval(assets);
            removalAction.setPellet(node);
            node.getControl(CEntityEvent.class)
                    .setOnRemoval(removalAction);
        }
        SphereCollisionShape collisionShape = new SphereCollisionShape(1.7f);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));       
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(CollisionGroups.NONE);
        physicsBody.setCollideWithGroups(CollisionGroups.NONE);
        /**
         * Add collision with characters
         */
        GhostControl collision = new GhostControl(collisionShape);
        collision.setCollisionGroup(CollisionGroups.PROJECTILES);
        collision.setCollideWithGroups(CollisionGroups.CHARACTERS);
        node.addControl(collision);
        node.addControl(physicsBody);
        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();
        node.addControl(buffControl);
        return node;
    }
}

class APelletRemoval implements ARemovalEvent {

    private AssetManager assetManager;
    private Spatial pellet;

    public APelletRemoval(AssetManager assetManager) {
        super();
        this.assetManager = assetManager;
    }

    private void createSmallExplosion(Node root, Vector3f location) {
        ParticleEmitter fire = new ParticleEmitter("shotgun-explosion",
                ParticleMesh.Type.Triangle, 20);

        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assetManager.loadTexture("Effects/flame.png"));
        fire.setMaterial(material);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setGravity(Vector3f.ZERO);

        fire.setRandomAngle(true);

        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 0.40f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.0f));
        fire.setLowLife(0.1f);
        fire.setHighLife(0.3f);
        fire.setNumParticles(100);
        fire.setStartSize(0.50f);
        fire.setEndSize(1.0f);
        fire.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(1.0f));
        fire.getParticleInfluencer().setVelocityVariation(6f);

        fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.2f));
        fire.setParticlesPerSec(0f);

        root.attachChild(fire);
        fire.setLocalTranslation(location);
        fire.emitAllParticles();
        fire.addControl(new CTimedExistence(1f));
    }

    private void createSmokePuff(Node root, Vector3f location) {
        ParticleEmitter smokePuff = new ParticleEmitter("smoke-puff",
                ParticleMesh.Type.Triangle, 20);
        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assetManager.loadTexture("Effects/flame_alpha.png"));
        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);
        smokePuff.setMaterial(material);
        smokePuff.setImagesX(2);
        smokePuff.setImagesY(2);
        smokePuff.setSelectRandomImage(true);
        smokePuff.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.2f));
        smokePuff.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f));
        smokePuff.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(5f));
        smokePuff.getParticleInfluencer().setVelocityVariation(1f);
        smokePuff.setStartSize(2f);
        smokePuff.setEndSize(6f);
        smokePuff.setGravity(Vector3f.ZERO);
        smokePuff.setLowLife(0.75f);
        smokePuff.setHighLife(1f);
        smokePuff.setParticlesPerSec(0f);
        smokePuff.setRandomAngle(true);
        smokePuff.setShape(new EmitterSphereShape(Vector3f.ZERO, 4f));

        root.attachChild(smokePuff);
        smokePuff.setLocalTranslation(location);
        smokePuff.emitAllParticles();
        smokePuff.addControl(new CTimedExistence(1f));
    }

    @Override
    public void exec(World world, int reason) {
        Vector3f worldTranslation = pellet.getWorldTranslation();
        createSmokePuff(world.getWorldRoot(), worldTranslation);
        createSmallExplosion(world.getWorldRoot(), worldTranslation);
    }

    public void setPellet(Spatial pellet) {
        this.pellet = pellet;
    }
}