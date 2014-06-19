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

import arkhados.util.ValueWrapper;
import com.jme3.app.state.AbstractAppState;
import com.jme3.network.NetworkClient;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author william
 */
public class Sender extends AbstractAppState {
    
    // These should always be added first
    private List<Command> unconfirmedGuaranteed = new ArrayList<>();
    
    private List<Command> enqueuedGuaranteed = new ArrayList<>();
    private List<Command> enqueuedUnreliables = new ArrayList<>();
        
    private Server server;
    private ValueWrapper<NetworkClient> client;
    
    
    private void broadcast() {
        assert this.client == null;
        
        OneTrueMessage otp = new OneTrueMessage();
        otp.getGuaranteed().addAll(this.unconfirmedGuaranteed);
        otp.getGuaranteed().addAll(this.enqueuedGuaranteed);
        otp.getUnreliables().addAll(this.enqueuedUnreliables);
        
        this.server.broadcast(otp);
        
        this.unconfirmedGuaranteed.addAll(enqueuedGuaranteed);
    }
    
    public void forceSend() {
        
    }
    
    public void confirmAllUntil(int until) {
        for (Iterator<Command> it = unconfirmedGuaranteed.iterator(); it.hasNext();) {
            Command command = it.next();
            
            if (command.getId() <= until) {
                it.remove();
            }
        }
    }
    
    public void addCommand(Command command) {
        if (command.isGuaranteed()) {
            this.enqueuedGuaranteed.add(command);
        } else {
            this.enqueuedUnreliables.add(command);
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
    }    
}
