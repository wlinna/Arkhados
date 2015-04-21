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

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.post.FilterPostProcessor;
import com.shaderblow.filter.grayscale.GrayScaleFilter;

/**
 *
 * @author william
 */
public class DeathManager extends AbstractAppState {

    private Application app;
    private FilterPostProcessor fpp;
    private boolean dead = false;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
        fpp = new FilterPostProcessor(app.getAssetManager());
        app.getViewPort().addProcessor(fpp);
    }

    public void death() {
        if (!dead) {
            fpp.addFilter(new GrayScaleFilter());
            dead = true;
        }
    }

    public void revive() {
        if (dead) {
            fpp.removeAllFilters();
            dead = false;
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        revive();
        app.getViewPort().removeProcessor(fpp);
    }
}