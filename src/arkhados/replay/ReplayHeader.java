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

package arkhados.replay;

import com.jme3.network.serializing.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Serializable
public class ReplayHeader {
    private Date date = new Date();
    private Map<Integer, String> players = new HashMap<>();

    public ReplayHeader() {
    }        
        
    public Map<Integer, String> getPlayers() {
        return players;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
