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
import arkhados.spell.buffs.AbsorbingShieldBuff;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.spell.buffs.BlindCC;
import arkhados.spell.buffs.CrowdControlBuff;
import arkhados.spell.buffs.CastSpeedBuff;
import arkhados.spell.buffs.DamageBuff;
import arkhados.spell.buffs.LifeStealBuff;
import arkhados.spell.buffs.PetrifyCC;
import arkhados.spell.buffs.SlowCC;
import arkhados.spell.buffs.SpeedBuff;
import arkhados.spell.influences.Influence;
import arkhados.spell.influences.SlowInfluence;
import arkhados.spell.influences.SpeedInfluence;
import arkhados.util.UserData;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CInfluenceInterface extends AbstractControl {

    private final List<AbstractBuff> buffs = new ArrayList<>();
    private final List<BlindCC> blinds = new ArrayList<>();
    private final List<CastSpeedBuff> numbs = new ArrayList<>();
    private final List<Influence> influences = new ArrayList<>();
    private final List<SlowInfluence> slowInfluences = new ArrayList<>();
    private final List<SpeedInfluence> speedInfluences = new ArrayList<>();
    private boolean dead = false;
    private boolean canControlMovement = true;
    private boolean speedConstant = false;
    private boolean immuneToProjectiles = false;
    // HACK: Maybe this should be global?
    private boolean isServer = true;

    public float mitigateDamage(float damage) {
        // TODO: Generic damage mitigation by shields, petrify etc.
        for (AbstractBuff buff : buffs) {
            if (buff instanceof ArmorBuff) {
                damage = ((ArmorBuff) buff).mitigate(damage);
                break;
            } else if (buff instanceof PetrifyCC) {
                damage = ((PetrifyCC) buff).damage(damage);
                break;
            }
        }

        return damage;
    }

    public void addBuff(AbstractBuff buff) {
        if (buff == null) {
            return;
        }

        buffs.add(buff);

        if (!buff.isFriendly()) {
            getSpatial().getControl(CResting.class).stopRegen();
        }

        if (!(buff instanceof CrowdControlBuff)) {
            return;
        }

        CrowdControlBuff cc = (CrowdControlBuff) buff;

        if (cc.preventsMoving()) {
            spatial.getControl(CCharacterMovement.class).stop();
        }

        if (cc.preventsCasting()) {
            spatial.getControl(CSpellCast.class).setCasting(false);
            spatial.getControl(CActionQueue.class).clear();
        }
    }

    public void setHealth(float health) {
        float healthBefore = spatial
                .getUserData(UserData.HEALTH_CURRENT);
        spatial.setUserData(UserData.HEALTH_CURRENT, health);
        if (healthBefore > 0f && health == 0f && !dead) {
            death();
        } else if (health < healthBefore && !isServer && health > 0f) {
            spatial.getControl(CCharacterSound.class)
                    .suffer(healthBefore - health);
        }
    }

    public boolean canMove() {
        for (AbstractBuff buff : buffs) {
            if (buff instanceof CrowdControlBuff) {
                if (((CrowdControlBuff) buff).preventsMoving()) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean canControlMovement() {
        CCharacterPhysics physics
                = spatial.getControl(CCharacterPhysics.class);
        if (!physics.getDictatedDirection().equals(Vector3f.ZERO)) {
            return false;
        }

        return canControlMovement;
    }

    public void setCanControlMovement(boolean can) {
        canControlMovement = can;
    }

    public boolean canCast() {
        if (spatial.getControl(CMovementForcer.class) != null) {
            return false;
        }

        for (AbstractBuff buff : buffs) {
            if (buff instanceof CrowdControlBuff) {
                if (((CrowdControlBuff) buff).preventsCasting()) {
                    return false;
                }
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
        spatial.setUserData(UserData.DAMAGE_FACTOR, 1f);
        float lifeStealBase
                = spatial.getUserData(UserData.LIFE_STEAL_BASE);
        spatial.setUserData(UserData.LIFE_STEAL, lifeStealBase);
        immuneToProjectiles = false;

        CCharacterMovement cMovement
                = spatial.getControl(CCharacterMovement.class);
        /**
         * Some buff or action might require entity's speed to remain constant
         * until the end (for example, Venator's ChargeAction).
         */
        cMovement.setSpeedToBase();

        applyBuffs(tpf);
        applySlowsAndSpeedBuffs();
        applyDamageBuffs();
        applyLifeStealBuffs();
        applyInfluences(tpf);

        // Why is this here?
        if (isServer) {
            cMovement.updateMovement(tpf);
        }
    }

    private void applyDamageBuffs() {
        float damageFactor = 1f;
        for (AbstractBuff buff : buffs) {
            if (buff instanceof DamageBuff) {
                damageFactor *= ((DamageBuff) buff).getFactor();
            }
        }

        spatial.setUserData(UserData.DAMAGE_FACTOR, damageFactor);
    }

    private void applyLifeStealBuffs() {
        float lifeSteal = spatial.getUserData(UserData.LIFE_STEAL_BASE);
        for (AbstractBuff buff : buffs) {
            if (buff instanceof LifeStealBuff) {
                LifeStealBuff lifeStealBuff = (LifeStealBuff) buff;
                lifeSteal += lifeStealBuff.getAmount();
            }
        }

        spatial.setUserData(UserData.LIFE_STEAL, lifeSteal);
    }

    private void applySlowsAndSpeedBuffs() {
        if (!spatial.getControl(CCharacterMovement.class).isSpeedConstant()) {
            float speedFactor = 1f;
            float constantSpeedAddition = 0f;

            for (AbstractBuff buff : buffs) {
                if (buff instanceof SlowCC) {
                    speedFactor *= ((SlowCC) buff).getSlowFactor();
                } else if (buff instanceof SpeedBuff) {
                    SpeedBuff speedBuff = (SpeedBuff) buff;
                    speedFactor *= speedBuff.getFactor();
                    constantSpeedAddition += speedBuff.getConstant();
                }
            }

            for (SlowInfluence slow : slowInfluences) {
                speedFactor *= slow.getSlowFactor();
            }

            slowInfluences.clear();

            for (SpeedInfluence speed : speedInfluences) {
                speedFactor *= speed.getSpeedFactor();
                constantSpeedAddition += speed.getConstant();
            }

            speedInfluences.clear();

            float msCurrent
                    = spatial.getUserData(UserData.SPEED_MOVEMENT);

            spatial.setUserData(UserData.SPEED_MOVEMENT,
                    msCurrent * speedFactor + constantSpeedAddition);
        }
    }

    private void applyBuffs(float tpf) {
        for (Iterator<AbstractBuff> it = buffs.iterator(); it.hasNext();) {
            AbstractBuff buff = it.next();
            buff.update(tpf);
            if (!buff.shouldContinue()) {
                buff.destroy();
                it.remove();
            }
        }
    }

    private void applyInfluences(float tpf) {
        for (Influence influence : influences) {
            influence.affect(this, tpf);
        }

        influences.clear();
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
        for (Iterator<AbstractBuff> it = buffs.iterator(); it.hasNext();) {
            AbstractBuff buff = it.next();
            if (buff.isDamageSensitive()) {
                buff.destroy();
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
        return buffs;
    }

    private <T extends AbstractBuff> boolean hasBuff(Class<T> buffClass) {
        for (AbstractBuff buff : buffs) {
            if (buffClass.isAssignableFrom(buff.getClass())) {
                return true;
            }
        }

        return false;
    }

    public void addInfluence(Influence influence) {
        influences.add(influence);

        if (influence instanceof SlowInfluence) {
            slowInfluences.add((SlowInfluence) influence);
        } else if (influence instanceof SpeedInfluence) {
            speedInfluences.add((SpeedInfluence) influence);
        }
    }

    public void reducePurifyingFlame(float dmg) {
        for (AbstractBuff abstractBuff : buffs) {
            if (abstractBuff instanceof AbsorbingShieldBuff) {
                ((AbsorbingShieldBuff) abstractBuff).reduce(dmg);
                return;
            }
        }
    }

    public boolean isAbleToCastWhileMoving() {
        return hasBuff(AbleToCastWhileMovingBuff.class);
    }

    public List<BlindCC> getBlinds() {
        return blinds;
    }

    public boolean isBlind() {
        return !blinds.isEmpty();
    }

    public List<CastSpeedBuff> getCastSpeedBuffs() {
        return numbs;
    }
}
