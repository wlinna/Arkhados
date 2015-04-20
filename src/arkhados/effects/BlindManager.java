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
package arkhados.effects;

import arkhados.Globals;
import arkhados.UserCommandManager;
import arkhados.spell.buffs.buffinformation.BuffInfoParameters;
import arkhados.util.UserDataStrings;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Spatial;
import com.shaderblow.filter.colorscale.ColorScaleFilter;
import java.util.ArrayList;
import java.util.List;

public class BlindManager extends AbstractAppState {

    private List<BlindEffect> blinds = new ArrayList<>();
    private AppStateManager stateManager;
    private FilterPostProcessor fpp =
            new FilterPostProcessor(Globals.assetManager);
    private ColorScaleFilter filter;
    private Application app;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
        this.stateManager = stateManager;
        fpp.setNumSamples(4);
    }

    void addBlindIfSelf(BlindEffect blind, BuffInfoParameters params) {
        int myCharacterId = stateManager.getState(UserCommandManager.class)
                .getCharacterId();
        Spatial spatial = params.buffControl.getSpatial();
        int entityId = spatial.getUserData(UserDataStrings.ENTITY_ID);

        if (entityId == myCharacterId) {
            addEffect();
            blinds.add(blind);
        }
    }

    private void addEffect() {
        if (!blinds.isEmpty()) {
            return;
        }

        // TODO: Add more impressive blind effect
        filter = new ColorScaleFilter(ColorRGBA.White.clone(), 0.4f);
        fpp.addFilter(filter);
        app.getViewPort().addProcessor(fpp);
    }

    public void removeBuffIfSelf(BlindEffect blind) {
        blinds.remove(blind);

        if (blinds.isEmpty()) {
            clean();
        }
    }

    private void clean() {
        if (fpp != null) {
            fpp.removeAllFilters();
            app.getViewPort().removeProcessor(fpp);
            filter = null;
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        clean();
    }
}
