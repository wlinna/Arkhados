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

import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.CrowdControlBuff;
import arkhados.spell.buffs.FearCC;
import arkhados.spell.buffs.IncapacitateCC;
import arkhados.util.UserDataStrings;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author william
 */
public class InfluenceInterfaceControl extends AbstractControl {

    private List<CrowdControlBuff> crowdControlInfluences = new ArrayList<CrowdControlBuff>();
    private List<AbstractBuff> otherBuffs = new ArrayList<AbstractBuff>();
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
    public float doDamage(final float damage, final boolean canBreakCC) {
        if (this.dead) {
            return 0f;
        }
        Float healthBefore = super.spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        // TODO: Damage mitigation by shields
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

        if (crowdControlInfluence instanceof IncapacitateCC) {
            super.spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
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
        super.spatial.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        this.immuneToProjectiles = false;

        if (!this.isSpeedConstant()) {
            Float msBase = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT_BASE);
            super.spatial.setUserData(UserDataStrings.SPEED_MOVEMENT, msBase);
        }
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

        if (this.canMove()) {
            if (this.isServer) {
                CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
                if (this.canControlMovement()) {
                    physics.restoreWalking();
                } else {
                    if (!this.isSpeedConstant()) {
                        Float msCurrent = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
                        Vector3f walkDir = physics.getWalkDirection();
                        Vector3f newWalkDir = walkDir.normalizeLocal().multLocal(msCurrent);
                        physics.setWalkDirection(newWalkDir);
                    }
                }
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        InfluenceInterfaceControl control = new InfluenceInterfaceControl();
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);

    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
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
            // TODO: Use some kind of flag instance of detecting type
            if (cc instanceof IncapacitateCC) {
                it.remove();
            } else if (cc instanceof FearCC) {
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
}