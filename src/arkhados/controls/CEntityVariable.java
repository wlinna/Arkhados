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

import arkhados.CharacterInteraction;
import arkhados.World;
import arkhados.net.Sender;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CEntityVariable extends AbstractControl {

    private World world;
    private PlayerEntityAwareness awareness;
    private Sender sender;
    
    public CEntityVariable(World worldManager, Sender sender) {
        this.world = worldManager;        
        this.sender = sender;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        // TODO: Move this feature to other control (or rename this)
        if (sender.isClient()) {
            return;
        }
        
        boolean validLoc = world.validateLocation(spatial.getLocalTranslation());
        if (!validLoc) {
            CInfluenceInterface influenceInterface =
                    spatial.getControl(CInfluenceInterface.class);
            if (influenceInterface != null) {
                CharacterInteraction.harm(null, influenceInterface, 75f * tpf, null, true);
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }


    public World getWorldManager() {
        return this.world;
    }

    public Sender getSender() {
        return sender;
    }

    public PlayerEntityAwareness getAwareness() {
        return awareness;
    }

    public void setAwareness(PlayerEntityAwareness awareness) {
        this.awareness = awareness;
    }
}