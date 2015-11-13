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
package arkhados.spell.spells.shadowmancer;

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.actions.cast.ACastProjectile;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.effects.EmitterArcShape;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.SilenceCC;
import arkhados.spell.buffs.SlowCC;
import arkhados.spell.buffs.SpeedBuff;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

public class ShadowSickness extends Spell {

    static final float DURATION = 5f;

    {
        iconName = "ShadowSickness.png";
    }

    public ShadowSickness(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 7f;
        final float range = 100f;
        final float castTime = 0.37f;

        final ShadowSickness spell = new ShadowSickness("Shadow Sickness",
                cooldown, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec)
                -> new ACastProjectile(spell, world);

        spell.nodeBuilder = new SicknessBuilder();

        return spell;
    }
}

class SicknessBuilder extends AbstractNodeBuilder {
    private static final float RADIUS = 2f;

    private ParticleEmitter createPurpleEmitter() {
        ParticleEmitter purple = new ParticleEmitter("poison-emitter",
                ParticleMesh.Type.Triangle, 400);
        Material mat = new Material(assets,
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assets.loadTexture("Effects/flame.png"));
        purple.setMaterial(mat);
        purple.setImagesX(2);
        purple.setImagesY(2);
        purple.setSelectRandomImage(true);
        purple.setStartColor(new ColorRGBA(0.8f, 0.015f, 0.8f, 0.6f));
        purple.setEndColor(new ColorRGBA(0.8f, 0.015f, 0.8f, 0.6f));
        purple.setStartSize(1f);
        purple.setEndSize(2f);
        purple.setLowLife(0.2f);
        purple.setHighLife(0.2f);
        purple.setParticlesPerSec(300);
        purple.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        purple.getParticleInfluencer().setVelocityVariation(0);
        purple.setInWorldSpace(false);
        EmitterShape shape = 
                new EmitterArcShape(Vector3f.ZERO, 0.3f, 0.8f, 1.5f * RADIUS);
        purple.setShape(shape);

        purple.setRandomAngle(true);
        return purple;
    }

    @Override
    public Node build(BuildParameters params) {        
        Sphere sphere = new Sphere(32, 32, 1.0f);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        projectileGeom.setCullHint(Spatial.CullHint.Always);

        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        node.setUserData(UserData.SPEED_MOVEMENT, 100f);
        node.setUserData(UserData.MASS, 30f);
        node.setUserData(UserData.DAMAGE, 0f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        SphereCollisionShape collisionShape = new SphereCollisionShape(RADIUS);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));

        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);

        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS
                | CollisionGroups.WALLS);

        node.addControl(physicsBody);

        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();
        buffControl.addBuff(new Sickness.MyBuilder(ShadowSickness.DURATION));
        buffControl.addBuff(new SilenceCC.MyBuilder(1.5f));

        node.addControl(buffControl);
        
        if (world.isClient()) {
            ParticleEmitter particles = createPurpleEmitter();
            node.attachChild(particles);
        }

        return (Node) node;
    }
}

class Sickness extends AbstractBuff {

    private Spatial owner;
    private Spatial target;
    private static final float RANGE_SQUARED = FastMath.sqr(70f);
    private SpeedBuff speedBuff;
    private SlowCC slowCc;
    private float dps = 50f;

    public Sickness(float duration) {
        super(duration);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        super.attachToCharacter(targetInterface);
        target = targetInterface.getSpatial();
        owner = getOwnerInterface().getSpatial();

        speedBuff = (SpeedBuff) new SpeedBuff.MyBuilder(0, 0, duration).build();
        slowCc = (SlowCC) new SlowCC.MyBuilder(duration, 0f).build();

        speedBuff.attachToCharacter(getOwnerInterface());
        slowCc.attachToCharacter(targetInterface);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        float distanceSquared = target.getLocalTranslation()
                .distanceSquared(owner.getLocalTranslation());

        if (distanceSquared > RANGE_SQUARED) {
            return;
        }

        CharacterInteraction.harm(getOwnerInterface(), targetInterface,
                dps * tpf, null, false);
        CharacterInteraction.help(getOwnerInterface(), getOwnerInterface(),
                dps * tpf, null);

        float currentSpeedFactor = speedBuff.getFactor();
        speedBuff.setFactor(currentSpeedFactor + tpf * .1f);

        float currentSlowFactor = slowCc.getSlowFactor();
        slowCc.setSlowFactor(currentSlowFactor - tpf * .1f);
    }

    static class MyBuilder extends AbstractBuffBuilder {

        public MyBuilder(float duration) {
            super(duration);
        }

        @Override
        public AbstractBuff build() {
            Sickness sickness = new Sickness(duration);
            return set(sickness);
        }
    }
}
