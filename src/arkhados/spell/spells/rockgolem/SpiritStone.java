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
import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.controls.RotationControl;
import arkhados.controls.SpellCastControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SpiritStone extends Spell {

    public SpiritStone(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 8f;
        final float range = 80f;
        final float castTime = 0.2f;

        final SpiritStone spell = new SpiritStone("SpiritStone", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new SpiritStoneCastAction(spell, worldManager);
            }
        };

        spell.nodeBuilder = new SpiritStoneBuilder();

        return spell;
    }
}

/**
 * SpiritStoneCastAction. NOTE: This is very much like CastOnGroundAction. Consider reusing that one
 *
 * @author william
 */
class SpiritStoneCastAction extends EntityAction {

    private Spell spell;
    private WorldManager worldManager;

    public SpiritStoneCastAction(Spell spell, WorldManager worldManager) {
        this.spell = spell;
        this.worldManager = worldManager;
    }

    @Override
    public boolean update(float tpf) {
        SpellCastControl castControl = spatial.getControl(SpellCastControl.class);
        Vector3f target = castControl.getClosestPointToTarget(spell).setY(10f);
        int playerId = spatial.getUserData(UserDataStrings.PLAYER_ID);
        worldManager.addNewEntity(spell.getId(), target, Quaternion.IDENTITY, playerId);
        return false;
    }
}

class SpiritStoneBuilder extends AbstractNodeBuilder {

    @Override
    public Node build() {
        Node node = (Node) assetManager.loadModel("Models/SpiritStone.j3o");
        for (Spatial childToScale : node.getChildren()) {
            childToScale.scale(3f);
        }

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 145f);
        node.setUserData(UserDataStrings.MASS, 600f);
        node.setUserData(UserDataStrings.DAMAGE, 0f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);
        node.setUserData(UserDataStrings.INCAPACITATE_LENGTH, 0f);

        // TODO: Put sound effect that's different
//        if (worldManager.isClient()) {
//            AudioNode sound = new AudioNode(assetManager, "Effects/Sound/MagmaBash.wav");
//            node.attachChild(sound);
//            sound.setPositional(true);
//            sound.setReverbEnabled(false);
//            sound.setVolume(1f);
//            sound.play();
//        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(8f);
        SpiritStonePhysicsControl physicsBody = new SpiritStonePhysicsControl(collisionShape,
                (Float) node.getUserData(UserDataStrings.MASS), worldManager);
        node.addControl(physicsBody);
        physicsBody.setCollisionGroup(CollisionGroups.SPIRIT_STONE);
        physicsBody.removeCollideWithGroup(CollisionGroups.SPIRIT_STONE);
        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS | CollisionGroups.PROJECTILES);
        physicsBody.setAngularDamping(1f);
        
        node.addControl(new TimedExistenceControl(8f, true));
        node.addControl(new RotationControl(0f, 2f, 0f));

        return node;
    }
}