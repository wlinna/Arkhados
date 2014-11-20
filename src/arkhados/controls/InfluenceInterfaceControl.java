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
package arkhados.controls;

import arkhados.Globals;
import arkhados.spell.buffs.AbleToCastWhileMovingBuff;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.spell.buffs.CrowdControlBuff;
import arkhados.spell.buffs.FearCC;
import arkhados.spell.buffs.IncapacitateCC;
import arkhados.spell.buffs.PetrifyCC;
import arkhados.spell.influences.Influence;
import arkhados.util.UserDataStrings;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author william
 */
public class InfluenceInterfaceControl extends AbstractControl {

    private final List<CrowdControlBuff> crowdControlBuffs = new ArrayList<>();
    private final List<AbstractBuff> otherBuffs = new ArrayList<>();
    private final List<Influence> influences = new ArrayList<>();
    private boolean dead = false;
    private boolean canControlMovement = true;
    private boolean speedConstant = false;
    private boolean immuneToProjectiles = false;
    // HACK: Maybe this should be global?
    private boolean isServer = true;

    /**
     * Do damage to character (damage can be mitigated).
     *
     * @param damage
     */
    public float doDamage(float damage, final boolean canBreakCC) {
        if (isDead()) {
            return 0f;
        }
        float healthBefore = spatial.getUserData(UserDataStrings.HEALTH_CURRENT);

        // TODO: Generic damage mitigation by shields, petrify etc.
        for (CrowdControlBuff cc : crowdControlBuffs) {
            if (cc instanceof PetrifyCC) {
                damage = ((PetrifyCC) cc).damage(damage);
                break;
            }
        }

        for (AbstractBuff buff : getBuffs()) {
            if (buff instanceof ArmorBuff) {
                damage = ((ArmorBuff) buff).mitigate(damage);
                break;
            }
        }

        float health = FastMath.clamp(healthBefore - damage, 0, healthBefore);
        spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);

        if (health == 0.0f) {
            death();
        }

        if (canBreakCC) {
            removeDamageSensitiveBuffs();
        }

        getSpatial().getControl(RestingControl.class).stopRegen();

