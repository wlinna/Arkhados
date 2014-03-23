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
package arkhados;

import arkhados.messages.effect.SoundEffectMessage;
import com.jme3.app.Application;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.concurrent.Callable;

/**
 *
 * @author william
 */
public class EffectHandler implements MessageListener {

    private Application app;
    private Server server = null;

    public EffectHandler(Application app) {
        this.app = app;
    }

    public EffectHandler(Application app, Server server) {
        this.app = app;
        this.server = server;
    }
    
    

    @Override
    public void messageReceived(Object source, Message m) {
        if (m instanceof SoundEffectMessage) {
            this.soundEffect((SoundEffectMessage) m);
        }
    }

    /**
     * Plays sent sound effect on client side
     *
     * @param message
     */
    private void soundEffect(final SoundEffectMessage message) {
        this.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AudioNode sound = new AudioNode(Globals.assetManager, message.getSoundName());
                sound.setPositional(true);
                sound.setReverbEnabled(false);
                sound.setVolume(1f);
                sound.setLocalTranslation(message.getLocation());
                sound.play();
                return null;
            }
        });
    }

    /**
     * Sends sound effect that should be played at client side
     *
     * @param effectResource path to sound effect
     * @param location location where sound effect will be played
     */
    public void sendEffect(String effectResource, Vector3f location) {
        this.server.broadcast(new SoundEffectMessage(effectResource, location));
    }

    public void setMessagesToListen(Client client) {
        client.addMessageListener(this, SoundEffectMessage.class);
    }
}