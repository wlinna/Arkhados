/*    This file is part of JMageBattle.

    JMageBattle is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JMageBattle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */
package magebattle.spells;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
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
import magebattle.WorldManager;
import magebattle.controls.ProjectileControl;

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

        Spell fireball = makeFireBall();
        Spells.put(fireball.getName(), fireball);

    }

    public static HashMap<String, Spell> getSpells() {
        return Spells;
    }
    private final String name;
    private final List<Float> cooldowns;
    private final List<Float> ranges;
    private final boolean isSelfCast;
    private Node node;

    private Spell(String name, List<Float> cooldowns, List<Float> ranges, boolean isSelfCast) {
        this.name = name;
        this.cooldowns = cooldowns;
        this.ranges = ranges;
        this.isSelfCast = isSelfCast;
    }

    private static Spell makeFireBall() {
        final List<Float> cooldowns = new ArrayList<Float>();
        cooldowns.add(5.0f);
        final List<Float> ranges = new ArrayList<Float>();
        ranges.add(40.0f);

        Sphere sphere = new Sphere(32, 32, 1.0f);
        Spell spell = new Spell("Fireball", cooldowns, ranges, false);
        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        spell.node = new Node("projectile");
        spell.node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material material = new Material(Spell.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        spell.node.setMaterial(material);
        spell.node.setUserData("speed-movement", 60.0f);
        spell.node.setUserData("mass", 30.0f);

        if (Spell.worldManager.isClient()) {
            ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 100);
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
            fire.getParticleInfluencer().setVelocityVariation(1.0f);
            fire.setRandomAngle(true);
            spell.node.attachChild(fire);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(5.0f);
//        spell.spatial.
        spell.node.addControl(new RigidBodyControl(collisionShape, (Float) spell.node.getUserData("mass")));
        spell.node.addControl(new ProjectileControl());

        return spell;
    }

    public String getName() {
        return this.name;
    }

    public Float getCooldown(int level) {
        return this.cooldowns.get(level - 1);
    }

    public Float getRange(int level) {
        return this.ranges.get(level - 1);
    }

    public boolean isIsSelfCast() {
        return this.isSelfCast;
    }

    public Node getNode() {
        return this.node;
    }

    public Node getNodeClone() {
        return this.node.clone(true);
    }
}
