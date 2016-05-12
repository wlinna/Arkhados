/*
 * Copyright (c) 2009-2010 jMonkeyEngine
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
package arkhados.effects;

import com.jme3.effect.shapes.EmitterShape;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.system.Annotations;
import com.jme3.util.clone.Cloner;
import java.io.IOException;

public class EmitterArcShape implements EmitterShape {
     @Annotations.ReadOnly  private Vector3f origin;
    private float width;
    private float height;
    private float copyDistance;
    
    public EmitterArcShape() {
    }

    public EmitterArcShape(Vector3f origin, float width, float copyDistance,
            float scale) {
        if (origin == null) {
            throw new NullPointerException();
        }
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be greater than 0");
        }
        this.origin = origin;
        this.width = width * scale;
        this.height = 1f / this.width;
        this.copyDistance = copyDistance * scale;

    }

    @Override
    public EmitterShape deepClone() {
        try {
            EmitterArcShape clone = (EmitterArcShape) super.clone();
            clone.origin = origin.clone();
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public void getRandomPoint(Vector3f store) {
        float angle = FastMath.nextRandomFloat() * FastMath.TWO_PI;

        store.x = FastMath.cos(angle) * width;
        store.y = 0f;
        store.z = 0.5f * FastMath.abs(FastMath.sin(angle)) * height 
                - 0.5f * copyDistance;
        
        store.multLocal(1f + FastMath.nextRandomFloat() * copyDistance);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
    }

    @Override
    public void read(JmeImporter im) throws IOException {
    }

    @Override
    public void getRandomPointAndNormal(Vector3f store, Vector3f normal) {
        this.getRandomPoint(store);
    }

    @Override
    public Object jmeClone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
