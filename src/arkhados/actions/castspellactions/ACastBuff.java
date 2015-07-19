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
package arkhados.actions.castspellactions;

import arkhados.SpatialDistancePair;
import arkhados.actions.EntityAction;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.Selector;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class ACastBuff extends EntityAction {

    private final Spell spell;
    private final float radius;
    private final List<AbstractBuffBuilder> buffs = new ArrayList<>();

    public ACastBuff(Spell spell, float radius) {
        this.spell = spell;
        this.radius = radius;
    }
        
    @Override
    public boolean update(float tpf) {
        SpatialDistancePair targetPair = getTarget();
        
        CInfluenceInterface caster =
                spatial.getControl(CInfluenceInterface.class);
        
        // If no allies are found, cast on self
        CInfluenceInterface target = targetPair != null
                ? targetPair.spatial.getControl(CInfluenceInterface.class)
                : caster;
        
        for (AbstractBuffBuilder builder : buffs) {
            AbstractBuff buff = builder.build();
            buff.setOwnerInterface(caster);
            buff.attachToCharacter(target);
        }

        return false;
    }

    private SpatialDistancePair getTarget() {
        Vector3f targetLoc = spatial.getControl(CSpellCast.class)
                .getClosestPointToTarget(spell);

        int myTeam = spatial.getUserData(UserDataStrings.TEAM_ID);
        ArrayList<SpatialDistancePair> spatials = Selector
                .getSpatialsWithinDistance(new ArrayList<SpatialDistancePair>(),
                targetLoc, radius, new Selector.IsAlliedCharacter(myTeam));

        return Selector.giveClosest(spatials);
    }

    public void addBuff(AbstractBuffBuilder buff) {
        buffs.add(buff);
    }
}
