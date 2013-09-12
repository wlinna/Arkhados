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

import arkhados.spells.buffs.CrowdControlBuff;
import arkhados.spells.buffs.IncapacitateCC;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author william
 */
public class InfluenceInterfaceControl extends AbstractControl {

    private List<CrowdControlBuff> crowdControlInfluences = new ArrayList<CrowdControlBuff>();
    private boolean dead = false;

    /**
     * Do damage to character (damage can be mitigated).
     *
     * @param damage
     */
    public void doDamage(float damage) {
        if (this.dead) {
            return;
        }
        Float health = super.spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        health = FastMath.clamp(health - damage, 0, health);
        super.spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        if (health == 0.0f) {
            this.death();
        }

        for (Iterator<CrowdControlBuff> it = crowdControlInfluences.iterator(); it.hasNext();) {
            CrowdControlBuff crowdControlEffect = it.next();
            if (crowdControlEffect instanceof IncapacitateCC) {
                it.remove();
            }

        }
    }

    public void addCrowdControlEffect(CrowdControlBuff crowdControlInfluence) {
        if (crowdControlInfluence != null) {
            this.crowdControlInfluences.add(crowdControlInfluence);

            if (crowdControlInfluence instanceof IncapacitateCC) {
                super.spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
            }
        }
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
    public boolean canCast() {
        for (CrowdControlBuff crowdControlInfluence : crowdControlInfluences) {
            if (crowdControlInfluence instanceof IncapacitateCC) {
                System.out.println("Can't cast. Incapacitated");
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
        for (Iterator<CrowdControlBuff> it = crowdControlInfluences.iterator(); it.hasNext();) {
            CrowdControlBuff crowdControlInfluence = it.next();
            boolean shouldContinue = crowdControlInfluence.updateDuration(tpf);
            if (!shouldContinue) {
                it.remove();
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
}
