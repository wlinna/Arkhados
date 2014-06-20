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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class Receiver implements MessageListener {
    private List<CommandHandler> handlers = new ArrayList<>();
    
    
    public void registerCommandHandler(CommandHandler handler) {        
        this.handlers.add(handler);        
    }
    
    private void ack() {
        // TODO: Use Sender to send ack
    }

    @Override
    public void messageReceived(Object source, Message m) {
        OneTrueMessage otp = (OneTrueMessage) m;
        
        this.handleGuaranteed(otp);
        this.handleUnreliable(otp);
        
        
    }
    
    private void handleGuaranteed(OneTrueMessage otp) {
        for (CommandHandler commandHandler : handlers) {
            commandHandler.readGuaranteed(otp.getGuaranteed());
        }
    }

    private void handleUnreliable(OneTrueMessage otp) {
        for (CommandHandler commandHandler: handlers) {
            commandHandler.readUnreliable(otp.getUnreliables());
        }
    }    
}