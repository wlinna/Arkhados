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
import arkhados.actions.ADelay;
import arkhados.actions.EntityAction;
import arkhados.actions.cast.ACastOnGround;
import arkhados.actions.cast.ACastSelfBuff;
import arkhados.controls.CActionQueue;
import arkhados.controls.CAreaEffect;
import arkhados.controls.CCircleVisibility;
import arkhados.controls.CTimedExistence;
import arkhados.effects.EmitterCircleShape;
import arkhados.spell.Spell;
import arkhados.spell.buffs.HealOverTimeBuff;
import arkhados.spell.buffs.SpeedBuff;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import com.jme3.audio.AudioData;
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

public class Energize extends Spell {

    {
        iconName = "MindPoison.png";
    }

    public static class AEnergizeCast extends EntityAction {

        static Spell spell;

        @Override
        public boolean update(float tpf) {
            CActionQueue queue = spatial.getControl(CActionQueue.class);

            queue.enqueueAction(new ACastOnGround(world, spell));
            
            ACastSelfBuff aSelfBuff = new ACastSelfBuff();
            aSelfBuff.addBuff(new PowerBuff.MyBuilder(6f));
            queue.enqueueAction(aSelfBuff);
            return false;
        }
    }

    public Energize(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Energize create() {
        final float cooldown = 7.5f;
        final float range = 100f;
        final float castTime = 0.4f;

        final Energize spell = new Energize("Energize", cooldown,
                range, castTime);

        AEnergizeCast.spell = spell;
        spell.castSpellActionBuilder = (Node caster, Vector3f vec) ->  
                new AEnergizeCast();

        spell.nodeBuilder = new EnergizeBuilder();

        return spell;
    }
}

class EnergizeBuilder extends AbstractNodeBuilder {

    private static final ColorRGBA COLOR = new ColorRGBA(.4f, 0.8f, 0.8f, 0.9f);

    private static final class AFinish extends EntityAction {

        private final CAreaEffect cAreaEffect;
        private final Node node;
        private final float remainTime;

        public AFinish(CAreaEffect cAreaEffect, Node node, float remainTime) {
            this.cAreaEffect = cAreaEffect;
            this.node = node;
            this.remainTime = remainTime;
        }

        @Override
        public boolean update(float tpf) {
            cAreaEffect.addEnterBuff(
                    new HealOverTimeBuff.MyBuilder(3f).healPerSec(33.3f));
            cAreaEffect.addEnterBuff(new SpeedBuff.MyBuilder(0.33f, 0f, 3f));
            node.addControl(new CTimedExistence(remainTime, true));
            return false;
        }
    }

    private ParticleEmitter createCyan(float radius, float remainTime) {
        ParticleEmitter cyan = new ParticleEmitter("purple-emitter",
                ParticleMesh.Type.Triangle, 10 * (int) radius);
        Material material
                = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assets.loadTexture("Effects/flame.png"));
        cyan.setMaterial(material);
        cyan.setImagesX(2);
        cyan.setImagesY(2);
        cyan.setSelectRandomImage(true);
        cyan.setStartColor(new ColorRGBA(0.1f, 0.8f, 0.8f, 0.6f));
        cyan.setEndColor(new ColorRGBA(0.1f, 0.8f, 0.8f, 0.05f));
        cyan.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_Y.mult(2f));
        cyan.setStartSize(6.5f);
        cyan.setEndSize(2.5f);
        cyan.setGravity(Vector3f.ZERO);
        cyan.setLowLife(remainTime * 0.9f);
        cyan.setHighLife(remainTime);
        cyan.setParticlesPerSec(0);
        cyan.getParticleInfluencer().setVelocityVariation(0.2f);
        cyan.setRandomAngle(true);

        EmitterCircleShape emitterShape
                = new EmitterCircleShape(Vector3f.ZERO, radius);
        cyan.setShape(emitterShape);

        return cyan;
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

    private ParticleEmitter createDimCyan(float radius, float delay) {
        ParticleEmitter cyan = new ParticleEmitter("black-emitter",
                ParticleMesh.Type.Triangle, 100 * (int) radius * (int) radius);
        Material mat
                = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture",
                Globals.assets.loadTexture("Effects/flame_alpha.png"));
        mat.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);
        cyan.setMaterial(mat);
        cyan.setImagesX(2);
        cyan.setImagesY(2);
        cyan.setSelectRandomImage(true);
                cyan.setStartColor(new ColorRGBA(0.1f, 0.8f, 0.8f, 0.6f));
        cyan.setEndColor(new ColorRGBA(0.1f, 0.8f, 0.8f, 0.05f));
        cyan.setStartColor(EnergizeBuilder.COLOR);
        cyan.setEndColor(EnergizeBuilder.COLOR);
        cyan.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        cyan.setStartSize(2.5f);
        cyan.setEndSize(4.5f);
        cyan.setGravity(Vector3f.ZERO);
        cyan.setLowLife(delay);
        cyan.setHighLife(delay);
        cyan.setParticlesPerSec(0);
        cyan.getParticleInfluencer().setVelocityVariation(0f);
        cyan.setRandomAngle(true);

        EmitterCircleShape emitterShape
                = new EmitterCircleShape(Vector3f.ZERO, radius);
        cyan.setShape(emitterShape);

        return cyan;
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

        mat.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);
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

            actionQueue.enqueueAction(new AFinish(cAreaEffect, node,
                    remainTime));
        } else if (world.isClient()) {
            ParticleEmitter black = createDimCyan(radius / 15f, delay);
            node.attachChild(black);
            black.setLocalTranslation(0f, 1f, 0f);
            black.emitAllParticles();

            actionQueue.enqueueAction(new EntityAction() {
                @Override
                public boolean update(float tpf) {
                    Vector3f worldTranslation = spatial.getWorldTranslation();

                    final ParticleEmitter cyan = createCyan(radius, remainTime);
                    world.getWorldRoot().attachChild(cyan);
                    cyan.setLocalTranslation(worldTranslation);
                    cyan.move(0f, 1f, 0f);
                    cyan.addControl(new CTimedExistence(5f));
                    cyan.emitAllParticles();

                    final ParticleEmitter white = 
                            createWhite(radius, remainTime);
                    world.getWorldRoot().attachChild(white);
                    white.setLocalTranslation(worldTranslation);
                    white.move(0f, 1f, 0f);
                    white.addControl(new CTimedExistence(5f));
                    white.emitAllParticles();

                    AudioNode sound = new AudioNode(assets,
                            "Effects/Sound/MindPoison.wav",
                            AudioData.DataType.Buffer);
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
