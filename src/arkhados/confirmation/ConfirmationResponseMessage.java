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
package arkhados.confirmation;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author william
 */
@Serializable
public class ConfirmationResponseMessage extends AbstractMessage {

    private int id;
    private boolean accepted;

    public ConfirmationResponseMessage() {
    }

    public ConfirmationResponseMessage(int id, boolean accepted) {
        this.id = id;
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public int getId() {
        return id;
    }
}
