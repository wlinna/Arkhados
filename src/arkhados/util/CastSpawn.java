package arkhados.util;

import arkhados.Globals;
import arkhados.World;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class CastSpawn {
    public static class SpawnInfo {
        public Spatial spatial;
        public Vector3f viewDirection;
        public Vector3f mouseTarget;
    }

    public static SpawnInfo spawn(Spatial spatial, Spell spell) {
        
        CSpellCast cCast = spatial.getControl(CSpellCast.class);
        Vector3f mouseTarget = cCast.getClosestPointToTarget(spell);

        Vector3f viewDirection = mouseTarget.subtract(
                spatial.getLocalTranslation()).normalizeLocal();
        spatial.getControl(CCharacterPhysics.class)
                .setViewDirection(viewDirection);

        float characterRadius = spatial.getUserData(UserData.RADIUS);
        Vector3f spawnLocation = spatial.getLocalTranslation().add(
                viewDirection.mult(characterRadius / 1.5f))
                .addLocal(0f, 10.0f, 0.0f);

        int playerId = spatial.getUserData(UserData.PLAYER_ID);

        Quaternion rotation = spatial.getLocalRotation();

        World world = Globals.app.getStateManager().getState(World.class);
        int id = world.addNewEntity(spell.getId(), spawnLocation, rotation,
                playerId);
        
        SpawnInfo spawnInfo = new SpawnInfo();
        spawnInfo.spatial = world.getEntity(id);
        spawnInfo.mouseTarget = mouseTarget;
        spawnInfo.viewDirection = viewDirection;
        
        return spawnInfo;
    }
}
