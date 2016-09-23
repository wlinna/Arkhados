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
package arkhados.debug;

import arkhados.Globals;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ChangeCharacaterMaterial {

    int currentIndex = 0;

    final Material[] materials = new Material[]{
//        Globals.assets.loadMaterial("TestMaterials/Electricity/electricity1.j3m"),
//        Globals.assets.loadMaterial("TestMaterials/Electricity/electricity2_2.j3m"),
//        Globals.assets.loadMaterial("TestMaterials/Electricity/electricity2.j3m"),
//        Globals.assets.loadMaterial("TestMaterials/Electricity/electricity3_line1.j3m"),
//        Globals.assets.loadMaterial("TestMaterials/Electricity/electricity3_line2.j3m"),
//        Globals.assets.loadMaterial("TestMaterials/Electricity/electricity3_line3.j3m"),
//        Globals.assets.loadMaterial("TestMaterials/Electricity/electricity4.j3m"),
//        Globals.assets.loadMaterial("TestMaterials/Electricity/electricity5_2.j3m")
    };

    public Node node;

    public void next() {
        if (node == null || materials.length == 0) {
            return;
        }

        currentIndex = (currentIndex + 1) % materials.length;
        Material mat = materials[currentIndex];
        System.out.println("Material name: " + mat.getAssetName());
        for (Spatial child : node.getChildren()) {
            if (!(child instanceof Geometry)) {
                continue;
            }
                        
            Geometry geom = (Geometry) child;
            
            if (geom.getName().startsWith("electrified")) {
                geom.setMaterial(mat);
            }
        }
    }
}
