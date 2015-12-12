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

package arkhados.mesh;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Ilkka
 */
public class Grid extends Mesh {

    private int samplesX;
    private int samplesY;
    private float scale;
    AbstractHeightMap heightmap = null;

    public Grid(int rows, int columns, float scale) {
        updateGeometry(rows, columns, scale);
    }

    public Grid(AbstractHeightMap hm, float scale) {
        heightmap = hm;
        updateGeometry(hm.getSize(), hm.getSize(), scale);
    }

    private void updateGeometry(int samplesX, int samplesY, float scale) {
        this.samplesX = samplesX;
        this.samplesY = samplesY;
        this.scale = scale;
        Vector3f[] vertices = createVertices();
        Vector3f[] normals = createNormals();
        Vector2f[] texCoords = createTexCoords();
        int[] indices = createIndices();
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        setBuffer(Type.Index, 3, indices);

        updateBound();
        setStatic();
    }

    private Vector3f[] createNormals() {
        Vector3f[] result = new Vector3f[samplesX * samplesY];
        Arrays.fill(result, Vector3f.UNIT_Y);

        return result;
    }

    private Vector3f[] createVertices() {
        Vector3f[] result = new Vector3f[samplesX * samplesY];
        float[] map = null;
        if (heightmap != null) {
            map = heightmap.getHeightMap();

        }
        for (int y = 0; y < samplesY; y++) {
            for (int x = 0; x < samplesX; x++) {
                int index = x + y * samplesX;
                result[index] = new Vector3f(x * scale, 0, y * scale);
                if (map != null) {
                    result[index].y = heightmap.getScaledHeightAtPoint(x, y);
                }
            }
        }

        return result;
    }

    private Vector2f[] createTexCoords() {
        Vector2f[] result = new Vector2f[samplesX * samplesY];

        for (int y = 0; y < samplesY; y++) {
            for (int x = 0; x < samplesX; x++) {
                result[x + y * samplesX] = new Vector2f(x / (float) samplesX,
                        y / (float) samplesY);
            }
        }

        return result;
    }

    private int[] createIndices() {
        List<Integer> resultList = new ArrayList<>();

        for (int y = 0; y < samplesY - 1; y++) {
            for (int x = 0; x < samplesX - 1; x++) {
                int idx = x + y * samplesX;
                int down = idx + samplesX;
                int right = idx + 1;
                int diagonal = down + 1;

                resultList.add(idx);
                resultList.add(diagonal);
                resultList.add(right);

                resultList.add(idx);
                resultList.add(down);
                resultList.add(diagonal);
            }
        }
        
        int[] result = new int[resultList.size()];

        for (int i = 0; i < resultList.size(); ++i) {
            result[i] = resultList.get(i);
        }

        return result;
    }
}
