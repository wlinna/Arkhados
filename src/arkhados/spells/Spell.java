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
package arkhados.spells;

import arkhados.spells.influences.DamagOverTimeeInfluence;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import arkhados.WorldManager;
import arkhados.actions.DelayAction;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.AreaEffectControl;
import arkhados.controls.EntityEventControl;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.effects.EmitterCircleShape;
import arkhados.entityevents.RemovalEventAction;
import arkhados.spells.influences.IncapacitateInfluence;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.math.Quaternion;

/**
 *
 * @author william
 */
public class Spell {

    private static AssetManager assetManager = null;
    private static WorldManager worldManager = null;
    private static HashMap<String, Spell> Spells = new HashMap<String, Spell>();

    public static void initSpells(AssetManager assetManager, WorldManager worldManager) {
        Spell.assetManager = assetManager;
        Spell.worldManager = worldManager;

        ProjectileControl.setWorldManager(worldManager);

        Spell fireball = initFireBall();
        Spells.put(fireball.getName(), fireball);

        Spell magmaBash = initMagmaBash();
        Spells.put(magmaBash.getName(), magmaBash);

        Spell emberCircle = initEmberCircle();
        Spells.put(emberCircle.getName(), emberCircle);



    }

    public static HashMap<String, Spell> getSpells() {
        return Spells;
    }
    private final String name;
    private final float cooldown;
    private final float range;
    private final float castTime;
    private CastSpellActionBuilder castSpellActionBuilder;
    private NodeBuilder nodeBuilder;

    private Spell(String name, float cooldown, float range, float castTime) {
        this.name = name;
        this.cooldown = cooldown;
        this.range = range;
        this.castTime = castTime;
    }

    public String getName() {
        return this.name;
    }

    public float getCooldown() {
        return this.cooldown;
    }

    public float getRange() {
        return this.range;
    }

    public float getCastTime() {
        return this.castTime;
    }

    public EntityAction buildCastAction(Vector3f vec) {
        return this.castSpellActionBuilder.newAction(vec);
    }

    public Node buildNode() {
        return this.nodeBuilder.build();
    }

