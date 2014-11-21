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
package arkhados.spell.spells.rockgolem;

import arkhados.CollisionGroups;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.PetrifyCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author william
 */


public class SealingBoulder extends Spell {
    {
        iconName = "SealingBoulder.png";
    }

    public SealingBoulder(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }
    
    public static Spell create() {
        final float cooldown = 7f;
        final float range = 120f;
        final float castTime = 0.4f;

        final SealingBoulder spell = new SealingBoulder("SealingBoulder", cooldown, range, castTime);
        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                final CastProjectileAction action = new CastProjectileAction(spell, worldManager);
                return action;
            }
        };
        
        spell.nodeBuilder = new SealingBoulderBuilder();

        return spell;
    }        
}

class SealingBoulderBuilder extends AbstractNodeBuilder {

    @Override
    public Node build() {
        Sphere sphere = new Sphere(32, 32, 2.0f);
        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        Node node = new Node("projectile");
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Gray);
        node.setMaterial(material);
        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 145f);
        node.setUserData(UserDataStrings.MASS, 10f);
        node.setUserData(UserDataStrings.DAMAGE, 80f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);
        node.setUserData(UserDataStrings.INCAPACITATE_LENGTH, 7.4f);

        if (worldManager.isClient()) {                        
            AudioNode sound = new AudioNode(assetManager, "Effects/Sound/MagmaBash.wav");
            node.attachChild(sound);
            sound.setPositional(true);
            sound.setReverbEnabled(false);
            sound.setVolume(1f);            
            sound.play();
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(4);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (Float) node.getUserData(UserDataStrings.MASS));
        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);
        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS | CollisionGroups.WALLS);
        node.addControl(physicsBody);

        node.addControl(new ProjectileControl());
        SpellBuffControl buffControl = new SpellBuffControl();
        node.addControl(buffControl);
        buffControl.addBuff(new PetrifyCC(-1, 3f));

        node.getControl(RigidBodyControl.class).setGravity(Vector3f.ZERO);
        return node;
    }
}