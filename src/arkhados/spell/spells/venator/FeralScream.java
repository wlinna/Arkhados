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
package arkhados.spell.spells.venator;

import arkhados.Globals;
import arkhados.SpatialDistancePair;
import arkhados.actions.ADelay;
import arkhados.actions.EntityAction;
import arkhados.characters.Venator;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CTimedExistence;
import arkhados.effects.EffectHandle;
import arkhados.effects.SimpleSoundEffect;
import arkhados.effects.WorldEffect;
import arkhados.effects.particle.ParticleEmitter;
import arkhados.spell.Spell;
import arkhados.spell.buffs.FearCC;
import arkhados.util.Selector;
import arkhados.util.UserData;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class FeralScream extends Spell {

    private static final float RANGE = 45f;
    private static final float HALF_ANGLE = (float) Math.toRadians(45f);

    {
        iconName = "feral_scream.png";
    }

    public FeralScream(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static FeralScream create() {
        final float cooldown = 8f;
        final float range = 45f;
        final float castTime = 0.3f;

        FeralScream spell = new FeralScream("Feral Scream", cooldown,
                range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec)
                -> new AFeralScream(RANGE, HALF_ANGLE);
        return spell;
    }

    public static class Effect implements WorldEffect {

        private static final float END_SIZE
                = 2f * RANGE * FastMath.tan(HALF_ANGLE) * 0.3f;

        private static final WorldEffect soundEffect
                = new SimpleSoundEffect("Effects/Sound/FeralScream.wav");

        private ParticleEmitter createWave(Vector3f velocity) {
            ParticleEmitter wave = new ParticleEmitter("scream-emitter",
                    ParticleMesh.Type.Triangle, 100);
            Material mat = new Material(Globals.assets,
                    "Common/MatDefs/Misc/Particle.j3md");
            mat.setTexture("Texture",
                    Globals.assets.loadTexture("Effects/shockwave.png"));
            wave.setMaterial(mat);
            wave.setImagesX(1);
            wave.setImagesY(1);

            wave.setGravity(Vector3f.ZERO);

            wave.setStartColor(new ColorRGBA(0.8f, 0.05f, 0.05f, 1f));
            wave.setEndColor(new ColorRGBA(0.8f, 0.05f, 0.05f, 0f));
            wave.setLowLife(0.21f);
            wave.setHighLife(0.21f);
            wave.setStartSize(0.4f);
            wave.setEndSize(END_SIZE);
            wave.getParticleInfluencer().setInitialVelocity(velocity);
            wave.getParticleInfluencer().setVelocityVariation(0f);
            wave.setParticlesPerSec(30f);

            return wave;
        }

        @Override
        public EffectHandle execute(Node root, Vector3f location,
                String parameter) {
            CCharacterPhysics phys = root.getControl(CCharacterPhysics.class);
            float height = phys.getCapsuleShape().getHeight();
            Vector3f startLocation = new Vector3f(root.getWorldTranslation())
                    .setY(height);
            soundEffect.execute(null, startLocation, null);

            Vector3f vel = phys.getViewDirection().normalize()
                    .multLocal(RANGE / 0.3f);

            final ParticleEmitter emitter = createWave(vel);
            emitter.setLocalTranslation(startLocation);
            root.getParent().attachChild(emitter);

            CActionQueue actionQueue = new CActionQueue();
            emitter.addControl(actionQueue);
            actionQueue.enqueueAction(new ADelay(0.2f));
            actionQueue.enqueueAction(new EntityAction() {

                @Override
                public boolean update(float tpf) {
                    emitter.setParticlesPerSec(0f);
                    float timeToExist = emitter.getLowLife();
                    emitter.addControl(new CTimedExistence(timeToExist));

                    return false;
                }
            });

            return null;
        }
    }
}

class AFeralScream extends EntityAction {

    private final float range;
    private final float maxRotationalDifference;

    public AFeralScream(float range, float maxRotationalDifference) {
        this.range = range;
        if (maxRotationalDifference > FastMath.HALF_PI) {
            throw new InvalidParameterException("Does not support higher"
                    + " rotational differences half pi radians");
        }

        this.maxRotationalDifference = maxRotationalDifference;
        setTypeId(Venator.ACTION_FERALSCREAM);
    }

    @Override
    public boolean update(float tpf) {
        CCharacterPhysics physicsControl
                = spatial.getControl(CCharacterPhysics.class);

        Vector3f targetLocation = physicsControl.getTargetLocation();
        final Vector3f viewDirection = targetLocation
                .subtract(spatial.getLocalTranslation()).normalizeLocal();
        spatial.getControl(CCharacterPhysics.class)
                .setViewDirection(viewDirection);

        int myTeam = spatial.getUserData(UserData.TEAM_ID);

        List<SpatialDistancePair> spatialDistances = Selector
                .getSpatialsWithinDistance(new ArrayList<SpatialDistancePair>(),
                        spatial, range, new Selector.IsCharacterOfOtherTeam(myTeam));

        FearCC.MyBuilder fearBuilder = new FearCC.MyBuilder(2f);
        fearBuilder.setOwnerInterface(
                spatial.getControl(CInfluenceInterface.class));

        for (SpatialDistancePair spatialDistancePair : spatialDistances) {
            CInfluenceInterface influenceInterface = spatialDistancePair.spatial
                    .getControl(CInfluenceInterface.class);
            if (!Selector.isInCone(spatial.getLocalTranslation(), viewDirection,
                    maxRotationalDifference, spatialDistancePair.spatial)) {
                continue;
            }

            FearCC fear = fearBuilder.build();

            final Vector3f initialDirection = spatialDistancePair.spatial
                    .getLocalTranslation()
                    .subtract(spatial.getLocalTranslation()).setY(0f);

            fear.setInitialDirection(initialDirection);
            fear.attachToCharacter(influenceInterface);
        }
        return false;
    }
}
