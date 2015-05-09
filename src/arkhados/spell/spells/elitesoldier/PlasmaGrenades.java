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
import arkhados.Globals;
import arkhados.actions.ChannelingSpellAction;
import arkhados.actions.EntityAction;
import arkhados.actions.SplashAction;
import arkhados.actions.castspellactions.CastGrenadeAction;
import arkhados.characters.EliteSoldier;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CGenericSync;
import arkhados.controls.CGrenade;
import arkhados.controls.CSpellBuff;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.DistanceScaling;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

public class PlasmaGrenades extends Spell {

    public PlasmaGrenades(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final PlasmaGrenades spell = new PlasmaGrenades("Plasma Grenades",
                Plasmagun.COOLDOWN, Plasmagun.RANGE, Plasmagun.CAST_TIME);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                CastGrenadeAction action = new CastGrenadeAction(spell);
                action.setTypeId(EliteSoldier.ACTION_PLASMAGUN);
                ChannelingSpellAction channel = new ChannelingSpellAction(spell,
                        3, 0.12f, action, true);
                return channel;
            }
        };

        spell.nodeBuilder = new PlasmaGrenadeBuilder();

        return spell;
    }
}

class PlasmaGrenadeBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createPlasmaEmitter() {
        ParticleEmitter plasma = new ParticleEmitter("plasma-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material materialRed = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                assetManager.loadTexture("Effects/plasma-particle.png"));
        plasma.setMaterial(materialRed);
        plasma.setImagesX(2);
        plasma.setImagesY(2);
        plasma.setSelectRandomImage(true);
        plasma.setStartColor(new ColorRGBA(0.8f, 0.350f, 0.9f, 1.0f));
        plasma.setEndColor(new ColorRGBA(0.80f, 0.30f, 0.9f, 0.95f));
        plasma.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        plasma.setStartSize(5.5f);
        plasma.setEndSize(4.5f);
        plasma.setGravity(Vector3f.ZERO);
        plasma.setLowLife(0.05f);
        plasma.setHighLife(0.05f);
        plasma.setParticlesPerSec(100);

        plasma.setRandomAngle(true);
        return plasma;
    }

    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 1.0f);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        projectileGeom.setCullHint(Spatial.CullHint.Always);

        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 140f);
        node.setUserData(UserDataStrings.MASS, 0.20f);
        node.setUserData(UserDataStrings.DAMAGE, 60f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);

        if (worldManager.isClient()) {
            ParticleEmitter plasma = createPlasmaEmitter();
            node.attachChild(plasma);

            node.addControl(new CEntityEvent());
            /**
             * Here we specify what happens on client side when plasmaball is
             * removed. In this case we want explosion effect.
             */
            PlasmaRemovalAction removalAction =
                    new PlasmaRemovalAction(assetManager);
            removalAction.setPlasmaEmitter(plasma);

            node.getControl(CEntityEvent.class)
                    .setOnRemoval(removalAction);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(5);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserDataStrings.MASS));

        physicsBody.setFriction(1f);
        physicsBody.setAngularDamping(1f);

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
        physicsBody.addCollideWithGroup(CollisionGroups.WALLS);
        physicsBody.addCollideWithGroup(CollisionGroups.SPIRIT_STONE);

        node.addControl(physicsBody);

        PhysicsSpace space = Globals.app.getStateManager()
                .getState(BulletAppState.class).getPhysicsSpace();

        Vector3f gravity = new Vector3f();

        space.getGravity(gravity);
        physicsBody.setGravity(gravity);

        if (worldManager.isServer()) {
            CGrenade cGrenade = new CGrenade();
            node.addControl(cGrenade);
            space.addTickListener(cGrenade);
            space.addCollisionListener(cGrenade);

            cGrenade.setDetonationTime(3f);
            SplashAction splash = new SplashAction(20f, 23f,
                    DistanceScaling.CONSTANT, null);
            splash.setSpatial(node);
            cGrenade.setSplashAction(splash);

            CSpellBuff buffControl = new CSpellBuff();
            node.addControl(buffControl);

            buffControl.addBuff(new SlowCC(-1, 1f, 0.3f));

            GhostControl characterCollision = new GhostControl(collisionShape);
            characterCollision.setCollideWithGroups(CollisionGroups.CHARACTERS);
            characterCollision.setCollisionGroup(CollisionGroups.PROJECTILES);
            node.addControl(characterCollision);
        }

        return node;
    }
}
