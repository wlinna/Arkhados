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

import arkhados.spell.buffs.AbleToCastWhileMovingBuff;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.spell.buffs.CrowdControlBuff;
import arkhados.spell.buffs.FearCC;
import arkhados.spell.buffs.IncapacitateCC;
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

    private List<CrowdControlBuff> crowdControlInfluences = new ArrayList<>();
    private List<AbstractBuff> otherBuffs = new ArrayList<>();
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
        if (this.dead) {
            return 0f;
        }
        Float healthBefore = super.spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        // TODO: Generic damage mitigation by shields
        for (AbstractBuff buff : this.getBuffs()) {
            if (buff instanceof ArmorBuff) {
                damage = ((ArmorBuff) buff).mitigate(damage);
                break;
            }
        }
        float health = FastMath.clamp(healthBefore - damage, 0, healthBefore);
        super.spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        if (health == 0.0f) {
            this.death();
        }

        if (canBreakCC) {
            this.removeDamageSensitiveBuffs();
        }
        return healthBefore - health;
    }

    public float heal(float healing) {
        if (this.dead) {
            return 0f;
        }
        // TODO: Healing mitigation from negative buff
        final Float maxHealth = super.spatial.getUserData(UserDataStrings.HEALTH_MAX);
        final Float healthBefore = super.spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        final float health = FastMath.clamp(healthBefore + healing, healthBefore, maxHealth);
        super.spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        return health - healthBefore;

    }

    public void addCrowdControlBuff(CrowdControlBuff crowdControlInfluence) {
        if (crowdControlInfluence == null) {
            return;
        }
        this.crowdControlInfluences.add(crowdControlInfluence);

        // TODO: Check whether other buffs stop casting or not
        if (crowdControlInfluence instanceof IncapacitateCC) {
            super.spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
            super.spatial.getControl(SpellCastControl.class).setCasting(false);
            super.spatial.getControl(ActionQueueControl.class).clear();
        }
    }

    public void addOtherBuff(AbstractBuff buff) {
        if (buff == null) {
            return;
        }
        this.otherBuffs.add(buff);
    }

    public void setHealth(float health) {
        super.spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        if (this.dead) {
            return;
        } else if (health == 0.0) {
            this.death();
        }
    }

    public boolean canMove() {
        for (CrowdControlBuff crowdControlInfluence : crowdControlInfluences) {
            if (crowdControlInfluence instanceof IncapacitateCC) {
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
        
        return this.canControlMovement;
    }

    public void setCanControlMovement(boolean can) {
        this.canControlMovement = can;
    }

    public boolean canCast() {
        for (CrowdControlBuff crowdControlInfluence : crowdControlInfluences) {
            // Consider letting buffs to set property and just returning that property
            if (crowdControlInfluence instanceof IncapacitateCC) {
                return false;
            } else if (crowdControlInfluence instanceof FearCC) {
                return false;
            }
        }
        return true;
    }

    public void death() {
        this.dead = true;
        super.spatial.getControl(CharacterAnimationControl.class).death();
        super.spatial.getControl(SpellCastControl.class).setEnabled(false);
    }

    @Override
    protected void controlUpdate(float tpf) {
        // TODO: Refactor InfluenceInterfaceControl's controlUpdate. It is very hard to understand.
        /**
         * First set entity's attributes to their defaults like damagefactor and
         * movement speed.
         */
        super.spatial.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        this.immuneToProjectiles = false;

        /**
         * Some buff or action might require entity's speed to remain constant
         * until the end (for example, Venator's ChargeAction).
         */
        if (!this.isSpeedConstant()) {
            Float msBase = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT_BASE);
            super.spatial.setUserData(UserDataStrings.SPEED_MOVEMENT, msBase);
        }

        this.applyBuffs(tpf);

        final SpellCastControl castControl = super.spatial.getControl(SpellCastControl.class);

        /**
         * This code here applies changes to movement if player can move.
         */
        if (this.canMove() && !castControl.isCasting()
                && !castControl.isChanneling() && this.isServer) {

            final CharacterPhysicsControl physics =
                    super.spatial.getControl(CharacterPhysicsControl.class);

            if (this.canControlMovement()) {
                super.spatial.getControl(UserInputControl.class).restoreWalking();

            } else {
                if (!this.isSpeedConstant() && physics.getDictatedDirection().equals(Vector3f.ZERO)) {
                    Float msCurrent = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
                    Vector3f walkDir = physics.getWalkDirection();
                    Vector3f newWalkDir = walkDir.normalizeLocal().multLocal(msCurrent);
                    physics.setWalkDirection(newWalkDir);
                }
            }
        }
    }

    private void applyBuffs(float tpf) {
        for (Iterator<AbstractBuff> it = this.otherBuffs.iterator(); it.hasNext();) {
            AbstractBuff buff = it.next();
            buff.update(tpf);
            if (!buff.shouldContinue()) {
                buff.destroy();
                it.remove();
                continue;
            }
        }

        for (Iterator<CrowdControlBuff> it = crowdControlInfluences.iterator(); it.hasNext();) {
            CrowdControlBuff cc = it.next();
            cc.update(tpf);
            if (!cc.shouldContinue()) {
                cc.destroy();
                it.remove();
                continue;
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public boolean isDead() {
        return dead;
    }

    public void setIsServer(boolean flag) {
        this.isServer = flag;
    }

    private void removeDamageSensitiveBuffs() {

        for (Iterator<CrowdControlBuff> it = crowdControlInfluences.iterator(); it.hasNext();) {
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
        this.speedConstant = constantSpeed;
    }

    public boolean isSpeedConstant() {
        return this.speedConstant;
    }

    public boolean isImmuneToProjectiles() {
        return this.immuneToProjectiles;
    }

    public void setImmuneToProjectiles(boolean immuneToProjectiles) {
        this.immuneToProjectiles = immuneToProjectiles;
    }

    public List<AbstractBuff> getBuffs() {
        return otherBuffs;
    }

    private <T extends AbstractBuff> boolean hasBuff(Class<T> buffClass) {
        if (buffClass.isAssignableFrom(CrowdControlBuff.class)) {
            for (AbstractBuff buff : this.crowdControlInfluences) {
                if (buffClass.isAssignableFrom(buff.getClass())) {
                    return true;
                }
            }
        } else {
            for (AbstractBuff buff : this.getBuffs()) {
                if (buffClass.isAssignableFrom(buff.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAbleToCastWhileMoving() {
        return this.hasBuff(AbleToCastWhileMovingBuff.class);
    }
}