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

import arkhados.Globals;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import java.util.LinkedList;
import java.util.Queue;

public class AudioQueue {

    private AudioTracker current;
    private final Queue<AudioNode> queue = new LinkedList<>();

    public synchronized void enqueueAudio(final AudioNode audio) {
        if (current == null) {
            current = new AudioTracker(audio);
            Globals.app.enqueue(() -> {
                audio.play();
                return null;
            });
        } else {
            queue.add(audio);
        }
    }

    public synchronized void update() {
        if (current == null) {
            return;
        }

        boolean playing = current.update();

        if (!playing) {
            AudioNode next = queue.peek();

            if (next != null) {
                next.play();
                queue.remove();
                current = new AudioTracker(next);
            } else {
                current = null;
            }
        }
    }
}

class AudioTracker {

    private final AudioNode node;
    private boolean hasStarted;

    public AudioTracker(AudioNode node) {
        this.node = node;
    }

    public boolean update() {
        if (hasStarted && node.getStatus() != AudioSource.Status.Playing) {
            return false;
        }

        if (node.getStatus() == AudioSource.Status.Playing) {
            hasStarted = true;
        }

        return true;
    }
}