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

public class Timer {

    private float original;
    private float timeLeft;
    private boolean active = false;
    private boolean timeJustEnded = false;

    public Timer(float seconds) {
        this.original = seconds;
        this.timeLeft = seconds;
    }

    public void update(float tpf) {
        this.timeJustEnded = false;

        if (!active) {
            return;
        }

        if (this.timeLeft > 0) {
            this.timeLeft -= tpf;
            if (this.timeLeft <= 0) {
                this.timeJustEnded = true;
            }
        }
    }

    public float getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(float timeLeft) {
        this.timeLeft = timeLeft;
        this.original = timeLeft;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean timeJustEnded() {
        return timeJustEnded;
    }

    public float getOriginal() {
        return original;
    }
}