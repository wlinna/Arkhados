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
package arkhados.ui;

import de.lessvoid.nifty.builder.EffectBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;

/**
 *
 * @author william
 */
public class HeroSelectionBuilder extends PanelBuilder {

    {
        childLayoutCenter();
        onStartScreenEffect(new EffectBuilder("move") {
            {
                effectParameter("mode", "in");
                effectParameter("direction", "top");
                effectParameter("length", "200");
                effectParameter("startDelay", "0");
                inherit(true);
            }
        });

        onEndScreenEffect(new EffectBuilder("move") {
            {
                effectParameter("mode", "out");
                effectParameter("direction", "bottom");
                effectParameter("length", "200");
                effectParameter("startDelay", "0");
                inherit(true);
            }
        });

        text(new NiftyLabelBuilder("Select your hero"));
        text(new NiftyLabelBuilder(""));

        panel(new PanelBuilder() {
            {
                childLayoutHorizontal();
                panel(new HeroCategoryBuilder("Ranged",
                        new HeroButtonBuilder("EmberMage", "Ember Mage"),
                        new HeroButtonBuilder("EliteSoldier", "Elite Soldier")));
                panel(new HeroCategoryBuilder("Melee",
                        new HeroButtonBuilder("Venator", "Venator"),
                        new HeroButtonBuilder("Madblow", "Madblow")));
                panel(new HeroCategoryBuilder("Tank",
                        new HeroButtonBuilder("RockGolem", "Rock Golem")));
                panel(new HeroCategoryBuilder("Support",
                        new HeroButtonBuilder("Shadowmancer", "Shadowmancer")));
            }
        });
    }
}

class NiftyLabelBuilder extends TextBuilder {

    public NiftyLabelBuilder(String value) {
        style("nifty-label");
        text(value);
    }
}

class HeroCategoryBuilder extends PanelBuilder {

    public HeroCategoryBuilder(String categoryName, HeroButtonBuilder... heroButtons) {
        childLayoutVertical();
        text(new NiftyLabelBuilder(categoryName));

        for (HeroButtonBuilder heroButtonBuilder : heroButtons) {
            control(heroButtonBuilder);
        }
    }
}

class HeroButtonBuilder extends ButtonBuilder {

    public HeroButtonBuilder(String id, String buttonLabel) {
        super(id, buttonLabel);
        interactOnClick(String.format("selectHero(%s)", id));
    }
}
