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

import arkhados.actions.ChargeAction;
import arkhados.actions.EntityAction;
import arkhados.actions.SplashAction;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.IncapacitateCC;
import arkhados.util.DistanceScaling;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;

/**
 *
 * @author william
 */
public class EarthQuake extends Spell {

    public EarthQuake(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 9f;
        final float range = 90f;
        final float castTime = 0.3f;

        EarthQuake quake = new EarthQuake("EarthQuake", cooldown, range, castTime);
        quake.nodeBuilder = null;

        quake.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new CastEarthQuakeAction();
            }
        };

        return quake;
    }
}

class CastEarthQuakeAction extends EntityAction {
    private final float chargeRange = 90f;

    public CastEarthQuakeAction() {
    }

    @Override
    public boolean update(float tpf) {
        ChargeAction charge = new ChargeAction(chargeRange);
        charge.setChargeSpeed(150f);
        charge.setHitDamage(100f);
        
        IncapacitateCC incapacitate = new IncapacitateCC(1.5f, -1);
        ArrayList<AbstractBuff> buffs = new ArrayList<>();
        buffs.add(incapacitate);
        
        SplashAction splash = new SplashAction(20f, 100f, 0f, DistanceScaling.CONSTANT, buffs);
        InfluenceInterfaceControl casterInterface =
                spatial.getControl(InfluenceInterfaceControl.class);        
        splash.setCasterInterface(casterInterface);                
        
        
        ActionQueueControl queue = spatial.getControl(ActionQueueControl.class);
        queue.enqueueAction(charge);
        queue.enqueueAction(splash);
        
        return false;
    }
}
