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
package arkhados.spell.buffs;

import arkhados.CharacterInteraction;

public class DamageOverTimeBuff extends AbstractBuff {
    /**
     * Damage per second
     */
    private float dps;
    private float time;

    private DamageOverTimeBuff(float duration) {
        super(duration);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        time += tpf;        
        if (time > 0.05f) {            
            CharacterInteraction.harm(getOwnerInterface(), targetInterface,
                    dps * time, null, false);
            time = 0f;
        }
    }

    public void setDps(float dps) {
        this.dps = dps;
    }

    public static class MyBuilder extends AbstractBuffBuilder {
        private float dps;
                
        public MyBuilder(float duration) {
            super(duration);
        }

        public MyBuilder dps(float dps) {
            this.dps = dps;
            return this;
        }
        
        @Override
        public AbstractBuff build() {
            DamageOverTimeBuff dot = new DamageOverTimeBuff(duration);
            dot.setDps(dps);
            return set(dot);
        }
    }
}
