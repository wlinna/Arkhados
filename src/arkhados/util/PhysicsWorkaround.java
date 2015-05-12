/*
 * Copyright (c) 2009-2012 jMonkeyEngine
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
package arkhados.util;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Iterator;
import java.util.List;

/**
 * Some PhysicsSpace code (with improvements) from jmonkeyengine git repo
 * because of flaws in 3.0 version of PhysicsSpace.
 */
public class PhysicsWorkaround {

    /**
     * adds an object to the physics space
     *
     * @param obj the PhysicsControl or Spatial with PhysicsControl to add
     */
    public static void add(PhysicsSpace space, Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Spatial) {
            Spatial node = (Spatial) obj;
            for (int i = 0; i < node.getNumControls(); i++) {
                if (node.getControl(i) instanceof PhysicsControl) {
                    space.add(((PhysicsControl) node.getControl(i)));
                }
            }
        } else {
            space.add(obj);
        }
    }

    /**
     * adds all physics controls and joints in the given spatial node to the
     * physics space (e.g. after loading from disk) - recursive if node
     *
     * @param spatial the rootnode containing the physics objects
     */
    public static void addAll(PhysicsSpace space, Spatial spatial) {
        add(space, spatial);
        if (spatial.getControl(RigidBodyControl.class) != null) {
            RigidBodyControl physicsNode =
                    spatial.getControl(RigidBodyControl.class);

            //add joints with physicsNode as BodyA
            List<PhysicsJoint> joints = physicsNode.getJoints();
            for (Iterator<PhysicsJoint> it = joints.iterator(); it.hasNext();) {
                PhysicsJoint physicsJoint = it.next();
                if (physicsNode.equals(physicsJoint.getBodyA())) {
                    //add(physicsJoint.getBodyB());
                    space.add(physicsJoint);
                }
            }
        }

        //recursion
        if (spatial instanceof Node) {
            List<Spatial> children = ((Node) spatial).getChildren();
            for (Iterator<Spatial> it = children.iterator(); it.hasNext();) {
                Spatial spat = it.next();
                addAll(space, spat);
            }
        }
    }

    /**
     * removes an object from the physics space
     *
     * @param obj the PhysicsControl or Spatial with PhysicsControl to remove
     */
    public static void remove(PhysicsSpace space, Object obj) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Spatial) {
            Spatial node = (Spatial) obj;
            for (int i = 0; i < node.getNumControls(); i++) {
                if (node.getControl(i) instanceof PhysicsControl) {
                    space.remove(((PhysicsControl) node.getControl(i)));
                }
            }
        } else {
            space.remove(obj);
        }
    }

    /**
     * Removes all physics controls and joints in the given spatial from the
     * physics space (e.g. before saving to disk) - recursive if node
     *
     * @param spatial the rootnode containing the physics objects
     */
    public static void removeAll(PhysicsSpace space, Spatial spatial) {
        if (spatial.getControl(RigidBodyControl.class) != null) {
            RigidBodyControl physicsNode =
                    spatial.getControl(RigidBodyControl.class);
            //remove joints with physicsNode as BodyA
            List<PhysicsJoint> joints = physicsNode.getJoints();
            for (Iterator<PhysicsJoint> it = joints.iterator(); it.hasNext();) {
                PhysicsJoint physicsJoint = it.next();
                if (physicsNode.equals(physicsJoint.getBodyA())) {
                    space.remove(physicsJoint);
                    //remove(physicsJoint.getBodyB());
                }
            }
        }

        remove(space, spatial);

        //recursion
        if (spatial instanceof Node) {
            List<Spatial> children = ((Node) spatial).getChildren();
            for (Iterator<Spatial> it = children.iterator(); it.hasNext();) {
                Spatial spat = it.next();
                removeAll(space, spat);
            }
        }
    }
}
