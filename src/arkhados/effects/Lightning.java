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
package arkhados.effects;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jme3tools.optimize.GeometryBatchFactory;

public class Lightning {

    private static List<Segment> generateSegments(float jitter) {
        List<Segment> segments = new ArrayList<>();
        segments.add(new Segment(Vector3f.ZERO.clone(), Vector3f.UNIT_Z.clone()));

        List<Segment> aux = new ArrayList<>();

        Vector3f mid = new Vector3f();
        Vector3f perpendicular = new Vector3f();

        for (int gen = 0; gen < 3; gen++) {
            aux.addAll(segments);
            segments.clear();
            for (Segment seg : aux) {
                FastMath.interpolateLinear(0.5f, seg.first, seg.second, mid);
                perpendicular.set(seg.second).subtractLocal(seg.first)
                        .normalizeLocal();
                float tempX = perpendicular.x;
                perpendicular.x = perpendicular.z;
                perpendicular.z = -tempX;

                // Now perpendicular IS perpendicular
                perpendicular.multLocal(
                        (FastMath.rand.nextFloat() - 0.5f) * 2f * jitter
                );
                mid.addLocal(perpendicular);

                segments.add(new Segment(seg.first, mid.clone()));
                segments.add(new Segment(mid.clone(), seg.second));
            }

            aux.clear();
        }

        return segments;
    }

    private static Mesh createMesh(List<Segment> segments) {
        List<Geometry> geoms = new ArrayList<>();
        for (Segment segment : segments) {
//            
            Quad quad = new Quad(0.5f, segment.first.distance(segment.second));
            Geometry geom = new Geometry("lightning-part", quad);
            float angle = FastMath.atan2(segment.second.z - segment.first.z,
                    segment.second.x - segment.first.x);
            geom.rotate(0f, angle, 0f);
            geom.setLocalTranslation(segment.first.x, 0, segment.first.z);
            geoms.add(geom);
        }
        Mesh outMesh = new Mesh();
        GeometryBatchFactory.mergeGeometries(geoms, outMesh);
        return outMesh;
    }

    public static Mesh createGeometry(float jitter, float widthFactor) {
        List<Segment> segments = generateSegments(jitter);
        Mesh mesh = new Mesh();

        Segment first = segments.get(0);

        SegmentBufferData firstSbData
                = createSegmentBufferData(Vector3f.ZERO, first.second, widthFactor);

        Vector3f[] positions = new Vector3f[(segments.size() + 1) * 2];
        positions[0] = firstSbData.leftPos;
        positions[1] = firstSbData.rightPos;

        short posIdx = 2;

        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            Vector3f directionGiver = i < segments.size() - 1 
                    ? segments.get(i + 1).second
                    : segment.second.subtract(segment.first).normalizeLocal().addLocal(segment.second);
            SegmentBufferData sbData = createSegmentBufferData(
                    segment.second, directionGiver, widthFactor);
            positions[posIdx++] = sbData.leftPos;
            positions[posIdx++] = sbData.rightPos;
        }

        Vector2f[] textureCoords = new Vector2f[positions.length];
        for (int i = 0; i < textureCoords.length; ++i) {
            float yTexCoord = positions[i].z;
            float xTexCoord = positions[i].x;
            textureCoords[i] = new Vector2f(xTexCoord, yTexCoord);
        }

        // Each 'quad' requires 2 triangles. Each triangle requires 3 positions.
        // Thus, 6 positions per quad
        short[] indices = new short[segments.size() * 6];
        int greatestIndex = positions.length - 1;
        short indexIdx = 0;
        short i = 0;
        for (;;) {
            // The greatest possible index for a left side triangle is 
            // greatestIndex - 1
            if (indexIdx == greatestIndex - 1) {
                break;
            }
            indices[i++] = indexIdx;
            indices[i++] = ++indexIdx;
            indices[i++] = ++indexIdx;

        }
        
        for (indexIdx = (short) greatestIndex;;) {
            
            // The smallest possible index for a right triangle is 1
            if (indexIdx == 1) {
                break;
            }
            indices[i++] = indexIdx;
            indices[i++] = --indexIdx;
            indices[i++] = --indexIdx;
        }

        mesh.setBuffer(VertexBuffer.Type.Position, 3,
                BufferUtils.createFloatBuffer(positions));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2,
                BufferUtils.createFloatBuffer(textureCoords));
        mesh.setBuffer(VertexBuffer.Type.Index, 3,
                BufferUtils.createShortBuffer(indices));

        mesh.updateBound();
        return mesh;

    }

    private static SegmentBufferData createSegmentBufferData(
            Vector3f first, Vector3f second, float widthFactor) {
        TempVars vars = TempVars.get();
        Vector3f dir = vars.vect1;
        Vector3f perpendicular = vars.vect2;

        second.subtract(first, dir).normalizeLocal();
        perpendicular.set(dir);

        float tempX = perpendicular.x;
        perpendicular.x = perpendicular.z;
        perpendicular.z = -tempX;
        perpendicular.multLocal(widthFactor);

        SegmentBufferData data = new SegmentBufferData();
        first.add(perpendicular, data.leftPos);
        first.add(perpendicular.negateLocal(), data.rightPos);
        vars.release();

        return data;
    }
}

class SegmentBufferData {

    public Vector3f leftPos = new Vector3f();
    public Vector3f rightPos = new Vector3f();
    public Vector3f leftBitangent = new Vector3f();
    public Vector3f rightBitangent = new Vector3f();
}

class Segment {

    Vector3f first;
    Vector3f second;

    public Segment(Vector3f first, Vector3f second) {
        this.first = first;
        this.second = second;
    }
}
