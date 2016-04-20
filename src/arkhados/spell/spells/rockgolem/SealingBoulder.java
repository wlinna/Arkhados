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
import arkhados.actions.cast.ACastProjectile;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.spell.Spell;
import arkhados.spell.buffs.PetrifyCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class SealingBoulder extends Spell {

    {
        iconName = "SealingBoulder.png";
    }

    public SealingBoulder(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 7f;
        final float range = 120f;
        final float castTime = 0.5f;

        final SealingBoulder spell
                = new SealingBoulder("SealingBoulder", cooldown, range, castTime);
        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastProjectile action = new ACastProjectile(spell, world);
            return action;
        };

        spell.nodeBuilder = new SealingBoulderBuilder();

        return spell;
    }
}

class SealingBoulderBuilder extends AbstractNodeBuilder {

    @Override
    public Node build(BuildParameters params) {
        Node node = (Node) assets.loadModel("Models/SealingBoulder.j3o");
        node.setLocalTranslation(params.location);

        node.setUserData(UserData.SPEED_MOVEMENT, 145f);
        node.setUserData(UserData.MASS, 10f);
        node.setUserData(UserData.DAMAGE, 120f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);
        node.setUserData(UserData.INCAPACITATE_LENGTH, 7.4f);

        if (world.isClient()) {
            AudioNode sound = new AudioNode(assets,
                    "Effects/Sound/MagmaBash.wav");
            node.attachChild(sound);
            sound.setPositional(true);
            sound.setReverbEnabled(false);
            sound.setVolume(1f);
            sound.play();
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(4);
        
        GhostControl characterCollision = new GhostControl(collisionShape);
        characterCollision.setCollideWithGroups(CollisionGroups.CHARACTERS);
        characterCollision.setCollisionGroup(CollisionGroups.PROJECTILES);
        node.addControl(characterCollision);
        
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));
        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);
        node.addControl(physicsBody);

        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();
        node.addControl(buffControl);
        buffControl.addBuff(new PetrifyCC.MyBuilder(1.9f));

        node.getControl(RigidBodyControl.class).setGravity(Vector3f.ZERO);
        return node;
    }
}
