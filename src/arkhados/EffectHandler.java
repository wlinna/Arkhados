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
package arkhados;

import arkhados.effects.EffectBox;
import arkhados.messages.syncmessages.ActionCommand;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.util.UserDataStrings;
import com.jme3.app.Application;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author william
 */
public class EffectHandler implements CommandHandler {

    private Application app;
    private WorldManager worldManager;
    private HashMap<Integer, EffectBox> actionEffects = new HashMap<>();

    public EffectHandler(Application app) {
        this.app = app;
    }

    public void addEffectBox(int id, EffectBox effectBox) {
        this.actionEffects.put(id, effectBox);
    }

    private void handleAction(final ActionCommand actionCommand) {
        final Spatial entity = this.worldManager.getEntity(actionCommand.getSyncId());
        if (entity == null) {
            return;
        }
        
        int nodeBuilderId = entity.getUserData(UserDataStrings.NODE_BUILDER_ID);         
        
        final EffectBox box = this.actionEffects.get(nodeBuilderId);
        if (box == null) {
            return;
        }

        this.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                box.executeActionEffect(actionCommand.getActionId(), worldManager.getWorldRoot(),
                        entity.getLocalTranslation());
                return null;
            }
        });
    }

    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void readGuaranteed(Object source, List<Command> guaranteed) {
        for (Command command : guaranteed) {
            if (command instanceof ActionCommand) {
                this.handleAction((ActionCommand) command);
            }
        }
    }

    @Override
    public void readUnreliable(Object source, List<Command> unreliables) {
    }
}