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
package arkhados.spell;

import arkhados.Globals;
import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.controls.CProjectile;
import arkhados.spell.spells.electrobot.ElectricBomb;
import arkhados.spell.spells.electrobot.ElectricPull;
import arkhados.spell.spells.electrobot.ElectroCharge;
import arkhados.spell.spells.electrobot.Paralyze;
import arkhados.spell.spells.electrobot.Zap;
import arkhados.spell.spells.shadowmancer.ShadowOrb;
import arkhados.spell.spells.elitesoldier.LikeAPro;
import arkhados.spell.spells.elitesoldier.Plasmagun;
import arkhados.spell.spells.elitesoldier.Railgun;
import arkhados.spell.spells.elitesoldier.RocketJump;
import arkhados.spell.spells.elitesoldier.RocketLauncher;
import arkhados.spell.spells.elitesoldier.Shotgun;
import arkhados.spell.spells.embermage.EmberCircle;
import arkhados.spell.spells.embermage.Fireball;
import arkhados.spell.spells.embermage.Firewalk;
import arkhados.spell.spells.embermage.Ignite;
import arkhados.spell.spells.embermage.MagmaBash;
import arkhados.spell.spells.embermage.Meteor;
import arkhados.spell.spells.embermage.PurifyingFlame;
import arkhados.spell.spells.rockgolem.EarthQuake;
import arkhados.spell.spells.rockgolem.MineralArmor;
import arkhados.spell.spells.rockgolem.SealingBoulder;
import arkhados.spell.spells.rockgolem.SpiritStone;
import arkhados.spell.spells.rockgolem.StoneFist;
import arkhados.spell.spells.rockgolem.Toss;
import arkhados.spell.spells.shadowmancer.DarkEnergy;
import arkhados.spell.spells.shadowmancer.DarkEnergySelf;
import arkhados.spell.spells.shadowmancer.VoidSpear;
import arkhados.spell.spells.shadowmancer.IntoTheShadows;
import arkhados.spell.spells.shadowmancer.Shadow;
import arkhados.spell.spells.shadowmancer.Drain;
import arkhados.spell.spells.shadowmancer.MindPoison;
import arkhados.spell.spells.venator.Backlash;
import arkhados.spell.spells.venator.Dagger;
import arkhados.spell.spells.venator.DeepWounds;
import arkhados.spell.spells.venator.FeralScream;
import arkhados.spell.spells.venator.Leap;
import arkhados.spell.spells.venator.Rend;
import arkhados.util.EntityFactory;
import arkhados.util.AbstractNodeBuilder;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.util.IntMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Spell contains data of spell's base data. Each Spell is created only once
 * and their data does not change.
 */
public abstract class Spell {

    protected static World world = null;
    /**
     * Spells has all spells mapped by their name so that spell data can be
     * retrieved from anywhere
     */
    private static final IntMap<Spell> Spells = new IntMap<>();
    private static final Map<String, Integer> SpellNameCreationIdMap =
            new HashMap<>();

