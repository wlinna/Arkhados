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

import arkhados.CharacterInteraction;
import arkhados.SpatialDistancePair;
import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.IncapacitateCC;
import arkhados.util.UserDataStrings;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
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
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new CastLeapAction(spell);
            }
        };
        spell.nodeBuilder = null;

        return spell;
    }
}

class CastLeapAction extends EntityAction {

    private float forwardSpeed = 105f;
    private final Spell spell;

    public CastLeapAction(final Spell spell) {
        this.spell = spell;
    }

    private void motionPathVersion() {
        final CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
        physics.switchToMotionCollisionMode();

        final MotionPath path = new MotionPath();

        // We set y to 1 to prevent ground collision on start
        final Vector3f startLocation = super.spatial.getLocalTranslation().clone().setY(1f);
        final Vector3f finalLocation = super.spatial.getControl(SpellCastControl.class).getClosestPointToTarget(this.spell);
        
        path.addWayPoint(startLocation);
        path.addWayPoint(super.spatial.getLocalTranslation().add(finalLocation.divide(2)).setY(finalLocation.length() / 3f));
        path.addWayPoint(finalLocation);

        MotionEvent motionControl = new MotionEvent(super.spatial, path);
        motionControl.setInitialDuration(finalLocation.distance(startLocation) / this.forwardSpeed);
        motionControl.setSpeed(2f);

        path.addListener(new MotionPathListener() {
            private void landingEffect() {
                final List<SpatialDistancePair> spatialsOnDistance = WorldManager.getSpatialsWithinDistance(spatial, 10f);
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
                    final InfluenceInterfaceControl thisInfluenceInterfaceControl = CastLeapAction.super.spatial.getControl(InfluenceInterfaceControl.class);
                    final InfluenceInterfaceControl targetInfluenceInterface = pairWithSmallestDistance.spatial.getControl(InfluenceInterfaceControl.class);

                    final Float damageFactor = spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
                    final float damage = 200f * damageFactor;

                    final List<AbstractBuff> buffs = new ArrayList<AbstractBuff>(1);
                    buffs.add(0, new IncapacitateCC(1f, -1));

                    CharacterInteraction.harm(thisInfluenceInterfaceControl, targetInfluenceInterface, damage, buffs, true);
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