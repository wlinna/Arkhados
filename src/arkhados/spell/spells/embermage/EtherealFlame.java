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

import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.characters.EmberMage;
import arkhados.controls.CCharacterMovement;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.controls.CTimedExistence;
import arkhados.effects.EffectHandle;
import arkhados.effects.EmitterCircleShape;
import arkhados.effects.WorldEffect;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import com.jme3.audio.AudioNode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class EtherealFlame extends Spell {

    public static final float DURATION = 1.6f;
    static final float SPEED = 300f;

    public EtherealFlame(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float range = 70f;
        final float castTime = 0.2f;

        final EtherealFlame spell = new EtherealFlame("Ethereal Flame",
                PurifyingFlame.COOLDOWN, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new AFireTrance(spell);
            }
        };

        return spell;
    }

    public static class Effect implements WorldEffect {

        private ParticleEmitter createFire(float radius) {
            ParticleEmitter fire = new ParticleEmitter("fire-emitter",
                    ParticleMesh.Type.Triangle, 20 * (int) radius);
            Material material =
                    new Material(assetManager,
                    "Common/MatDefs/Misc/Particle.j3md");
            material.setTexture("Texture",
                    assetManager.loadTexture("Effects/flame.png"));
            fire.setMaterial(material);
            fire.setImagesX(2);
            fire.setImagesY(2);
            fire.setSelectRandomImage(true);
            fire.setStartColor(new ColorRGBA(0.95f, 0.15f, 0f, 1f));
            fire.setEndColor(new ColorRGBA(1f, 1f, 0f, 0.5f));
            fire.getParticleInfluencer()
                    .setInitialVelocity(Vector3f.UNIT_Y.mult(80f));
            fire.setStartSize(4.5f);
            fire.setEndSize(4.5f);
            fire.setGravity(Vector3f.ZERO);
            fire.setLowLife(0.3f);
            fire.setHighLife(0.7f);
            fire.setParticlesPerSec((int) (3 * radius * radius));
            fire.getParticleInfluencer().setVelocityVariation(0.05f);
            fire.setRandomAngle(true);

            EmitterCircleShape emitterShape =
                    new EmitterCircleShape(Vector3f.ZERO, radius);
            fire.setShape(emitterShape);

            return fire;
        }

        @Override
        public EffectHandle execute(final Node root, Vector3f location,
                String p) {
            final ParticleEmitter fire = createFire(5f);
            root.attachChild(fire);

            final AudioNode sound = new AudioNode(assetManager,
                    "Effects/Sound/Firewalk.wav");
            root.attachChild(sound);
            sound.setPositional(true);
            sound.setReverbEnabled(false);
            sound.setVolume(1f);
            sound.play();

            return new EffectHandle() {
                @Override
                public void end() {
                    sound.stop();
                    sound.removeFromParent();
                    fire.setParticlesPerSec(0f);

                    CTimedExistence timedExistence = new CTimedExistence(1f);
                    fire.addControl(timedExistence);
                }
            };
        }
    }
}

class AFireTrance extends EntityAction implements ATrance {

    {
        setTypeId(EmberMage.ACTION_ETHEREAL_FLAME);
    }
    private Spell spell;
    private float timeLeft = EtherealFlame.DURATION;
    private CInfluenceInterface cInfluence;
    private CCharacterMovement cMovement;

    public AFireTrance(Spell spell) {
        this.spell = spell;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        cInfluence = spatial.getControl(CInfluenceInterface.class);
        cMovement = spatial.getControl(CCharacterMovement.class);
    }

    @Override
    public boolean update(float tpf) {
        timeLeft -= tpf;
        if (timeLeft <= 0f) {
            return false;
        }

        if (!cInfluence.isAbleToCastWhileMoving()) {
            cMovement.stop();
        }

        return true;
    }

    @Override
    public void activate(Spatial activator) {
        CSpellCast spellCast = spatial.getControl(CSpellCast.class);
        Vector3f target = spellCast.getClosestPointToTarget(spell);
        // We set this to 10 to prevent effect from disappearing while moving
        timeLeft = 10f;

        motion(target);
    }

    private void motion(final Vector3f target) {
        Vector3f start = spatial.getLocalTranslation();

        final MotionPath path = new MotionPath();
        path.setPathSplineType(Spline.SplineType.Linear);
        path.addWayPoint(start);
        path.addWayPoint(target);

        MotionEvent motionControl = new MotionEvent(spatial, path);
        motionControl.setSpeed(1f);
        motionControl.setInitialDuration(
                target.distance(start) / EtherealFlame.SPEED);

        final CCharacterPhysics body =
                spatial.getControl(CCharacterPhysics.class);
        body.lookAt(target);
        body.switchToMotionCollisionMode();

        path.addListener(new MotionPathListener() {
            @Override
            public void onWayPointReach(MotionEvent motionControl,
                    int wayPointIndex) {
                if (path.getNbWayPoints() == wayPointIndex + 1) {
                    body.switchToNormalPhysicsMode();
                    body.warp(target);
                    timeLeft = 0f;
                }
            }
        });

        motionControl.play();
    }

    @Override
    public void end() {
        super.end();
        announceEnd();
    }
}