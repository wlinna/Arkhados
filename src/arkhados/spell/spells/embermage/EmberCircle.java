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

import arkhados.actions.DelayAction;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastOnGroundAction;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.AreaEffectControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.effects.EmitterCircleShape;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.influences.DamagOverTimeInfluence;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;

/**
 * Embermage's Ember Circle (Q) spell. Area of Effect spell that deals damage
 * over time in certain area. Has small activation delay.
 */
public class EmberCircle extends Spell {

    public EmberCircle(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static EmberCircle create() {
        final float cooldown = 6f;
        final float range = 100f;
        final float castTime = 0.4f;

        final EmberCircle spell = new EmberCircle("Ember Circle", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f vec) {
                final CastOnGroundAction castOnGround = new CastOnGroundAction(worldManager, spell);
                castOnGround.addEnterBuff(Ignite.ifNotCooldownCreateDamageOverTimeBuff(caster));
                return castOnGround;
            }
        };

        spell.nodeBuilder = new EmberCircleBuilder();

        return spell;
    }
}

class EmberCircleBuilder extends NodeBuilder {

    @Override
    public Node build() {
        final Node node = (Node) assetManager.loadModel("Models/Circle.j3o");
        final float radius = 15f;
        node.scale(radius, 1f, radius);

        Material material = assetManager.loadMaterial("Materials/EmberCircleGround.j3m");
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        node.setQueueBucket(RenderQueue.Bucket.Transparent);
        material.setTexture("AlphaMap", assetManager.loadTexture("Textures/EmberCircleAlphaMap.png"));
//        material.setBoolean("UseAlpha", true);
        node.setMaterial(material);


        node.setUserData(UserDataStrings.DAMAGE_PER_SECOND, 100f);
        ActionQueueControl actionQueue = new ActionQueueControl();
        node.addControl(actionQueue);

        actionQueue.enqueueAction(new DelayAction(0.8f));

        if (worldManager.isServer()) {
            GhostControl ghost = new GhostControl(new CylinderCollisionShape(new Vector3f(radius, 0.05f, radius), 1));
            ghost.removeCollideWithGroup(GhostControl.COLLISION_GROUP_01);
            node.addControl(ghost);

            final AreaEffectControl areaEffectControl = new AreaEffectControl(ghost);
            node.addControl(areaEffectControl);

            actionQueue.enqueueAction(new EntityAction() {
                @Override
                public boolean update(float tpf) {
                    Float dps = (Float) super.spatial.getUserData(UserDataStrings.DAMAGE_PER_SECOND);
                    areaEffectControl.addInfluence(new DamagOverTimeInfluence(dps));

                    node.addControl(new TimedExistenceControl(5f, true));

                    return false;
                }
            });
        }

        if (worldManager.isClient()) {
            actionQueue.enqueueAction(new EntityAction() {
                @Override
                public boolean update(float tpf) {
                    final ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 50 * (int) radius);
                    Material materialRed = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Particle.j3md");
                    materialRed.setTexture("Texture", NodeBuilder.assetManager.loadTexture("Effects/flame.png"));
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
}