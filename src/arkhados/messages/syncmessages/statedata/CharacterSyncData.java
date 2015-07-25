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

package arkhados.messages.syncmessages.statedata;

import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSyncInterpolation;
import arkhados.util.UserData;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */

@Serializable
public class CharacterSyncData extends StateData {

    private Vector3f location = new Vector3f();
    private Vector3f walkDirection = new Vector3f();
    private Vector3f viewDirection = new Vector3f();
    private float health;

    public CharacterSyncData() {        
    }

    public CharacterSyncData(int id, Spatial spatial) {
        super(id);        
        location.set(spatial.getLocalTranslation());
        CCharacterPhysics body = spatial.getControl(CCharacterPhysics.class);
        walkDirection.set(body.getWalkDirection());
        viewDirection.set(body.getViewDirection());
        health = spatial.getUserData(UserData.HEALTH_CURRENT);
    }

    @Override
    public void applyData(Object target) {
        Spatial character = (Spatial) target;
        character.getControl(CInfluenceInterface.class).setHealth(health);
        character.getControl(CSyncInterpolation.class).interpolate(location);
        CCharacterPhysics body = character.getControl(CCharacterPhysics.class);
        body.warp(location);
        body.setViewDirection(viewDirection);
        body.setWalkDirection(walkDirection);
    }

    @Override
    public boolean isGuaranteed() {
        return false;
    }   
}