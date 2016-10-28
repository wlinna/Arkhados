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
package arkhados.spell.spells.electrobot;

import arkhados.CollisionGroups;
import arkhados.Globals;
import arkhados.SpatialDistancePair;
import arkhados.actions.ADelay;
import arkhados.actions.EntityAction;
import arkhados.actions.cast.ACastOnGround;
import arkhados.controls.CActionQueue;
import arkhados.controls.CAreaEffect;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CCircleVisibility;
import arkhados.controls.CTimedExistence;
import arkhados.effects.EmitterCircleShape;
import arkhados.spell.Spell;
import arkhados.spell.buffs.DamageOverTimeBuff;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.Selector;
import arkhados.util.UserData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

public class ElectricPull2 extends Spell {

    {
        iconName = "MindPoison.png";
    }

    public ElectricPull2(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static ElectricPull2 create() {
        final float cooldown = 7.5f;
        final float range = 100f;
        final float castTime = 0.4f;

        final ElectricPull2 spell = new ElectricPull2("Electric Pull",
                cooldown, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec)
                -> new ACastOnGround(world, spell);

        spell.nodeBuilder = new ElectricPull2Builder();

        return spell;
    }
}

class ElectricPull2Builder extends AbstractNodeBuilder {

    private static final ColorRGBA color = new ColorRGBA(0f, 0f, 0f, 0.7f);

    private static final class AFinish extends EntityAction {

        private final CAreaEffect cAreaEffect;
        private final Node node;

        public AFinish(CAreaEffect cAreaEffect, Node node) {
            this.cAreaEffect = cAreaEffect;
            this.node = node;
        }

        @Override
        public boolean update(float tpf) {
            Vector3f centerLoc = node.getLocalTranslation();
            
            List<SpatialDistancePair> spatials = new ArrayList<>();
            Selector.getSpatialsWithinDistance(spatials, spatial, 15f,
                    new Selector.IsCharacterOfOtherTeam(
                            spatial.getUserData(UserData.TEAM_ID)));

            for (SpatialDistancePair target : spatials) {
                Vector3f dir = centerLoc.subtract(target.spatial
                        .getLocalTranslation()).setY(0).multLocal(400f);
                target.spatial.getControl(CCharacterPhysics.class)
                        .applyImpulse(dir);
            }

            cAreaEffect.addEnterBuff(new SlowCC.MyBuilder(1.5f, 0.5f));
            node.addControl(new CTimedExistence(0f, true));
            return false;
        }
    }

    private ParticleEmitter createCyan(float radius, float remainTime) {
        ParticleEmitter purple = new ParticleEmitter("purple-emitter",
                ParticleMesh.Type.Triangle, 10 * (int) radius);
        Material material
                = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assets.loadTexture("Effects/flame.png"));
        purple.setMaterial(material);
        purple.setImagesX(2);
        purple.setImagesY(2);
        purple.setSelectRandomImage(true);
        purple.setStartColor(new ColorRGBA(0.1f, 0.8f, 0.8f, 0.6f));
        purple.setEndColor(new ColorRGBA(0.1f, 0.8f, 0.8f, 0.05f));
        purple.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_Y.mult(2f));
        purple.setStartSize(6.5f);
        purple.setEndSize(2.5f);
        purple.setGravity(Vector3f.ZERO);
        purple.setLowLife(remainTime * 0.9f);
        purple.setHighLife(remainTime);
        purple.setParticlesPerSec(0);
        purple.getParticleInfluencer().setVelocityVariation(0.2f);
        purple.setRandomAngle(true);

        EmitterCircleShape emitterShape
                = new EmitterCircleShape(Vector3f.ZERO, radius);
        purple.setShape(emitterShape);

