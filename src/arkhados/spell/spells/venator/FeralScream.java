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
import arkhados.spell.buffs.FearCC;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.security.InvalidParameterException;
import java.util.List;

/**
 *
 * @author william
 */
public class FeralScream extends Spell {

    public FeralScream(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static FeralScream create() {
        final float cooldown = 8f;
        final float range = 45f;
        final float castTime = 0.3f;

        final FeralScream spell = new FeralScream("Feral Scream", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new FeralScreamAction(range, 45f);
            }
        };
        return spell;
    }
}

class FeralScreamAction extends EntityAction {

    private final float range;
    private final float maxRotationalDifference;

    public FeralScreamAction(float range, float maxRotationalDifference) {
        this.range = range;
        if (maxRotationalDifference > 90f) {
            throw new InvalidParameterException("Does not support higher rotational differences than 90 degrees");
        }
        this.maxRotationalDifference = (float) Math.toRadians(maxRotationalDifference);
    }

    @Override
    public boolean update(float tpf) {

        CharacterPhysicsControl physicsControl = super.spatial.getControl(CharacterPhysicsControl.class);

        Vector3f targetLocation = physicsControl.getTargetLocation();
        final Vector3f viewDirection = targetLocation.subtract(super.spatial.getLocalTranslation()).normalizeLocal();
        super.spatial.getControl(CharacterPhysicsControl.class).setViewDirection(viewDirection);
        final Vector3f forward = viewDirection.mult(range);

        Quaternion yaw = new Quaternion();
        yaw.fromAngleAxis(this.maxRotationalDifference, Vector3f.UNIT_Y);
        final Vector3f leftNormal = yaw.mult(forward);
        leftNormal.set(-leftNormal.z, 0, leftNormal.x);
        Plane leftPlane = new Plane(leftNormal, super.spatial.getLocalTranslation().dot(leftNormal));

        yaw.fromAngleAxis(-this.maxRotationalDifference, Vector3f.UNIT_Y);
        final Vector3f rightNormal = yaw.mult(forward);
        rightNormal.set(rightNormal.z, 0, -rightNormal.x);
        Plane rightPlane = new Plane(rightNormal, super.spatial.getLocalTranslation().dot(rightNormal));

        List<SpatialDistancePair> spatialDistances = WorldManager.getSpatialsWithinDistance(super.spatial, this.range);
        for (SpatialDistancePair spatialDistancePair : spatialDistances) {
            InfluenceInterfaceControl influenceInterface = spatialDistancePair.spatial.getControl(InfluenceInterfaceControl.class);
            if (influenceInterface == null) {
                continue;
            }
            if (!this.isInCone(leftPlane, rightPlane, spatialDistancePair.spatial)) {
                continue;
            }
            final float duration = 1f;
            FearCC fear = new FearCC(-1, duration);
            fear.setInitialDirection(spatialDistancePair.spatial.getLocalTranslation().subtract(super.spatial.getLocalTranslation()));
            fear.attachToCharacter(influenceInterface);
        }
        return false;
    }

    public boolean isInCone(Plane left, Plane right, Spatial spatial) {
        final Vector3f location = spatial.getLocalTranslation();
        if (left.whichSide(location) == Plane.Side.Negative) {
            return false;
        }
        if (right.whichSide(location) == Plane.Side.Negative) {
            return false;
        }
        return true;
    }
}