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
import com.jme3.math.Quaternion;
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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jme3tools.optimize.GeometryBatchFactory;

public class Lightning {
    private static List<Segment> generateSegments(float jitter, float branchProbability) {
        return generateSegments(jitter, branchProbability, 3);
    }
    private static List<Segment> generateSegments(
            float jitter, float branchProbability, int generations) {
        List<Segment> segments = new ArrayList<>();
        segments.add(new Segment(Vector3f.ZERO.clone(), Vector3f.UNIT_Z.clone()));

        List<Segment> aux = new ArrayList<>();

        Vector3f perpendicular = new Vector3f();
        Vector3f direction = new Vector3f();
        Quaternion rotation = new Quaternion();

        for (int gen = 0; gen < generations; gen++) {
            aux.addAll(segments);
            segments.clear();
            for (Segment seg : aux) {
                Vector3f mid = new Vector3f();
                seg.second.subtract(seg.first, direction).normalizeLocal();
                FastMath.interpolateLinear(0.5f, seg.first, seg.second, mid);
                perpendicular.set(direction);
                
                float tempX = perpendicular.x;
                perpendicular.x = perpendicular.z;
                perpendicular.z = -tempX;

                // Now perpendicular IS perpendicular
                perpendicular.multLocal(
                        (FastMath.rand.nextFloat() - 0.5f) * 2f * jitter);
                mid.addLocal(perpendicular);
                              
                Segment begin = new Segment(seg.first, mid);
                segments.add(begin);
                segments.add(new Segment(mid, seg.second));
                
                // Branch. Prevent branching inside branches
                if (FastMath.rand.nextFloat() 
                        < branchProbability && begin.strength == 1f) {
                    float branchLength = seg.first.distance(mid) * 0.7f;
                    float angle = FastMath.rand.nextFloat() 
                            * FastMath.QUARTER_PI * 0.7f;
                    rotation.fromAngles(0f, angle, 0f);
                    rotation.multLocal(direction);
                    Segment branch = new Segment(mid,
                            mid.add(direction.multLocal(branchLength)));
                    branch.strength = 0.7f * begin.strength;
                    segments.add(branch);
                }
            }

            aux.clear();
        }

        return segments;
    }

    // TODO: Remove this when the current lightning implementation proves
    // itself ready
    private static Mesh createMesh(List<Segment> segments) {
        List<Geometry> geoms = new ArrayList<>();
        for (Segment segment : segments) {      
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

    public static Mesh createGeometry(float jitter, float widthFactor,
            float branchProbability) {
        List<Segment> segments = generateSegments(jitter, branchProbability);
        
        Map<Vector3f, SegmentBufferData> points = new IdentityHashMap<>();
        
        Mesh mesh = new Mesh();

        Segment first = segments.get(0);

        SegmentBufferData firstSbData = createSegmentBufferData(
                first.first, first.second, widthFactor, points);

        Vector3f[] positions = new Vector3f[(segments.size() + 1) * 2];
        
        firstSbData.leftIndex = 0;
        firstSbData.rightIndex = 1;
        positions[firstSbData.leftIndex] = firstSbData.leftPos;
        positions[firstSbData.rightIndex] = firstSbData.rightPos;

        short posIdx = 2;

        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            Vector3f directionGiver = segment.second.subtract(segment.first)
                    .normalizeLocal().addLocal(segment.second);
            SegmentBufferData sbData = createSegmentBufferData(
                    segment.second, directionGiver, widthFactor, points);

            sbData.leftIndex = posIdx++;
            positions[sbData.leftIndex] = sbData.leftPos;
            sbData.rightIndex = posIdx++;
            positions[sbData.rightIndex] = sbData.rightPos;
        }

        Vector2f[] textureCoords = new Vector2f[positions.length];
        for (int i = 0; i < textureCoords.length; ++i) {
            float yTexCoord = positions[i].z;
            float xTexCoord = positions[i].x;
            textureCoords[i] = new Vector2f(xTexCoord, yTexCoord);
        }
        
        short[] indices = new short[segments.size() * 6];
        short indexIdx = 0;
        
        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            
            SegmentBufferData start = points.get(segment.first);
            SegmentBufferData end = points.get(segment.second);

            indices[indexIdx++] = end.leftIndex;
            indices[indexIdx++] = start.leftIndex;
            indices[indexIdx++] = start.rightIndex;
            
            indices[indexIdx++] = start.rightIndex;
            indices[indexIdx++] = end.rightIndex;
            indices[indexIdx++] = end.leftIndex;
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
            Vector3f first, Vector3f second, float widthFactor,
            Map<Vector3f, SegmentBufferData> existingData) {
        SegmentBufferData data = existingData.get(first);
        if (data != null) {
            return data;
        }
        
        data = new SegmentBufferData();
        data.leftPos = new Vector3f();
        data.rightPos = new Vector3f();
        
        TempVars vars = TempVars.get();
        Vector3f dir = vars.vect1;
        Vector3f perpendicular = vars.vect2;

        second.subtract(first, dir).normalizeLocal();
        perpendicular.set(dir);

        float tempX = perpendicular.x;
        perpendicular.x = perpendicular.z;
        perpendicular.z = -tempX;
        perpendicular.multLocal(widthFactor);

        first.add(perpendicular, data.leftPos);
        first.add(perpendicular.negateLocal(), data.rightPos);
        vars.release();

        existingData.put(first, data);
        return data;
    }
}

class SegmentBufferData {

    public Vector3f leftPos;
    public Vector3f rightPos;
    public short leftIndex;
    public short rightIndex;
    public Vector3f leftBitangent = new Vector3f();
    public Vector3f rightBitangent = new Vector3f();
}

class Segment {

    Vector3f first;
    Vector3f second;
    float strength = 1f;

    public Segment(Vector3f first, Vector3f second) {
        this.first = first;
        this.second = second;
    }
}
