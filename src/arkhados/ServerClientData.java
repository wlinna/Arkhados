/*
 * Copyright (c) 2009-2011 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package arkhados;

import com.jme3.network.HostedConnection;
import java.util.Collection;
import java.util.HashMap;

/**
 * Stores info about active clients on the server
 * @author normenhansen
 */
public class ServerClientData {
    
    private static final HashMap<Integer, HostedConnection> connections = new HashMap<>();
    private static final HashMap<Integer, ServerClientData> players = new HashMap<>();
    private long latencySampleCount = 0;
    private float latestLatency;
    private float averageLatency;
    private int playerId;    
    private boolean connected;
    
    public static synchronized Collection<Integer> getClients() {
        return players.keySet();
    }
    
    public static synchronized void add(int id) {
        ServerClientData.players.put(id, new ServerClientData());                
    }

    public static synchronized void remove(int id) {
        ServerClientData.players.remove(id);
    }

    public static synchronized boolean exists(int id) {
        return ServerClientData.players.containsKey(id);
    }

    public static synchronized boolean isConnected(int id) {
        return ServerClientData.players.get(id).isConnected();
    }

    public static synchronized void setConnected(int id, boolean connected) {
        ServerClientData.players.get(id).setConnected(connected);
    }

    public static synchronized int getPlayerId(int id) {
        return ServerClientData.players.get(id).getPlayerId();
    }

    public static synchronized void setPlayerId(int id, int playerId) {
        ServerClientData.players.get(id).setPlayerId(playerId);
    }
    
    public static synchronized void addLatencySample(int id, float sample) {
        ServerClientData.players.get(id).addLatencySample(sample);
    }
    
    public static synchronized float getLatestLatency(int id) {
        return ServerClientData.players.get(id).getLatestLatency();
    }
    
    public static synchronized float getAverageLatency(int id) {
        return ServerClientData.players.get(id).getAverageLatency();
    }

    public static synchronized void addConnection(int playerId,
            HostedConnection connection) {
        connections.put(playerId, connection);
    }
    
    public static synchronized void removeConnection(int playerId) {
        connections.remove(playerId);
    }
    
    public static synchronized HostedConnection getConnection(int playerId) {
        return connections.get(playerId);
    }    

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public void addLatencySample(float latency) {
        averageLatency = (averageLatency * latencySampleCount + latency)
                / (latencySampleCount + 1);
        latestLatency = latency;
        ++latencySampleCount;
    }
    
    public float getAverageLatency() {
        return averageLatency;
    }
    
    public float getLatestLatency() {
        return latestLatency;
    }
}
