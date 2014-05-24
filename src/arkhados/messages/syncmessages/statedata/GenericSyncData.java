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

import arkhados.controls.SyncInterpolationControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
@Serializable
public class GenericSyncData extends StateData {

    private Vector3f location = new Vector3f();
    private Quaternion rotation = new Quaternion();

    public GenericSyncData() {
    }        

    public GenericSyncData(long syncId, Spatial spatial) {
        super(syncId);        
        this.location.set(spatial.getLocalTranslation());
        this.rotation.set(spatial.getLocalRotation());
    }        

    @Override
    public void applyData(Object target) {
        Spatial spatial = (Spatial) target;
        SyncInterpolationControl syncInterpolation = spatial.getControl(SyncInterpolationControl.class);
        if (syncInterpolation != null) {
            syncInterpolation.interpolate(this.location);
        } else {
            spatial.setLocalTranslation(this.location);
        }
        spatial.setLocalRotation(this.rotation);
    }
}