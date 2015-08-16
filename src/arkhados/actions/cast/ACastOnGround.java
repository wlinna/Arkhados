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
package arkhados.actions.cast;

import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.controls.CAreaEffect;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.UserData;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

public class ACastOnGround extends EntityAction {

    private World world;
    private Spell spell;
    private final List<AbstractBuffBuilder> additionalEnterBuffs
            = new ArrayList<>();
    // NOTE: Add additionalExitBuffs -list if needed

    public ACastOnGround(World world, Spell spell) {
        this.world = world;
        this.spell = spell;
    }

    @Override
    public boolean update(float tpf) {
        CSpellCast castControl = 
                spatial.getControl(CSpellCast.class);
        Vector3f adjustedTarget =
                castControl.getClosestPointToTarget(spell).setY(0.1f);
        Integer playerId = spatial.getUserData(UserData.PLAYER_ID);
        int entityId = world.addNewEntity(spell.getId(),
                adjustedTarget, Quaternion.IDENTITY, playerId);

        Spatial entity = world.getEntity(entityId);
        CAreaEffect aoeControl =
                entity.getControl(CAreaEffect.class);
        aoeControl.setOwnerInterface(
                spatial.getControl(CInfluenceInterface.class));
        for (AbstractBuffBuilder buff : additionalEnterBuffs) {
            aoeControl.addEnterBuff(buff);
        }
        return false;
    }

    public void addEnterBuff(AbstractBuffBuilder buff) {
        if (buff == null) {
            throw new IllegalArgumentException(
                    "Nulls not allowed in containers");
        }
        additionalEnterBuffs.add(buff);
    }
}
