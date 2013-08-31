/*    This file is part of JMageBattle.

 JMageBattle is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 JMageBattle is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */
package magebattle.controls;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import magebattle.WorldManager;
import magebattle.actions.CastSpellAction;
import magebattle.actions.RunToAction;
import magebattle.spells.Spell;
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class SpellCastControl extends AbstractControl {

    private Spatial character;
    private WorldManager worldManager;
    private HashMap<String, Float> cooldowns = new HashMap<String, Float>();

    public SpellCastControl(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        this.character = spatial;
    }

    public void cast(final String spellName, Vector3f targetLocation) {
        if (!this.enabled) {
            return;
        }
//        if (cooldowns.get(name) <= 0.0f) {
        // TODO: make character run close enough before casting
//            Integer level = this.levels.get(name);
//            cooldowns.put(name, Spell.getSpells().get(name).getCooldown(level));

        if (this.worldManager.isServer()) {

            Spell spell = Spell.getSpells().get(spellName);
            final int LEVEL = 1; // TODO: Make levels dynamic
            final float range = spell.getRange(LEVEL);

            if ("Fireball".equals(spell.getName())) {
                // TODO: Add spell casting time and animation

                float characterRadius = super.spatial.getUserData(UserDataStrings.RADIUS);
                final Vector3f viewDirection = targetLocation.subtract(super.spatial.getLocalTranslation()).normalizeLocal();

                super.spatial.getControl(CharacterPhysicsControl.class).setViewDirection(viewDirection);

                long projectileId = this.worldManager.addNewEntity(spell.getName(), super.spatial.getLocalTranslation().add(viewDirection.mult(characterRadius)).addLocal(0f, 10.0f, 0.0f), Quaternion.IDENTITY);
                Spatial projectile = this.worldManager.getEntity(projectileId);

                final SphereCollisionShape collisionSphere = (SphereCollisionShape) projectile.getControl(RigidBodyControl.class).getCollisionShape();

                // FIXME: Get radius of BetterCharacterControl's capsule
                final float radius = collisionSphere.getRadius() * 1.0f;

                RigidBodyControl body = projectile.getControl(RigidBodyControl.class);
                body.setPhysicsLocation(body.getPhysicsLocation().add(viewDirection.multLocal(radius + characterRadius)).addLocal(0.0f, 10.0f, 0.0f));

                // HACK: IMPORTANT! This must be changed to more generic ASAP
                projectile.getControl(ProjectileControl.class).setTarget(targetLocation);

            }

        }
    }

    private Vector3f findClosestCastingLocation(final Vector3f targetLocation, float range) {
        Vector3f displacement = super.getSpatial().getLocalTranslation().subtract(targetLocation);
        float distanceSquared = displacement.lengthSquared();
        if (displacement.lengthSquared() <= FastMath.sqr(range)) {
            return super.getSpatial().getLocalTranslation();

        }
        displacement.normalizeLocal().multLocal(range);
        return displacement.addLocal(targetLocation);
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (Map.Entry<String, Float> entry : this.cooldowns.entrySet()) {
            entry.setValue(entry.getValue() - tpf);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        SpellCastControl control = new SpellCastControl(this.worldManager);
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
}
