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

import arkhados.spell.Spell;
import arkhados.spell.SpellCastListener;
import arkhados.spell.SpellCastValidator;
import arkhados.spell.spells.elitesoldier.AmmunitionSlot;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the amount of EliteSoldiers ammo. Replenishes Elite Soldiers
 * ammunition and checks that Elite Soldier has enough ammo to cast spell
 *
 * @author william
 */
public class EliteSoldierAmmunitionControl extends AbstractControl implements SpellCastValidator, SpellCastListener {

    private final List<AmmunitionLoader> ammunitionLoaders = new ArrayList<>(4);

    {
        this.ammunitionLoaders.add(AmmunitionLoader.Shotgun());
        this.ammunitionLoaders.add(AmmunitionLoader.Machinegun());
        this.ammunitionLoaders.add(AmmunitionLoader.Plasmagun());
        this.ammunitionLoaders.add(AmmunitionLoader.Rocket());
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (AmmunitionLoader ammunitionLoader : ammunitionLoaders) {
            ammunitionLoader.update(tpf);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        EliteSoldierAmmunitionControl control = new EliteSoldierAmmunitionControl();
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

    @Override
    public boolean validateSpellCast(SpellCastControl castControl, Spell spell) {
        if ("Shotgun".equals(spell.getName())) {
            return ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot()).hasEnough(6);
        } else if ("Machinegun".equals(spell.getName())) {
            return ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot()).hasEnough(1);
        } else if ("Plasmagun".equals(spell.getName())) {
            return ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot()).hasEnough(3);
        } else if ("Rocket Launcher".equals(spell.getName())
                || "Rocket Jump".equals(spell.getName())) {
            return ammunitionLoaders.get(AmmunitionSlot.ROCKETS.slot()).hasEnough(1);
        }

        return true;
    }

    @Override
    public void spellCasted(SpellCastControl castControl, Spell spell) {
        if ("Shotgun".equals(spell.getName())) {
            this.ammunitionLoaders.get(AmmunitionSlot.SHOTGUN.slot()).consumeAmmo(6);
        } else if ("Machinegun".equals(spell.getName())) {
            this.ammunitionLoaders.get(AmmunitionSlot.MACHINEGUN.slot()).consumeAmmo(1);
        } else if ("Plasmagun".equals(spell.getName())) {
            this.ammunitionLoaders.get(AmmunitionSlot.PLASMAGUN.slot()).consumeAmmo(3);
        } else if ("Rocket Launcher".equals(spell.getName())
                || "Rocket Jump".equals(spell.getName())) {
            this.ammunitionLoaders.get(AmmunitionSlot.ROCKETS.slot()).consumeAmmo(1);
        }
    }
}

class AmmunitionLoader {

    private final int amountPerTick;
    private final int time;
    private final int maxAmount;
    private int amount;
    private float timer = 0f;

    private AmmunitionLoader(int amountPerTick, int time, int startingAmount, int maxAmount) {
        this.amountPerTick = amountPerTick;
        this.time = time;
        this.amount = startingAmount;
        this.maxAmount = maxAmount;
    }

    public void update(final float tpf) {
        if (this.amount >= this.maxAmount) {
            this.timer = 0f;
            return;
        }
        this.timer += tpf;
        if (this.timer >= this.time) {
            this.timer = 0f;
            this.amount += amountPerTick;
            if (this.amount > this.maxAmount) {
                this.amount = this.maxAmount;
            }
        }
    }

    public void consumeAmmo(int amount) {
        this.amount -= amount;
    }

    public boolean hasEnough(int needed) {
        return this.amount >= needed;
    }

    public static AmmunitionLoader Shotgun() {
        return new AmmunitionLoader(4, 1, 40, 40);
    }

    public static AmmunitionLoader Machinegun() {
        return new AmmunitionLoader(4, 1, 40, 40);
    }

    public static AmmunitionLoader Plasmagun() {
        return new AmmunitionLoader(3, 1, 0, 20);
    }

    public static AmmunitionLoader Rocket() {
        return new AmmunitionLoader(1, 7, 1, 4);
    }
}