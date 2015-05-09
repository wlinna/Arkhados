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
import arkhados.actions.EntityAction;
import arkhados.characters.Venator;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.FearCC;
import arkhados.util.Selector;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class FeralScream extends Spell {

    {
        iconName = "feral_scream.png";
    }

    public FeralScream(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static FeralScream create() {
        final float cooldown = 8f;
        final float range = 45f;
        final float castTime = 0.3f;

        FeralScream spell = new FeralScream("Feral Scream", cooldown,
                range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new AFeralScream(range, 45f);
            }
        };
        return spell;
    }
}

class AFeralScream extends EntityAction {

    private final float range;
    private final float maxRotationalDifference;

    public AFeralScream(float range, float maxRotationalDifference) {
        this.range = range;
        if (maxRotationalDifference > 90f) {
            throw new InvalidParameterException("Does not support "
                    + "higher rotational differences than 90 degrees");
        }

        this.maxRotationalDifference =
                (float) Math.toRadians(maxRotationalDifference);
        setTypeId(Venator.ACTION_FERALSCREAM);
    }

    @Override
    public boolean update(float tpf) {
        // TODO: Replace with Selector.coneSelect
        CCharacterPhysics physicsControl =
                spatial.getControl(CCharacterPhysics.class);

        Vector3f targetLocation = physicsControl.getTargetLocation();
        final Vector3f viewDirection = targetLocation
                .subtract(spatial.getLocalTranslation()).normalizeLocal();
        spatial.getControl(CCharacterPhysics.class)
                .setViewDirection(viewDirection);
        final Vector3f forward = viewDirection.mult(range);

        Quaternion yaw = new Quaternion();
        yaw.fromAngleAxis(maxRotationalDifference, Vector3f.UNIT_Y);
        final Vector3f leftNormal = yaw.mult(forward);
        leftNormal.set(-leftNormal.z, 0, leftNormal.x);
        Plane leftPlane = new Plane(leftNormal,
                spatial.getLocalTranslation().dot(leftNormal));

        yaw.fromAngleAxis(-maxRotationalDifference, Vector3f.UNIT_Y);
        final Vector3f rightNormal = yaw.mult(forward);
        rightNormal.set(rightNormal.z, 0, -rightNormal.x);
        Plane rightPlane = new Plane(rightNormal,
                spatial.getLocalTranslation().dot(rightNormal));

        List<SpatialDistancePair> spatialDistances = Selector
                .getSpatialsWithinDistance(new ArrayList<SpatialDistancePair>(),
                spatial, range);
        for (SpatialDistancePair spatialDistancePair : spatialDistances) {
            CInfluenceInterface influenceInterface =
                    spatialDistancePair.spatial
                    .getControl(CInfluenceInterface.class);

            if (influenceInterface == null) {
                continue;
            }
            if (spatialDistancePair.spatial == spatial) {
                continue;
            }

            if (!Selector.isInCone(leftPlane, rightPlane,
                    spatialDistancePair.spatial)) {
                continue;
            }
            final float duration = 2f;
            FearCC fear = new FearCC(-1, duration);

            final Vector3f initialDirection = spatialDistancePair.spatial
                    .getLocalTranslation()
                    .subtract(spatial.getLocalTranslation()).setY(0f);

            fear.setInitialDirection(initialDirection);
            fear.attachToCharacter(influenceInterface);
        }
        return false;
    }
}