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
package arkhados.ui.hud;

import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import java.util.ArrayList;
import java.util.List;

public class PlayerStatisticsPanelBuilder extends PanelBuilder {

    public PlayerStatisticsPanelBuilder(int num) {
        super(String.valueOf(num) + "-statistics");
        alignLeft();
        valignTop();
        childLayoutHorizontal();
        height("50px");
        width("100%");
        backgroundImage("Interface/Images/InfoTab.png");
        imageMode("resize");

        List<TextBuilder> textBuilders = new ArrayList<>();
        
        StatisticsTextBuilder nameBuilder =
                new StatisticsTextBuilder(num + "-name");
        textBuilders.add(nameBuilder);
        textBuilders.add(new StatisticsTextBuilder(num + "-damage"));
        textBuilders.add(new StatisticsTextBuilder(num + "-restoration"));
        textBuilders.add(new StatisticsTextBuilder(num + "-kills"));
        
        for (TextBuilder b : textBuilders) {
            text(b);
        }
    }
}

class StatisticsTextBuilder extends TextBuilder {

    public StatisticsTextBuilder(String id) {
        super(id);
        alignLeft();
        valignCenter();
        style("nifty-label");
        width("70px");
        marginLeft("80px");
        textHAlignLeft();
        color("#f00f");        
    }
}