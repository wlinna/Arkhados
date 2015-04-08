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
import arkhados.spell.buffs.PetrifyCC;
import arkhados.spell.buffs.SlowCC;
import arkhados.spell.buffs.SpeedBuff;
import arkhados.spell.influences.Influence;
import arkhados.spell.influences.SlowInfluence;
import arkhados.util.UserDataStrings;
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
public class CInfluenceInterface extends AbstractControl {

    private final List<CrowdControlBuff> crowdControlBuffs = new ArrayList<>();
    private final List<AbstractBuff> otherBuffs = new ArrayList<>();
    // This buff separation needs to be thinked about. There's lots of annoying
    // ifs around which are easy to forget     
    private final List<SlowCC> slowBuffs = new ArrayList<>();
    private final List<SpeedBuff> speedBuffs = new ArrayList<>();
    private final List<Influence> influences = new ArrayList<>();
    private final List<SlowInfluence> slowInfluences = new ArrayList<>();
    private boolean dead = false;
    private boolean canControlMovement = true;
    private boolean speedConstant = false;
    private boolean immuneToProjectiles = false;
    // HACK: Maybe this should be global?
    private boolean isServer = true;

    public float mitigateDamage(float damage) {
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

        return damage;
    }

    public void addCrowdControlBuff(CrowdControlBuff cc) {
        if (cc == null) {
            return;
        }

        if (cc instanceof SlowCC) {
            slowBuffs.add((SlowCC) cc);
        } else {
            crowdControlBuffs.add(cc);
        }

        getSpatial().getControl(CResting.class).stopRegen();

        // TODO: Check whether other buffs stop casting or not
        // TODO: Remove this ugly repetition

        if (cc.preventsMoving()) {
            spatial.getControl(CCharacterMovement.class).stop();
        }

        if (cc.preventsCasting()) {
            spatial.getControl(CSpellCast.class).setCasting(false);
            spatial.getControl(CActionQueue.class).clear();
        }
    }

    public void addOtherBuff(AbstractBuff buff) {
        if (buff == null) {
            return;
        }

        if (buff instanceof SpeedBuff) {
            speedBuffs.add((SpeedBuff) buff);
        } else {
            otherBuffs.add(buff);
        }

        if (!buff.isFriendly()) {
            getSpatial().getControl(CResting.class).stopRegen();
        }
    }

    public void setHealth(float health) {
        float healthBefore = spatial
                .getUserData(UserDataStrings.HEALTH_CURRENT);
        spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        if (healthBefore > 0f && health == 0f && !dead) {
            death();
        } else if (health < healthBefore && !isServer && health > 0f) {
            spatial.getControl(CCharacterSound.class)
                    .suffer(healthBefore - health);
        }
    }

    public boolean canMove() {
        for (CrowdControlBuff cc : crowdControlBuffs) {
            if (cc.preventsMoving()) {
                return false;
            }
        }
        return true;
    }

    public boolean canControlMovement() {
        CCharacterPhysics physics =
                spatial.getControl(CCharacterPhysics.class);
        if (!physics.getDictatedDirection().equals(Vector3f.ZERO)) {
            return false;
        }

        return canControlMovement;
    }

    public void setCanControlMovement(boolean can) {
        canControlMovement = can;
    }

    public boolean canCast() {
        for (CrowdControlBuff crowdControlBuff : crowdControlBuffs) {
            if (crowdControlBuff.preventsCasting()) {
                return false;
            }
        }
        return true;
    }

    public void death() {
        dead = true;
        spatial.getControl(CCharacterMovement.class).stop();
        spatial.getControl(CCharacterAnimation.class).death();
        spatial.getControl(CSpellCast.class).setEnabled(false);
        if (!isServer) {
            spatial.getControl(CCharacterSound.class).death();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (!Globals.worldRunning) {
            return;
        }

        /**
         * First set entity's attributes to their defaults like damagefactor and
         * movement speed.
         */
        spatial.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        immuneToProjectiles = false;

        CCharacterMovement cMovement =
                spatial.getControl(CCharacterMovement.class);
        /**
         * Some buff or action might require entity's speed to remain constant
         * until the end (for example, Venator's ChargeAction).
         */
        cMovement.setSpeedToBase();

        applySlowsAndSpeedBuffs(tpf);

        applyBuffs(tpf);
        applyInfluences(tpf);

        // Why is this here?
        if (isServer) {
            cMovement.updateMovement(tpf);
        }
    }

    private void applySlowsAndSpeedBuffs(float tpf) {
        if (!spatial.getControl(CCharacterMovement.class).isSpeedConstant()) {
            float speedFactor = 1f;

            for (Iterator<SlowCC> it = slowBuffs.iterator(); it.hasNext();) {
                SlowCC slow = it.next();
                slow.update(tpf);

                if (!slow.shouldContinue()) {
                    slow.destroy();
                    it.remove();
                    continue;
                }

                speedFactor *= slow.getSlowFactor();
            }

            float constantSpeedAddition = 0f;

            for (Iterator<SpeedBuff> it = speedBuffs.iterator();
                    it.hasNext();) {
                SpeedBuff speedBuff = it.next();
                speedBuff.update(tpf);

                if (!speedBuff.shouldContinue()) {
                    speedBuff.destroy();
                    it.remove();
                    continue;
                }

                speedFactor *= speedBuff.getFactor();
                constantSpeedAddition += speedBuff.getConstant();
            }

            for (SlowInfluence slow : slowInfluences) {
                speedFactor *= slow.getSlowFactor();
            }

            slowInfluences.clear();

            float msCurrent =
                    spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);

            spatial.setUserData(UserDataStrings.SPEED_MOVEMENT,
                    msCurrent * speedFactor + constantSpeedAddition);
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

        for (Iterator<CrowdControlBuff> it = crowdControlBuffs.iterator();
                it.hasNext();) {
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

    public void removeDamageSensitiveBuffs() {
        for (Iterator<AbstractBuff> it = otherBuffs.iterator(); it.hasNext();) {
            AbstractBuff buff = it.next();

            if (buff.isDamageSensitive()) {
                buff.destroy();
                it.remove();
            }
        }

        for (Iterator<CrowdControlBuff> it = crowdControlBuffs.iterator();
                it.hasNext();) {
            CrowdControlBuff cc = it.next();

            if (cc.isDamageSensitive()) {
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
        if (influence instanceof SlowInfluence) {
            slowInfluences.add((SlowInfluence) influence);
        } else {
            influences.add(influence);
        }
    }

    public boolean isAbleToCastWhileMoving() {
        return hasBuff(AbleToCastWhileMovingBuff.class);
    }

    public List<CrowdControlBuff> getCrowdControlBuffs() {
        return crowdControlBuffs;
    }

    public List<SpeedBuff> getSpeedBuffs() {
        return speedBuffs;
    }

    public List<SlowCC> getSlows() {
        return slowBuffs;
    }
}