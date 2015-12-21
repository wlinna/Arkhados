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
import arkhados.actions.ACastingSpell;
import arkhados.actions.EntityAction;
import arkhados.actions.cast.AMeleeAttack;
import arkhados.characters.Venator;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.effects.EffectHandle;
import arkhados.effects.RandomChoiceEffect;
import arkhados.effects.SimpleSoundEffect;
import arkhados.effects.WorldEffect;
import arkhados.effects.particle.ParticleEmitter;
import arkhados.spell.Spell;
import arkhados.util.Selector;
import arkhados.util.UserData;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.function.Predicate;

public class Rend extends Spell {

    static final float RANGE = 13.5f;

    {
        iconName = "rend.png";
        multipart = true;
    }

    public final WorldEffect castEffect = new RendBloodEffect();

    public Rend(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.5f;
        final float castTime = 0.17f;

        final Rend spell = new Rend("Rend", cooldown, RANGE, castTime);
        spell.setCanMoveWhileCasting(true);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ADoubleMeleeAttack action
                    = new ADoubleMeleeAttack(spell);
            action.setTypeId(Venator.ANIM_SWIPE_LEFT);
            return action;
        };

        spell.nodeBuilder = null;
        return spell;
    }
}

class ADoubleMeleeAttack extends EntityAction {

    private static final class AEnd extends EntityAction {

        @Override
        public boolean update(float tpf) {
            // HACK: This should happen automatically
            spatial.getControl(CSpellCast.class).setCasting(false);
            // TODO: MAKE SURE it's okay to disable this
            // spatial.getControl(UserInputControl.class).restoreWalking();
            return false;
        }
    }

    private final Spell spell;

    public ADoubleMeleeAttack(Spell spell) {
        this.spell = spell;
    }

    @Override
    public boolean update(float tpf) {
        // TODO: Make an attack start with different animation than previous one
        float range = spell.getRange();
        CActionQueue queue = spatial.getControl(CActionQueue.class);
        final VenatorMeleeAttack action1 = new VenatorMeleeAttack(75f, range);
        ACastingSpell action2Anim = new ACastingSpell(spell, true);
        VenatorMeleeAttack action2 = new VenatorMeleeAttack(85f, range);

        // action1 already has the default spell casting animation
        action2Anim.setTypeId(Venator.ANIM_SWIPE_RIGHT);
        queue.enqueueAction(action1);
        queue.enqueueAction(action2Anim);
        queue.enqueueAction(action2);

        queue.enqueueAction(new AEnd());

        return false;
    }

    private class VenatorMeleeAttack extends AMeleeAttack {

        public VenatorMeleeAttack(float damage, float range) {
            super(damage, range);
        }

        @Override
        public void setTypeIdOnHit() {
            setTypeId(Venator.ACTION_REND_HIT);
        }
    }
}

class RendBloodEffect implements WorldEffect {

    private static final RandomChoiceEffect randomSound
            = new RandomChoiceEffect();
    
    private static final Vector3f velocity = Vector3f.UNIT_XYZ.mult(7f);

    static {
        SimpleSoundEffect e1
                = new SimpleSoundEffect("Effects/Sound/RendHit1.wav");
        SimpleSoundEffect e2
                = new SimpleSoundEffect("Effects/Sound/RendHit2.wav");
        SimpleSoundEffect e3
                = new SimpleSoundEffect("Effects/Sound/RendHit3.wav");
        SimpleSoundEffect e4
                = new SimpleSoundEffect("Effects/Sound/RendHit4.wav");
        e1.setVolume(0.3f);
        e2.setVolume(0.3f);
        e3.setVolume(0.3f);
        e4.setVolume(0.3f);
        randomSound.add(e1);
        randomSound.add(e2);
        randomSound.add(e3);
        randomSound.add(e4);
    }

    static ParticleEmitter createBloodEmitter() {
        ParticleEmitter blood = new ParticleEmitter("blood-emitter",
                ParticleMesh.Type.Triangle, 150);
        Material mat = new Material(Globals.assets,
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture",
                Globals.assets.loadTexture("Effects/flame_alpha.png"));
        mat.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);      
        blood.setMaterial(mat);
        blood.setImagesX(2);
        blood.setImagesY(2);
        blood.setSelectRandomImage(true);
        blood.setStartColor(new ColorRGBA(0.5f, 0f, 0.0f, 1.0f));
        blood.setEndColor(new ColorRGBA(0.5f, 0f, 0.0f, 0.5f));
        blood.getParticleInfluencer().setInitialVelocity(velocity);
        blood.getParticleInfluencer().setVelocityVariation(1f);
        blood.setStartSize(1.5f);
        blood.setEndSize(1f);
        
        blood.setGravity(Vector3f.ZERO);
        blood.setLowLife(0.3f);
        blood.setHighLife(0.3f);
        blood.setParticlesPerSec(0);

        blood.setRandomAngle(true);
        return blood;
    }

    private SpatialDistancePair checkHit(Spatial spatial,
            CCharacterPhysics cPhysics) {
        Vector3f hitDirection = cPhysics.getViewDirection()
                .normalize();

        final int myTeamId = spatial.getUserData(UserData.TEAM_ID);

        Predicate<SpatialDistancePair> pred = (SpatialDistancePair value) -> {
            if (value.spatial == spatial) {
                return false;
            }

            Integer nullableTeamId
                    = value.spatial.getUserData(UserData.TEAM_ID);
            if (nullableTeamId == null) {
                return false;
            }

            CInfluenceInterface influenceInterface = value.spatial
                    .getControl(CInfluenceInterface.class);

            return influenceInterface != null
                    && !nullableTeamId.equals(myTeamId);
        };

        return Selector.giveClosest(Selector.coneSelect(new ArrayList<>(), pred,
                spatial.getLocalTranslation(), hitDirection,
                Rend.RANGE, (float) Math.toRadians(50f)));
    }

    @Override
    public EffectHandle execute(Node root, Vector3f loc, String p) {
        randomSound.execute(root, loc, p);

        CCharacterPhysics cPhysics = root
                .getControl(CCharacterPhysics.class);
        cPhysics.getCapsuleShape().getHeight();
        SpatialDistancePair hit = checkHit(root, cPhysics);

        if (hit == null || hit.distance > Rend.RANGE) {
            return null;
        }

        Vector3f dir = hit.spatial.getLocalTranslation()
                .subtract(root.getLocalTranslation()).normalizeLocal();
        
        Ray ray = new Ray(root.getLocalTranslation().add(0f,
                cPhysics.getCapsuleShape().getHeight() * 0.66f, 0f), dir);
        CollisionResults collisions = new CollisionResults();
        hit.spatial.collideWith(ray, collisions);
        Vector3f hitLoc = collisions.getClosestCollision().getContactPoint();

        ParticleEmitter e = createBloodEmitter();
        ((Node) hit.spatial).attachChild(e);
        hit.spatial.worldToLocal(hitLoc, e.getLocalTranslation());
        e.emitAllParticles();
        return null;
    }
}