        return purple;
    }

    private ParticleEmitter createWhite(float radius, float remainTime) {
        ParticleEmitter purple = new ParticleEmitter("purple-emitter",
                ParticleMesh.Type.Triangle, 5 * (int) radius);
        Material material
                = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assets.loadTexture("Effects/flame.png"));
        purple.setMaterial(material);
        purple.setImagesX(2);
        purple.setImagesY(2);
        purple.setSelectRandomImage(true);
        purple.setStartColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.8f));
        purple.setEndColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.05f));
        purple.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_Y.mult(2f));
        purple.setStartSize(4.5f);
        purple.setEndSize(4.5f);
        purple.setGravity(Vector3f.ZERO);
        purple.setLowLife(remainTime * 0.2f);
        purple.setHighLife(remainTime * 0.3f);
        purple.setParticlesPerSec(0);
        purple.getParticleInfluencer().setVelocityVariation(0.2f);
        purple.setRandomAngle(true);

        EmitterCircleShape emitterShape
                = new EmitterCircleShape(Vector3f.ZERO, radius);
        purple.setShape(emitterShape);

        return purple;
    }

    private ParticleEmitter createBlack(float radius, float delay) {
        ParticleEmitter purple = new ParticleEmitter("black-emitter",
                ParticleMesh.Type.Triangle, 100 * (int) radius * (int) radius);
        Material mat
                = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture",
                Globals.assets.loadTexture("Effects/flame_alpha.png"));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        purple.setMaterial(mat);
        purple.setImagesX(2);
        purple.setImagesY(2);
        purple.setSelectRandomImage(true);
        purple.setStartColor(ElectricPull2Builder.color);
        purple.setEndColor(ElectricPull2Builder.color);
        purple.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        purple.setStartSize(2.5f);
        purple.setEndSize(4.5f);
        purple.setGravity(Vector3f.ZERO);
        purple.setLowLife(delay);
        purple.setHighLife(delay);
        purple.setParticlesPerSec(0);
        purple.getParticleInfluencer().setVelocityVariation(0f);
        purple.setRandomAngle(true);

        EmitterCircleShape emitterShape
                = new EmitterCircleShape(Vector3f.ZERO, radius);
        purple.setShape(emitterShape);

        return purple;
    }

    @Override
    public Node build(final BuildParameters params) {
        final Node node = (Node) assets.loadModel("Models/Circle.j3o");
        node.setLocalTranslation(params.location);

        for (Spatial child : node.getChildren()) {
            child.setCullHint(Spatial.CullHint.Always);
        }

        final float radius = 15f;
        node.scale(radius, 1f, radius);

        Material mat = new Material(assets,
                "Common/MatDefs/Misc/Unshaded.j3md");

        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.setColor("Color", ColorRGBA.BlackNoAlpha);
        node.setMaterial(mat);
        CActionQueue actionQueue = new CActionQueue();
        node.addControl(actionQueue);

        float delay = Math.max(0.8f - params.age, 0f);
        actionQueue.enqueueAction(new ADelay(delay));

        final float remainTime = 1f;

        if (world.isServer()) {
            node.addControl(new CCircleVisibility(radius));

            GhostControl ghost = new GhostControl(new CylinderCollisionShape(
                    new Vector3f(radius, 0.05f, radius), 1));
            ghost.setCollideWithGroups(CollisionGroups.CHARACTERS);
            node.addControl(ghost);

            CAreaEffect cAreaEffect = new CAreaEffect(ghost);
            node.addControl(cAreaEffect);

            actionQueue.enqueueAction(new AFinish(cAreaEffect, node));
        } else if (world.isClient()) {
            ParticleEmitter black = createBlack(radius / 15f, delay);
            node.attachChild(black);
            black.setLocalTranslation(0f, 1f, 0f);
            black.emitAllParticles();

            actionQueue.enqueueAction(new EntityAction() {
                @Override
                public boolean update(float tpf) {
                    Vector3f worldTranslation = spatial.getWorldTranslation();

                    ParticleEmitter cyan = createCyan(radius, remainTime);
                    world.getWorldRoot().attachChild(cyan);
                    cyan.setLocalTranslation(worldTranslation);
                    cyan.move(0f, 1f, 0f);
                    cyan.addControl(new CTimedExistence(5f));
                    cyan.emitAllParticles();

                    ParticleEmitter white = createWhite(radius, remainTime);
                    world.getWorldRoot().attachChild(white);
                    white.setLocalTranslation(worldTranslation);
                    white.move(0f, 1f, 0f);
                    white.addControl(new CTimedExistence(5f));
                    white.emitAllParticles();

                    AudioNode sound = new AudioNode(assets,
                            "Effects/Sound/MindPoison.wav");
                    ((Node) spatial).attachChild(sound);
                    sound.setPositional(true);
                    sound.setReverbEnabled(false);
                    sound.setVolume(2f);
                    sound.play();

                    return false;
                }
            });
        }

        return node;
    }
}
