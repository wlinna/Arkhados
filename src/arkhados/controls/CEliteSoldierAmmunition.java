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

import arkhados.characters.EliteSoldierSyncData;
import arkhados.spell.Spell;
import arkhados.spell.SpellCastListener;
import arkhados.spell.SpellCastValidator;
import arkhados.spell.spells.elitesoldier.AmmunitionSlot;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the amount of EliteSoldiers ammo. Replenishes Elite Soldiers
 * ammunition and checks that Elite Soldier has enough ammo to cast spell
 *
 * @author william
 */
public class CEliteSoldierAmmunition extends AbstractControl
        implements SpellCastValidator, SpellCastListener {

    private final List<AmmunitionLoader> ammunitionLoaders = new ArrayList<>(4);

    {
        ammunitionLoaders.add(AmmunitionLoader.Shotgun());
        ammunitionLoaders.add(AmmunitionLoader.Plasmagun());
        ammunitionLoaders.add(AmmunitionLoader.Rocket());
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (spatial.getControl(CEntityVariable.class).getWorldManager()
                .isClient()) {
            return;
        }

        for (AmmunitionLoader ammunitionLoader : ammunitionLoaders) {
            ammunitionLoader.update(tpf);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public boolean validateSpellCast(CSpellCast castControl, Spell spell) {
        if ("Shotgun".equals(spell.getName())) {
            return ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot())
                    .hasEnough(8);
        } else if ("Plasmagun".equals(spell.getName())
                || "Plasma Grenades".equals(spell.getName())) {
            return ammunitionLoaders.get(AmmunitionSlot.PLASMAGUN.slot())
                    .hasEnough(3);
        } else if ("Rocket Launcher".equals(spell.getName())
                || "Rocket Jump".equals(spell.getName())) {
            return ammunitionLoaders.get(AmmunitionSlot.ROCKETS.slot())
                    .hasEnough(1);
        }

        return true;
    }

    @Override
    public void spellCasted(CSpellCast castControl, Spell spell) {
        if ("Shotgun".equals(spell.getName())) {
            ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot())
                    .consumeAmmo(8);
        } else if ("Plasmagun".equals(spell.getName())
                || "Plasma Grenades".equals(spell.getName())) {
            ammunitionLoaders.get(AmmunitionSlot.PLASMAGUN.slot())
                    .consumeAmmo(3);
        } else if ("Rocket Launcher".equals(spell.getName())
                || "Rocket Jump".equals(spell.getName())) {
            ammunitionLoaders.get(AmmunitionSlot.ROCKETS.slot()).consumeAmmo(1);
        }
    }

    public EliteSoldierSyncData addAmmoSynchronizationData(
            EliteSoldierSyncData syncData, float towardsFuture) {
        syncData.setPellets(ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot())
                .predictAmmoAmount(towardsFuture));
        syncData.setPlasmas(ammunitionLoaders
                .get(AmmunitionSlot.PLASMAGUN.slot())
                .predictAmmoAmount(towardsFuture));
        syncData.setRockets(ammunitionLoaders
                .get(AmmunitionSlot.ROCKETS.slot())
                .predictAmmoAmount(towardsFuture));
        return syncData;
    }

    public void synchronizeAmmunition(int pellets, int plasmas, int rockets) {
        ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot()).setAmount(pellets);
        ammunitionLoaders.get(AmmunitionSlot.PLASMAGUN.slot())
                .setAmount(plasmas);
        ammunitionLoaders.get(AmmunitionSlot.ROCKETS.slot()).setAmount(rockets);
    }

    public void likeAPro() {
        ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot()).addAmmunition(16);
        ammunitionLoaders.get(AmmunitionSlot.PLASMAGUN.slot()).addAmmunition(3);
        ammunitionLoaders.get(AmmunitionSlot.ROCKETS.slot()).addAmmunition(2);
    }
}

class AmmunitionLoader {

    private final int amountPerTick;
    private final int time;
    private final int maxAmount;
    private int amount;
    private float timer = 0f;

    private AmmunitionLoader(int amountPerTick, int time, int startingAmount,
            int maxAmount) {
        this.amountPerTick = amountPerTick;
        this.time = time;
        this.amount = startingAmount;
        this.maxAmount = maxAmount;
    }

    public void update(final float tpf) {
        if (amount >= maxAmount) {
            timer = 0f;
            return;
        }
        timer += tpf;
        if (timer >= time) {
            timer = 0f;
            amount += amountPerTick;
            if (amount > maxAmount) {
                amount = maxAmount;
            }
        }
    }

    public int predictAmmoAmount(float towardsFuture) {
        if (timer + towardsFuture >= time) {
            return amount + amountPerTick;
        }

        return amount;
    }

    public void consumeAmmo(int amount) {
        this.amount -= amount;
    }

    public boolean hasEnough(int needed) {
        return amount >= needed;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void addAmmunition(int amount) {
        this.amount += amount;
        if (this.amount > maxAmount) {
            this.amount = maxAmount;
        }
    }

    public static AmmunitionLoader Shotgun() {
        return new AmmunitionLoader(4, 1, 36, 36);
    }

    public static AmmunitionLoader Plasmagun() {
        return new AmmunitionLoader(3, 8, 6, 9);
    }

    public static AmmunitionLoader Rocket() {
        return new AmmunitionLoader(1, 8, 3, 4);
    }
}