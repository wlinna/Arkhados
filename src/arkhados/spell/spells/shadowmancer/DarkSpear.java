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
import arkhados.actions.cast.ACastProjectile;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.spell.Spell;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

public class DarkSpear extends Spell {
    {
        iconName = "DarkSpear.png";
    }

    public DarkSpear(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 8f;
        final float range = 110f;
        final float castTime = 0.35f;

        final DarkSpear spell = new DarkSpear("Dark Spear", cooldown,
                range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) 
                -> new ACastProjectile(spell, world);

        spell.nodeBuilder = new SpearBuilder();

        return spell;
    }
}

class CSpear extends AbstractControl {

    @Override
    protected void controlUpdate(float tpf) {
        float dmg = spatial.getUserData(UserData.DAMAGE);
        spatial.setUserData(UserData.DAMAGE, dmg + tpf * 200f);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }    
}

class SpearBuilder extends AbstractNodeBuilder {

    @Override
    public Node build(BuildParameters params) {
        Node node =
                (Node) assetManager.loadModel("Models/Spear.j3o");
        node.getChild(0).scale(3f);
        node.setLocalTranslation(params.location);

        node.setUserData(UserData.SPEED_MOVEMENT, 170f);
        node.setUserData(UserData.MASS, 30f);
        node.setUserData(UserData.DAMAGE, 200f);
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
        node.addControl(buffControl);

        node.addControl(new CSpear());
        
        return node;
    }
}