    /**
     * Creates each spell and saves them to Spells-map. Should be called only
     * once
     */
    public static void initSpells(EntityFactory entityFactory, World world) {
        Spell.world = world;

        AbstractNodeBuilder.setAssetManager(Globals.assets);
        AbstractNodeBuilder.setWorld(world);

        CProjectile.setWorld(world);

        // *************** INIT spells here ************************

        addSpell(entityFactory, Fireball.create());
        addSpell(entityFactory, MagmaBash.create());
        addSpell(entityFactory, EmberCircle.create());
        addSpell(entityFactory, Meteor.create());
        addSpell(entityFactory, PurifyingFlame.create());
        addSpell(entityFactory, Firewalk.create());
        addSpell(entityFactory, Ignite.create());

        addSpell(entityFactory, Rend.create());
        addSpell(entityFactory, Dagger.create());
        addSpell(entityFactory, Leap.create());
        addSpell(entityFactory, FeralScream.create());
        addSpell(entityFactory, DeepWounds.create());
        addSpell(entityFactory, Backlash.create());

        addSpell(entityFactory, Shotgun.create());              
        addSpell(entityFactory, Railgun.create());
        addSpell(entityFactory, Plasmagun.create());
        addSpell(entityFactory, RocketLauncher.create());
        addSpell(entityFactory, LikeAPro.create());
        addSpell(entityFactory, RocketJump.create());

        addSpell(entityFactory, StoneFist.create());
        addSpell(entityFactory, SealingBoulder.create());
        addSpell(entityFactory, SpiritStone.create());
        addSpell(entityFactory, Toss.create());
        addSpell(entityFactory, MineralArmor.create());
        addSpell(entityFactory, EarthQuake.create());
        
        addSpell(entityFactory, ShadowOrb.create());
        addSpell(entityFactory, DarkEnergy.create());
        addSpell(entityFactory, DarkEnergySelf.create());
        addSpell(entityFactory, Drain.create());
        addSpell(entityFactory, MindPoison.create());
        addSpell(entityFactory, VoidSpear.create());
        addSpell(entityFactory, Shadow.create());      
        addSpell(entityFactory, IntoTheShadows.create());
        
        addSpell(entityFactory, Zap.create());
        addSpell(entityFactory, ElectricBomb.create());
        addSpell(entityFactory, ElectricPull.create());
        addSpell(entityFactory, Paralyze.create());
        addSpell(entityFactory, ElectroCharge.create());
    }

    private static void addSpell(EntityFactory entityFactory, Spell spell) {
        int nodeBuilderId = entityFactory.addNodeBuilder(spell.nodeBuilder);
        spell.setId(nodeBuilderId);
        Spells.put(nodeBuilderId, spell);
        SpellNameCreationIdMap.put(spell.getName(), nodeBuilderId);
    }

    public static Spell getSpell(int creationId) {
        return Spells.get(creationId);
    }

    public static Spell getSpell(String spellName) {
        Integer creationId = SpellNameCreationIdMap.get(spellName);

        if (creationId == null) {
            return null;
        }

        return Spells.get(creationId);
    }
    private final String name;
    private int id;
    private final float cooldown;
    private final float range;
    private final float castTime;
    private boolean canMoveWhileCasting = false;
    private boolean moveTowardsTarget = true;
    // HACK: This is used by SpellCastControl to determine if character should restore walking after casting action
    protected boolean multipart = false;
    protected String iconName = null;
    protected CastSpellActionBuilder castSpellActionBuilder;
    protected AbstractNodeBuilder nodeBuilder;

    /**
     * Creates spell with given parameters
     *
     * @param name visible to player so give human friendly name
     * @param cooldown Time it takes to 'reload' spell so that it can be used again
     * @param range range of spell. NOTE: Currently not used in projectiles
     * @param castTime Time it takes to cast spell
     */
    protected Spell(String name, float cooldown, float range, float castTime) {
        this.name = name;
        this.cooldown = cooldown;
        this.range = range;
        this.castTime = castTime;
    }

    public String getName() {
        return name;
    }

    public float getCooldown() {
        return cooldown;
    }

    public float getRange() {
        return range;
    }

    public float getCastTime() {
        return castTime;
    }

    public String getIconName() {
        return iconName;
    }

    /**
     * Constructs new EntityAction that will cast the spell.
     *
     * @param vec Initial direction or target location vector, depending on spell. Often not
     * necessary.
     * @return EntityAction that will cast the spell
     */
    public EntityAction buildCastAction(Node caster, Vector3f vec) {
        return castSpellActionBuilder.newAction(caster, vec);
    }

    public boolean canMoveWhileCasting() {
        return canMoveWhileCasting;
    }

    public void setCanMoveWhileCasting(boolean canMoveWhileCasting) {
        this.canMoveWhileCasting = canMoveWhileCasting;
    }

    public boolean moveTowardsTarget() {
        return moveTowardsTarget;
    }

    public void setMoveTowardsTarget(boolean mustMoveTowardsViewDirection) {
        this.moveTowardsTarget = mustMoveTowardsViewDirection;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}