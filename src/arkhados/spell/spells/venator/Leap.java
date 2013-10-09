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
package arkhados.spell.spells.venator;

import arkhados.SpatialDistancePair;
import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.IncapacitateCC;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import java.util.List;

/**
 *
 * @author william
 */
public class Leap extends Spell {

    public Leap(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 10f;
        final float range = 100f;
        final float castTime = 0.2f;

        final Leap spell = new Leap("Leap", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Vector3f vec) {
                return new CastLeapAction();
            }
        };
        spell.nodeBuilder = null;

        return spell;
    }
}

class CastLeapAction extends EntityAction {

    private float forwardSpeed = 105f;

    private void motionPathVersion() {


        final CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
        physics.switchToMotionCollisionMode();
        Vector3f displacement = physics.getTargetLocation().subtract(super.spatial.getLocalTranslation());


        final MotionPath path = new MotionPath();
        // We set y to 1 to prevent ground collision on start

        final Vector3f startLocation = super.spatial.getLocalTranslation().clone().setY(1f);
        final Vector3f finalLocation = physics.getTargetLocation().clone().setY(1f);
        path.addWayPoint(startLocation);
        path.addWayPoint(super.spatial.getLocalTranslation().add(displacement.divide(2)).setY(displacement.length() / 3f));
        path.addWayPoint(finalLocation);
//        path.setCurveTension(1f);
//        path.setPathSplineType(Spline.SplineType.CatmullRom);


        MotionEvent motionControl = new MotionEvent(super.spatial, path);
//        motionControl.setDirectionType(MotionEvent.Direction.PathAndRotation);
//        motionControl.setRotation(new Quaternion().fromAngleNormalAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
        motionControl.setInitialDuration(finalLocation.distance(startLocation) / this.forwardSpeed);
        motionControl.setSpeed(2f);

        path.addListener(new MotionPathListener() {
            private void landingEffect() {
                List<SpatialDistancePair> spatialsOnDistance =  WorldManager.getSpatialsWithinDistance(spatial, 10f);
                if (spatialsOnDistance == null) {
                    return;
                }
                SpatialDistancePair pairWithSmallestDistance = null;
                for (SpatialDistancePair spatialDistancePair : spatialsOnDistance) {
                    // Check if spatial is character

                    if (spatialDistancePair.spatial.getControl(InfluenceInterfaceControl.class) == null) {
                        continue;
                    }

                    if (pairWithSmallestDistance == null) {
                        pairWithSmallestDistance = spatialDistancePair;
                    }
                    if (spatialDistancePair.distance < pairWithSmallestDistance.distance) {
                        pairWithSmallestDistance = spatialDistancePair;
                    }
                }
                if (pairWithSmallestDistance != null) {
                    InfluenceInterfaceControl influenceInterface = pairWithSmallestDistance.spatial.getControl(InfluenceInterfaceControl.class);
                    influenceInterface.doDamage(200f);
                    influenceInterface.addCrowdControlEffect(new IncapacitateCC(1f, -1));
                }
            }

            public void onWayPointReach(MotionEvent motionControl, int wayPointIndex) {
                if (path.getNbWayPoints() == wayPointIndex + 1) {
                    physics.switchToNormalPhysicsMode();
                    this.landingEffect();
                }
            }
        });

        motionControl.play();
    }

    @Override
    public boolean update(float tpf) {
        this.motionPathVersion();
        return false;
    }
}