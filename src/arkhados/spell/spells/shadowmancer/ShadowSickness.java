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
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.ACastProjectile;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.SlowCC;
import arkhados.spell.buffs.SpeedBuff;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ShadowSickness extends Spell {

    {
        iconName = "damaging_dagger.png";
    }

    public ShadowSickness(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 6f;
        final float range = 100f;
        final float castTime = 0.35f;

        final ShadowSickness spell = new ShadowSickness("Shadow Sickness",
                cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new ACastProjectile(spell, worldManager);
            }
        };

        spell.nodeBuilder = new SicknessBuilder();

        return spell;
    }
}

class SicknessBuilder extends AbstractNodeBuilder {

    @Override
    public Node build(BuildParameters params) {
        Spatial node = assetManager.loadModel("Models/DamagingDagger.j3o");
        node.setLocalTranslation(params.location);

        node.setUserData(UserData.SPEED_MOVEMENT, 170f);
        node.setUserData(UserData.MASS, 30f);
        node.setUserData(UserData.DAMAGE, 0f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        SphereCollisionShape collisionShape = new SphereCollisionShape(4);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));

        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);

        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS
                | CollisionGroups.WALLS);

        node.addControl(physicsBody);

        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();        
        buffControl.addBuff(new Sickness.MyBuilder(5f));

        node.addControl(buffControl);

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

        speedBuff = (SpeedBuff) new SpeedBuff.MyBuilder(0, 0, 5f).build();
        slowCc = (SlowCC) new SlowCC.MyBuilder(5f, 0f).build();

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
        slowCc.setSlowFactor(currentSlowFactor + tpf * .1f);
    }

    static class MyBuilder extends AbstractBuffBuilder {

        public MyBuilder(float duration) {
            super(duration);
        }

        @Override
        public AbstractBuff build() {
            Sickness sickness = new Sickness(5f);
            return set(sickness);
        }
    }
}