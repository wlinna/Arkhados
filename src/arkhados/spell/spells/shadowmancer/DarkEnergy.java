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

import arkhados.CollisionGroups;
import arkhados.Globals;
import arkhados.World;
import arkhados.actions.ASplash;
import arkhados.actions.cast.ACastProjectile;
import arkhados.characters.EliteSoldier;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CTimedExistence;
import arkhados.effects.particle.ParticleEmitter;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.Spell;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.spell.buffs.HealOverTimeBuff;
import arkhados.util.DistanceScaling;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

/**
 * Shadowmancer's main buff (M2) spell. 
 */
public class DarkEnergy extends Spell {

    public static final float SPLASH_RADIUS = 25f;

    {
        iconName = "DarkEnergy.png";
        setMoveTowardsTarget(false);
    }

    public DarkEnergy(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 7f;
        final float range = 120f;
        final float castTime = 0.4f;

        final DarkEnergy spell = new DarkEnergy("Dark Energy",
                cooldown, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f location) -> {
            ACastProjectile castProjectile = new ACastProjectile(spell, world);
            castProjectile.setTypeId(EliteSoldier.ACTION_ROCKET_LAUNCHER);
            castProjectile.detonateAtTarget(true);
            return castProjectile;
        };

        spell.nodeBuilder = new EnergyBuilder();

        return spell;
    }
}

class EnergyBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createEnergyEmitter() {
        ParticleEmitter energy = new ParticleEmitter("energy-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material material = new Material(assets,
                "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assets.loadTexture("Effects/flame_alpha.png"));
        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);
        energy.setMaterial(material);
        energy.setImagesX(2);
        energy.setImagesY(2);
        energy.setSelectRandomImage(true);
        energy.setStartColor(new ColorRGBA(0f, 0f, 0f, 0.8f));
        energy.setEndColor(new ColorRGBA(0f, 0f, 0f, 0.6f));
        energy.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        energy.setStartSize(4.5f);
        energy.setEndSize(0.5f);
        energy.setGravity(Vector3f.ZERO);
        energy.setLowLife(0.1f);
        energy.setHighLife(0.1f);
        energy.setParticlesPerSec(100);

        energy.setRandomAngle(true);
        return energy;
    }

    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 1);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        projectileGeom.setCullHint(Spatial.CullHint.Always);

        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);
        Material material = new Material(assets,
                "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);

        node.setUserData(UserData.SPEED_MOVEMENT, 180f);
        node.setUserData(UserData.MASS, 0.1f);
        node.setUserData(UserData.DAMAGE, 0f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        if (world.isClient()) {
            ParticleEmitter energy = createEnergyEmitter();
            node.attachChild(energy);

            node.addControl(new CEntityEvent());

            AEnergyRemoval removalAction = new AEnergyRemoval();
            removalAction.setEnergyEmitter(energy);

            node.getControl(CEntityEvent.class)
                    .setOnRemoval(removalAction);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(6f);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(CollisionGroups.NONE);
        physicsBody.setCollideWithGroups(CollisionGroups.NONE);

        node.addControl(physicsBody);

        CProjectile projectileControl = new CProjectile();

        ASplash splash = new ASplash(DarkEnergy.SPLASH_RADIUS)
                .distanceScaling(DistanceScaling.CONSTANT).heal(0);
        splash.addBuff(new HealOverTimeBuff.MyBuilder(3f).healPerSec(30f));
        splash.addBuff(new ArmorBuff.MyBuilder(4f, 35f, 0.5f));

        splash.setSpatial(node);
        projectileControl.setSplashAction(splash);

        node.addControl(new CSpellBuff());
        
        node.addControl(projectileControl);
        physicsBody.setCollisionGroup(CollisionGroups.NONE);
        physicsBody.setCollideWithGroups(CollisionGroups.NONE);

        return node;
    }
}

class AEnergyRemoval implements ARemovalEvent {

    private ParticleEmitter dark;
    private final AudioNode sound;

    public AEnergyRemoval() {
        sound = new AudioNode(Globals.assets,
                "Effects/Sound/DarkEnergy.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
    }

    public void setEnergyEmitter(ParticleEmitter energy) {
        this.dark = energy;
    }

    @Override
    public void exec(World world, int reason) {
        Vector3f worldTranslation = dark.getParent().getLocalTranslation();

        Node node = new Node("dark-energy-explosion");
        world.getWorldRoot().attachChild(node);
        node.setLocalTranslation(worldTranslation);

        dark.removeFromParent();
        node.attachChild(dark);
        node.attachChild(sound);
        dark.setLocalTranslation(Vector3f.ZERO);
        node.addControl(new CTimedExistence(6f));

        dark.setStartColor(new ColorRGBA(0f, 0f, 0f, 0.7f));
        dark.setEndColor(new ColorRGBA(0f, 0f, 0f, 0f));
        dark.setLowLife(0.4f);
        dark.setHighLife(0.5f);
        dark.setNumParticles(120);
        dark.setStartSize(5.5f);
        dark.setEndSize(11f);
        dark.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(45f));
        dark.getParticleInfluencer().setVelocityVariation(1f);

        dark.emitAllParticles();
        dark.setParticlesPerSec(0f);

        sound.setVolume(5f);
        sound.play();
    }
}
