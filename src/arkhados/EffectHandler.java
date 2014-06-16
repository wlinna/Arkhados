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

import arkhados.effects.RocketExplosionEffect;
import arkhados.effects.SimpleSoundEffect;
import arkhados.effects.WorldEffect;
import arkhados.messages.effect.EffectMessage;
import com.jme3.app.Application;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 *
 * @author william
 */
public class EffectHandler implements MessageListener {
    // HACK: I just wanted to get rid of Strings in messages fast. This solution
    // is very ugly though and needs to be refactored later

    public static final int ROCKET_EXPLOSION = 0;
    public static final int SIMPLE_SOUND_EFFECT = 1;
    
    private static final HashMap<Integer, String> numberParameterMap = new HashMap<>();
    private static final HashMap<String, Integer> parameterNumberMap = new HashMap<>();

    {
        mapNumberAndParameter(SHOTGUN_WAV, "Effects/Sound/Shotgun.wav");
    }

    private static void mapNumberAndParameter(int number, String parameter) {
        numberParameterMap.put(number, parameter);
        parameterNumberMap.put(parameter, number);
    }
    
    private static int getParameterNumber(String parameter) {
        if (parameter == null ||  !parameterNumberMap.containsKey(parameter)) {
            return -1;
        }
        
        return parameterNumberMap.get(parameter);
    }
    
    public static final int SHOTGUN_WAV = 0;
    private Application app;
    private Server server = null;
    private WorldManager worldManager;
    private HashMap<Integer, WorldEffect> effects = new HashMap<>();

    {
        this.effects.put(ROCKET_EXPLOSION, new RocketExplosionEffect());
        this.effects.put(SIMPLE_SOUND_EFFECT, new SimpleSoundEffect());
    }

    public EffectHandler(Application app, WorldManager worldManager) {
        this.app = app;
        this.worldManager = worldManager;
    }

    public EffectHandler(Application app, Server server) {
        this.app = app;
        this.server = server;
    }

    @Override
    public void messageReceived(Object source, Message m) {
        if (m instanceof EffectMessage) {
            this.effect((EffectMessage) m);
        }
    }

    private void effect(final EffectMessage message) {
        final WorldEffect worldEffect = effects.get(message.getEffectName());
        if (worldEffect == null) {
            return;
        }

        this.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                worldEffect.execute(worldManager.getWorldRoot(), message.getLocation(), numberParameterMap.get(message.getParameter()));
                return null;
            }
        });
    }

    public void sendEffect(int effectId, String effectParameter, Vector3f location) {
        this.server.broadcast(new EffectMessage(effectId, getParameterNumber(effectParameter) , location));
    }

    public void setMessagesToListen(Client client) {
        client.addMessageListener(this, EffectMessage.class);
    }
}