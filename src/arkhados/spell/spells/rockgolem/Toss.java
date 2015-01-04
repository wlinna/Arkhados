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

import arkhados.CharacterInteraction;
import arkhados.ServerFogManager;
import arkhados.SpatialDistancePair;
import arkhados.actions.EntityAction;
import arkhados.actions.SplashAction;
import arkhados.characters.RockGolem;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.EntityVariableControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.messages.WorldEffectCommand;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.util.DistanceScaling;
import arkhados.util.Predicate;
import arkhados.util.Selector;
import arkhados.util.UserDataStrings;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class Toss extends Spell {

    {
        iconName = "Toss.png";
    }

    public Toss(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Toss create() {
        float cooldown = 8f;
        float range = 80f;
        float castTime = 0.3f;

        final Toss toss = new Toss("Toss", cooldown, range, castTime);

        toss.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new CastTossAction(toss, 20f);
            }
        };

        toss.nodeBuilder = null;
        return toss;
    }
}

class CastTossAction extends EntityAction {

    private Spell spell;
    private float range;

    public CastTossAction(Spell spell, float throwRange) {
        this.spell = spell;
        this.range = throwRange;
    }

    @Override
    public boolean update(float tpf) {
        ActionQueueControl actionQueue = spatial.getControl(ActionQueueControl.class);

        TossAction tossAction = new TossAction(spell, range);
        actionQueue.enqueueAction(tossAction);
        return false;
    }
}

class TossAction extends EntityAction {

    private Spell spell;
    private float range;
    private final float forwardSpeed = 105f;

    public TossAction(Spell spell, float range) {
        this.spell = spell;
        this.range = range;
    }

    @Override
    public boolean update(float tpf) {
        final CharacterPhysicsControl physicsControl =
                spatial.getControl(CharacterPhysicsControl.class);
        Vector3f hitDirection = physicsControl.calculateTargetDirection()
                .normalize().multLocal(range);

        physicsControl.setViewDirection(hitDirection);

        Predicate<SpatialDistancePair> predicate =
                new Predicate<SpatialDistancePair>() {
            @Override
            public boolean test(SpatialDistancePair value) {
                InfluenceInterfaceControl targetInfluenceControl = value.spatial
                        .getControl(InfluenceInterfaceControl.class);
                if (targetInfluenceControl == null) {
                    SpiritStonePhysicsControl stone =
                            value.spatial
                            .getControl(SpiritStonePhysicsControl.class);
                    if (stone != null) {
                        int myTeamId = spatial
                                .getUserData(UserDataStrings.TEAM_ID);
                        if (value.spatial.getUserData(UserDataStrings.TEAM_ID)
                                .equals(myTeamId)) {
                            return true;
                        }
                    }
                    return false;
                }

                if (value.spatial == spatial) {
                    return false;
                }

                return true;
            }
        };

        List<SpatialDistancePair> targets = Selector.coneSelect(
                new ArrayList<SpatialDistancePair>(), predicate,
                spatial.getLocalTranslation(), hitDirection, range, 90f);

        Spatial closest = null;
        float smallestDistance = 9999f;

        for (SpatialDistancePair target : targets) {
            if (target.distance < smallestDistance) {
                smallestDistance = target.distance;
                closest = target.spatial;
            }
        }

        if (closest != null) {
            toss(closest);
        }

        return false;
    }

    private void toss(final Spatial target) {
        Vector3f startLocation = spatial.getLocalTranslation().clone().setY(1);
        Vector3f finalLocation = spatial.getControl(SpellCastControl.class)
                .getClosestPointToTarget(spell);

        final MotionPath path = new MotionPath();
        path.addWayPoint(startLocation);
        path.addWayPoint(spatial.getLocalTranslation().add(finalLocation)
                .divideLocal(2)
                .setY(finalLocation.distance(startLocation) / 2f));
        path.addWayPoint(finalLocation);

        path.setPathSplineType(Spline.SplineType.CatmullRom);
        path.setCurveTension(0.75f);

        MotionEvent motionControl = new MotionEvent(target, path);
        motionControl.setInitialDuration(finalLocation
                .distance(startLocation) / forwardSpeed);
        motionControl.setSpeed(1.6f);

        final SpiritStonePhysicsControl stonePhysics =
                target.getControl(SpiritStonePhysicsControl.class);

        MotionPathListener motionPathListener = new MotionPathListener() {
            @Override
            public void onWayPointReach(MotionEvent motionControl,
                    int wayPointIndex) {
                if (wayPointIndex == path.getNbWayPoints() - 1) {
                    if (stonePhysics == null) {
                        target.getControl(CharacterPhysicsControl.class)
                                .switchToNormalPhysicsMode();
                    }
                    landingEffect();
                    motionControl.setEnabled(false);
                }
            }

            private void landingEffect() {
                SplashAction splashAction = new SplashAction(20, 250, 0,
                        DistanceScaling.CONSTANT, null);
                splashAction.setSpatial(target);
                splashAction.excludeSpatial(spatial);
                splashAction.excludeSpatial(target);
                splashAction.setCasterInterface(spatial
                        .getControl(InfluenceInterfaceControl.class));
                splashAction.update(0f);
                if (stonePhysics == null) {
                    InfluenceInterfaceControl targetInterface =
                            target.getControl(InfluenceInterfaceControl.class);
                    InfluenceInterfaceControl myInterface =
                            spatial.getControl(InfluenceInterfaceControl.class);

                    CharacterInteraction.harm(myInterface, targetInterface,
                            150f, null, true);
                } else {
                    target.getLocalTranslation().setY(10f);
                }

                ServerFogManager fogManager = spatial
                        .getControl(EntityVariableControl.class).getAwareness()
                        .getFogManager();

                fogManager.addCommand(target,
                        new WorldEffectCommand(RockGolem.WORLDEFFECT_EARTHQUAKE,
                        target.getLocalTranslation()));
            }
        };

        path.addListener(motionPathListener);

        motionControl.play();

        if (stonePhysics == null) {
            target.getControl(CharacterPhysicsControl.class)
                    .switchToMotionCollisionMode();
        }
    }
}