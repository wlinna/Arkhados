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

package arkhados.util;

import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import java.util.Iterator;


public class NiftyUtils {
    public static void removeChildren(Element element) {
        for (Iterator<Element> it = element
                .getChildren().iterator(); it.hasNext();) {

            Element child = it.next();
            child.markForRemoval();
        }
    }

    public static void removeChildren(Screen screen, String elementName) {
        removeChildren(screen.findElementByName(elementName));
    }
}
