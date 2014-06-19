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
package arkhados.net;

import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */
public class GuaranteedSending implements MessageListener {

    private int runningId = 0;
    private Server server;
    private Map<Integer, MessageWithId> unAckedMessages = new HashMap<>(40);

    public void broadcast(MessageWithId message) {
        this.unAckedMessages.put(runningId++, message);
        server.broadcast(message);
    }
    
    private void resendUnAcked() {
        for (MessageWithId message : unAckedMessages.values()) {
            
        }
    }

    @Override
    public void messageReceived(Object source, Message m) {
        Ack message = (Ack) m;

        if (unAckedMessages.containsKey(message.getId())) {
            unAckedMessages.remove(message.getId());
        }
    }
}
