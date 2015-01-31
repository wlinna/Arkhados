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
import java.util.concurrent.Callable;

/**
 *
 * @author william
 */
public class AudioQueue {

    private Queue<AudioNode> queue = new LinkedList<>();

    public synchronized void enqueueAudio(final AudioNode audio) {
        AudioNode current = queue.peek();
        queue.add(audio);

        if (current == null) {
            Globals.app.enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    audio.play();
                    return null;
                }
            });
        }
    }

    public synchronized void update() {
        AudioNode current = queue.peek();

        if (current == null) {
            return;
        }

        if (current.getStatus() == AudioSource.Status.Stopped) {
            queue.remove();
            AudioNode next = queue.peek();
            if (next != null) {
                next.play();
            }
        }
    }
}
