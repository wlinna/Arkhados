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
package arkhados.spell.spells.elitesoldier;

import arkhados.Globals;
import arkhados.actions.EntityAction;
import arkhados.actions.SplashAction;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.SpellCastControl;
import arkhados.effects.RocketExplosionEffect;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.util.DistanceScaling;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class RocketJump extends Spell {

    {
        super.iconName = "rocket_jump.jpeg";
    }

    public RocketJump(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 7.5f;
        final float range = 100;
        final float castTime = 0.2f;

        final RocketJump spell = new RocketJump("Rocket Jump", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                EntityAction action = new CastRocketJumpAction(spell);
                return action;
            }
        };

        spell.nodeBuilder = null;

        return spell;
    }
}

class CastRocketJumpAction extends EntityAction {

    private static final float forwardSpeed = 105;
    private final Spell spell;

    public CastRocketJumpAction(Spell spell) {
        this.spell = spell;
    }

    private void motionPath() {        
        final CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
        physics.switchToMotionCollisionMode();

        final MotionPath path = new MotionPath();

        // We set y to 1 to prevent ground collision on start
        final Vector3f startLocation = super.spatial.getLocalTranslation().clone().setY(1f);
        final Vector3f finalLocation = super.spatial.getControl(SpellCastControl.class).getClosestPointToTarget(this.spell);

        path.addWayPoint(startLocation);
        path.addWayPoint(super.spatial.getLocalTranslation().add(finalLocation)
                .divideLocal(2).setY(finalLocation.distance(startLocation) / 1.8f));
        path.addWayPoint(finalLocation);

        MotionEvent motionControl = new MotionEvent(super.spatial, path);
        motionControl.setInitialDuration(finalLocation.distance(startLocation) / CastRocketJumpAction.forwardSpeed);
        motionControl.setSpeed(1f);

        physics.setViewDirection(finalLocation.subtract(startLocation));

        path.addListener(new MotionPathListener() {
            @Override
            public void onWayPointReach(MotionEvent motionControl, int wayPointIndex) {
                if (wayPointIndex == path.getNbWayPoints() - 1) {
                    physics.switchToNormalPhysicsMode();
                }
            }
        });
        
        motionControl.play();
    }

    @Override
    public boolean update(float tpf) {
        this.motionPath();
        SplashAction splash = new SplashAction(30, 100, 23000, DistanceScaling.CONSTANT, null);
        splash.setSpatial(spatial);
        splash.update(-1);
        Globals.effectHandler.sendEffect("rocket-explosion", null, spatial.getLocalTranslation());
        return false;
    }
}