    private static Spell initFireBall() {
        final float cooldown = 1.0f;
        final float range = 40f;
        final float castTime = 0.2f;

        final Spell spell = new Spell("Fireball", cooldown, range, castTime);
        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Vector3f location) {
                return new CastProjectileAction(spell, location, worldManager);
            }
        };

        spell.nodeBuilder = new NodeBuilder() {
            public Node build() {
                Sphere sphere = new Sphere(32, 32, 1.0f);

                Geometry projectileGeom = new Geometry("projectile-geom", sphere);
                Node node = new Node("projectile");
                node.attachChild(projectileGeom);

                // TODO: Give at least bit better material
                Material material = new Material(Spell.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                material.setColor("Color", ColorRGBA.Yellow);
                node.setMaterial(material);
                node.setUserData(UserDataStrings.SPEED_MOVEMENT, 70f);
                node.setUserData(UserDataStrings.MASS, 30f);
                node.setUserData(UserDataStrings.DAMAGE, 150f);
                node.setUserData(UserDataStrings.IMPULSE_FACTOR, 10000f);

                if (Spell.worldManager.isClient()) {
                    final ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 100);
                    Material materialRed = new Material(Spell.assetManager, "Common/MatDefs/Misc/Particle.j3md");
                    materialRed.setTexture("Texture", Spell.assetManager.loadTexture("Effects/flame.png"));
                    fire.setMaterial(materialRed);
                    fire.setImagesX(2);
                    fire.setImagesY(2);
                    fire.setSelectRandomImage(true);
                    fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 1.0f));
                    fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
                    fire.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
                    fire.setStartSize(6.5f);
                    fire.setEndSize(0.5f);
                    fire.setGravity(Vector3f.ZERO);
                    fire.setLowLife(0.2f);
                    fire.setHighLife(0.3f);
                    fire.setParticlesPerSec(40);
                    fire.getParticleInfluencer().setVelocityVariation(0.5f);
                    fire.setRandomAngle(true);
                    node.attachChild(fire);

                    node.addControl(new EntityEventControl());
                    node.getControl(EntityEventControl.class).setOnRemoval(new RemovalEventAction() {
                        private ParticleEmitter fire;

                        public RemovalEventAction setEmitter(ParticleEmitter fire) {
                            this.fire = fire;
                            return this;
                        }

                        public void exec(WorldManager worldManager, String reason) {
                            if (!"collision".equals(reason)) {
                                return;
                            }
                            Vector3f worldTranslation = fire.getParent().getLocalTranslation();
                            fire.removeFromParent();
                            worldManager.getWorldRoot().attachChild(fire);
                            fire.setLocalTranslation(worldTranslation);
                            fire.addControl(new TimedExistenceControl(0.3f));
                            fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_X.mult(15.0f));
                            fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 6.0f));
                            fire.emitAllParticles();
                            fire.setParticlesPerSec(0.0f);

                            // TODO: Add soundeffect too!
                        }
                    }.setEmitter(fire));
                }

                SphereCollisionShape collisionShape = new SphereCollisionShape(5.0f);
                RigidBodyControl physicsBody = new RigidBodyControl(collisionShape, (Float) node.getUserData(UserDataStrings.MASS));
                physicsBody.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_16);
                physicsBody.removeCollideWithGroup(RigidBodyControl.COLLISION_GROUP_16);
                node.addControl(physicsBody);

                node.addControl(new ProjectileControl());
                SpellBuffControl buffControl = new SpellBuffControl();
                node.addControl(buffControl);

                node.getControl(RigidBodyControl.class).setGravity(Vector3f.ZERO);

                return node;
            }
        };

        return spell;
    }

    private static Spell initMagmaBash() {
        final float cooldown = 5f;
        final float range = 40f;
        final float castTime = 0.3f;

        final Spell spell = new Spell("Magma Bash", cooldown, range, castTime);
        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Vector3f vec) {
                return new CastProjectileAction(spell, vec, worldManager);
            }
        };
        spell.nodeBuilder = new NodeBuilder() {
            public Node build() {
                Sphere sphere = new Sphere(32, 32, 1.0f);
                Geometry projectileGeom = new Geometry("projectile-geom", sphere);
                Node node = new Node("projectile");
                node.attachChild(projectileGeom);

                // TODO: Give at least bit better material
                Material material = new Material(Spell.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                material.setColor("Color", ColorRGBA.Black);
                node.setMaterial(material);
                node.setUserData(UserDataStrings.SPEED_MOVEMENT, 140f);
                node.setUserData(UserDataStrings.MASS, 10f);
                node.setUserData(UserDataStrings.DAMAGE, 0f);
                node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);
                node.setUserData(UserDataStrings.INCAPACITATE_LENGTH, 0.4f);

                if (Spell.worldManager.isClient()) {
                    final ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 80);
                    Material materialRed = new Material(Spell.assetManager, "Common/MatDefs/Misc/Particle.j3md");
                    materialRed.setTexture("Texture", Spell.assetManager.loadTexture("Effects/flame.png"));
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
                }

                SphereCollisionShape collisionShape = new SphereCollisionShape(5.0f);
                RigidBodyControl physicsBody = new RigidBodyControl(collisionShape, (Float) node.getUserData(UserDataStrings.MASS));
                physicsBody.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_16);
                physicsBody.removeCollideWithGroup(RigidBodyControl.COLLISION_GROUP_16);
                node.addControl(physicsBody);

                node.addControl(new ProjectileControl());
                SpellBuffControl buffControl = new SpellBuffControl();
                node.addControl(buffControl);
                buffControl.addCrowControlInfluence(new IncapacitateInfluence(1f, -1));

                node.getControl(RigidBodyControl.class).setGravity(Vector3f.ZERO);
                return node;
            }
        };


        return spell;
    }

    private static Spell initEmberCircle() {
        final float cooldown = 6f;
        final float range = 40f;
        final float castTime = 0.4f;

        final Spell spell = new Spell("Ember Circle", cooldown, range, castTime);
        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(final Vector3f location) {
                return new EntityAction() {
                    @Override
                    public boolean update(float tpf) {
                        worldManager.addNewEntity(spell.getName(), location.setY(0.1f), Quaternion.IDENTITY);
                        return false;
                    }
                };

            }
        };
        spell.nodeBuilder = new NodeBuilder() {
            public Node build() {
                final Node node = (Node) assetManager.loadModel("Models/Circle.j3o");
                final float radius = 15f;
                node.scale(radius, 1f, radius);
                // Let's use simple black color first
                Material black = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                black.setColor("Color", ColorRGBA.Black);
                node.setMaterial(black);

                node.setUserData(UserDataStrings.DAMAGE_PER_SECOND, 100f);
                ActionQueueControl actionQueue = new ActionQueueControl();
                node.addControl(actionQueue);

                actionQueue.enqueueAction(new DelayAction(0.8f));

                if (worldManager.isServer()) {
                    GhostControl ghost = new GhostControl(new CylinderCollisionShape(new Vector3f(radius, 0.05f, radius), 1));
                    node.addControl(ghost);

                    final AreaEffectControl areaEffectControl = new AreaEffectControl(ghost);
                    node.addControl(areaEffectControl);

                    actionQueue.enqueueAction(new EntityAction() {
                        @Override
                        public boolean update(float tpf) {
                            Float dps = (Float) super.spatial.getUserData(UserDataStrings.DAMAGE_PER_SECOND);
                            areaEffectControl.addInfluence(new DamagOverTimeeInfluence(dps));

                            node.addControl(new TimedExistenceControl(10f, true));

                            return false;
                        }
                    });
                }

                if (worldManager.isClient()) {
                    actionQueue.enqueueAction(new EntityAction() {
                        @Override
                        public boolean update(float tpf) {
                            final ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 50 * (int) radius);
                            Material materialRed = new Material(Spell.assetManager, "Common/MatDefs/Misc/Particle.j3md");
                            materialRed.setTexture("Texture", Spell.assetManager.loadTexture("Effects/flame.png"));
                            fire.setMaterial(materialRed);
                            fire.setImagesX(2);
                            fire.setImagesY(2);
                            fire.setSelectRandomImage(true);
                            fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 1.0f));
                            fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
                            fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Y.mult(2f));
                            fire.setStartSize(6.5f);
                            fire.setEndSize(0.5f);
                            fire.setGravity(Vector3f.ZERO);
                            fire.setLowLife(1f);
                            fire.setHighLife(2f);
                            fire.setParticlesPerSec((int) (0.5 * radius * radius));
                            fire.getParticleInfluencer().setVelocityVariation(0.2f);
                            fire.setRandomAngle(true);
                            ((Node) super.spatial).attachChild(fire);
                            fire.setLocalTranslation(Vector3f.ZERO);
                            EmitterCircleShape emitterShape = new EmitterCircleShape(Vector3f.ZERO, 1f);
                            fire.setShape(emitterShape);

                            return false;
                        }
                    });
                }
                return node;
            }
        };

        return spell;
    }
}