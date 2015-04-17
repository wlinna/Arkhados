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

import arkhados.net.Command;
import arkhados.net.Sender;
import com.jme3.app.state.AbstractAppState;

public class FakeSender extends AbstractAppState implements Sender{

    @Override
    public void addCommand(Command command) {
    }

    @Override
    public void sendMessage() {
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public void reset() {        
    }

    @Override
    public void setShouldSend(boolean shouldSend) {
    }

    @Override
    public void readGuaranteed(Object source, Command guaranteed) {
    }

    @Override
    public void readUnreliable(Object source, Command unreliable) {
    }
}