        return healthBefore - health;
    }

    public float heal(float healing) {
        if (isDead()) {
            return 0f;
        }
        // TODO: Healing mitigation from negative buff
        float maxHealth = spatial.getUserData(UserDataStrings.HEALTH_MAX);
        float healthBefore = spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        Float health = FastMath.clamp(healthBefore + healing, healthBefore, maxHealth);
        spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        return health - healthBefore;

    }

    public void addCrowdControlBuff(CrowdControlBuff crowdControlInfluence) {
        if (crowdControlInfluence == null) {
            return;
        }

        crowdControlBuffs.add(crowdControlInfluence);

        getSpatial().getControl(RestingControl.class).stopRegen();

        // TODO: Check whether other buffs stop casting or not
        if (crowdControlInfluence instanceof IncapacitateCC 
                || crowdControlInfluence instanceof PetrifyCC) {
            spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
            spatial.getControl(SpellCastControl.class).setCasting(false);
            spatial.getControl(ActionQueueControl.class).clear();
        }
    }

    public void addOtherBuff(AbstractBuff buff) {
        if (buff == null) {
            return;
        }
        otherBuffs.add(buff);

        if (!buff.isFriendly()) {
            getSpatial().getControl(RestingControl.class).stopRegen();
        }
    }

    public void setHealth(float health) {
        float healthBefore = spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        if (health == 0f && !dead) {
            death();
        } else if (health < healthBefore && !isServer && health > 0f) {
            spatial.getControl(CharacterSoundControl.class).suffer(healthBefore - health);
        }
    }

    public boolean canMove() {
        for (CrowdControlBuff crowdControlInfluence : crowdControlBuffs) {
            if (crowdControlInfluence instanceof IncapacitateCC) {
                return false;
            } else if (crowdControlInfluence instanceof PetrifyCC) {
                return false;
            }
        }
        return true;
    }

    public boolean canControlMovement() {
        CharacterPhysicsControl physics = spatial.getControl(CharacterPhysicsControl.class);
        if (!physics.getDictatedDirection().equals(Vector3f.ZERO)) {
            return false;
        }

        return canControlMovement;
    }

    public void setCanControlMovement(boolean can) {
        canControlMovement = can;
    }

    public boolean canCast() {
        for (CrowdControlBuff crowdControlInfluence : crowdControlBuffs) {
            // Consider letting buffs to set property and just returning that property
            if (crowdControlInfluence instanceof IncapacitateCC) {
                return false;
            } else if (crowdControlInfluence instanceof FearCC) {
                return false;
            } else if (crowdControlInfluence instanceof PetrifyCC) {
                return false;
            }
        }
        return true;
    }

    public void death() {
        dead = true;
        spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
        spatial.getControl(CharacterAnimationControl.class).death();
        spatial.getControl(SpellCastControl.class).setEnabled(false);
        if (!isServer) {
            spatial.getControl(CharacterSoundControl.class).death();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (!Globals.worldRunning) {
            return;
        }

        // TODO: Refactor InfluenceInterfaceControl's controlUpdate. It is very hard to understand.
        /**
         * First set entity's attributes to their defaults like damagefactor and movement speed.
         */
        spatial.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        immuneToProjectiles = false;

        /**
         * Some buff or action might require entity's speed to remain constant until the end (for
         * example, Venator's ChargeAction).
         */
        if (!isSpeedConstant()) {
            float msBase = spatial.getUserData(UserDataStrings.SPEED_MOVEMENT_BASE);
            spatial.setUserData(UserDataStrings.SPEED_MOVEMENT, msBase);
        }

        applyBuffs(tpf);
        applyInfluences(tpf);

        SpellCastControl castControl = spatial.getControl(SpellCastControl.class);

        /**
         * This code here applies changes to movement if player can move.
         */
        if (canMove() && !castControl.isCasting() && !castControl.isChanneling() && isServer) {

            CharacterPhysicsControl physics = spatial.getControl(CharacterPhysicsControl.class);
            if (canControlMovement()) {
                spatial.getControl(UserInputControl.class).restoreWalking();

            } else {
                if (!isSpeedConstant() && physics.getDictatedDirection().equals(Vector3f.ZERO)) {
                    float msCurrent = spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
                    Vector3f walkDir = physics.getWalkDirection();
                    Vector3f newWalkDir = walkDir.normalizeLocal().multLocal(msCurrent);
                    physics.setWalkDirection(newWalkDir);
                }
            }
        }
    }

    private void applyBuffs(float tpf) {
        for (Iterator<AbstractBuff> it = otherBuffs.iterator(); it.hasNext();) {
            AbstractBuff buff = it.next();
            buff.update(tpf);
            if (!buff.shouldContinue()) {
                buff.destroy();
                it.remove();
                continue;
            }
        }

        for (Iterator<CrowdControlBuff> it = crowdControlBuffs.iterator(); it.hasNext();) {
            CrowdControlBuff cc = it.next();
            cc.update(tpf);
            if (!cc.shouldContinue()) {
                cc.destroy();
                it.remove();
                continue;
            }
        }
    }

    private void applyInfluences(float tpf) {
        for (Iterator<Influence> it = influences.iterator(); it.hasNext();) {
            Influence influence = it.next();
            influence.affect(this, tpf);
            it.remove();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public boolean isDead() {
        return dead;
    }

    public void setIsServer(boolean flag) {
        isServer = flag;
    }

    private void removeDamageSensitiveBuffs() {

        for (Iterator<CrowdControlBuff> it = crowdControlBuffs.iterator(); it.hasNext();) {
            CrowdControlBuff cc = it.next();
            boolean remove = false;
            // TODO: Use some kind of flag instead of detecting type
            if (cc instanceof IncapacitateCC) {
                remove = true;
            } else if (cc instanceof FearCC) {
                remove = true;
            }
            if (remove) {
                cc.destroy();
                it.remove();
            }
        }
    }

    public void setSpeedConstant(boolean constantSpeed) {
        speedConstant = constantSpeed;
    }

    public boolean isSpeedConstant() {
        return speedConstant;
    }

    public boolean isImmuneToProjectiles() {
        return immuneToProjectiles;
    }

    public void setImmuneToProjectiles(boolean immuneToProjectiles) {
        this.immuneToProjectiles = immuneToProjectiles;
    }

    public List<AbstractBuff> getBuffs() {
        return otherBuffs;
    }

    private <T extends AbstractBuff> boolean hasBuff(Class<T> buffClass) {
        if (buffClass.isAssignableFrom(CrowdControlBuff.class)) {
            for (AbstractBuff buff : crowdControlBuffs) {
                if (buffClass.isAssignableFrom(buff.getClass())) {
                    return true;
                }
            }
        } else {
            for (AbstractBuff buff : getBuffs()) {
                if (buffClass.isAssignableFrom(buff.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addInfluence(Influence influence) {
        influences.add(influence);
    }

    public boolean isAbleToCastWhileMoving() {
        return hasBuff(AbleToCastWhileMovingBuff.class);
    }

    public List<CrowdControlBuff> getCrowdControlBuffs() {
        return crowdControlBuffs;
    }
}