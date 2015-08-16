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
import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.controls.CAreaEffect;
import arkhados.controls.CRotation;
import arkhados.controls.CSpellCast;
import arkhados.controls.CSyncInterpolation;
import arkhados.controls.CTimedExistence;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.influences.SlowInfluence;
import arkhados.spell.influences.SpeedInfluence;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SpiritStone extends Spell {

    static final float COOLDOWN = 8f;
    static final float RANGE = 80f;
    static final float CAST_TIME = 0.3f;

    {
        iconName = "SpiritStone.png";
    }

    public SpiritStone(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final SpiritStone spell =
                new SpiritStone("Spirit Stone", COOLDOWN, RANGE, CAST_TIME);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new ASpiritStoneCast(spell, world);
            }
        };

        spell.nodeBuilder = new SpiritStoneBuilder(true);

        return spell;
    }
}

/**
 * SpiritStoneCastAction. NOTE: This is very much like CastOnGroundAction.
 * Consider reusing that one
 *
 * @author william
 */
class ASpiritStoneCast extends EntityAction {

    private Spell spell;
    private World world;

    public ASpiritStoneCast(Spell spell, World world) {
        this.spell = spell;
        this.world = world;
    }

    @Override
    public boolean update(float tpf) {
        CSpellCast castControl = spatial.getControl(CSpellCast.class);
        Vector3f target = castControl.getClosestPointToTarget(spell).setY(10f);
        int playerId = spatial.getUserData(UserData.PLAYER_ID);
        world.addNewEntity(spell.getId(), target,
                Quaternion.IDENTITY, playerId);
        return false;
    }
}

class SpiritStoneBuilder extends AbstractNodeBuilder {

    private final boolean primary;
    private final float duration;
    private final float influenceRadius;

    public SpiritStoneBuilder(boolean primary) {
        this.primary = primary;
        duration = primary ? 8f : 5f;
        influenceRadius = primary ? 30f : 18f;
    }

    @Override
    public Node build(BuildParameters params) {
        Node node = (Node) assetManager.loadModel("Models/SpiritStone.j3o");
        node.setLocalTranslation(params.location);

        for (Spatial childToScale : node.getChildren()) {
            childToScale.scale(3f);
        }

        node.setUserData(UserData.SPEED_MOVEMENT, 145f);
        node.setUserData(UserData.MASS, 600f);
        node.setUserData(UserData.DAMAGE, 0f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);
        node.setUserData(UserData.INCAPACITATE_LENGTH, 0f);

        // TODO: Put sound effect that's different
//        if (world.isClient()) {
//            AudioNode sound = new AudioNode(assetManager, "Effects/Sound/MagmaBash.wav");
//            node.attachChild(sound);
//            sound.setPositional(true);
//            sound.setReverbEnabled(false);
//            sound.setVolume(1f);
//            sound.play();
//        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(5f);
        CSpiritStonePhysics physicsBody =
                new CSpiritStonePhysics(collisionShape,
                (float) node.getUserData(UserData.MASS), world);
        node.addControl(physicsBody);
        physicsBody.setCollisionGroup(CollisionGroups.SPIRIT_STONE);
        physicsBody.removeCollideWithGroup(CollisionGroups.SPIRIT_STONE);
        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS
                | CollisionGroups.PROJECTILES);
        physicsBody.setAngularDamping(1f);

        node.addControl(new CTimedExistence(duration, true));
        node.addControl(new CRotation(0f, 2f, 0f));
        node.addControl(new CSyncInterpolation());

        if (world.isServer()) {
            GhostControl ghost = new GhostControl(new CylinderCollisionShape(
                    new Vector3f(influenceRadius, 0.05f, influenceRadius), 1));
            ghost.setCollideWithGroups(CollisionGroups.CHARACTERS);
            ghost.setCollisionGroup(CollisionGroups.NONE);
            node.addControl(ghost);

            CAreaEffect cAreaOfEffect = new CAreaEffect(ghost);
            node.addControl(cAreaOfEffect);

            if (primary) {
                SpeedInfluence speedInfluence = new SpeedInfluence();
                speedInfluence.setConstant(1f);
                cAreaOfEffect.addInfluence(speedInfluence);
            } else {
                SlowInfluence slowInfluence = new SlowInfluence();
                slowInfluence.setSlowFactor(0.8f);
                cAreaOfEffect.addInfluence(slowInfluence);
            }

        }

        return node;
    }
}