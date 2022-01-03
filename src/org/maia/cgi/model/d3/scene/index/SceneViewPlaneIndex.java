package org.maia.cgi.model.d3.scene.index;

import java.util.Iterator;

import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.RaytraceableObject3D;
import org.maia.cgi.render.d3.ReusableObjectPack;

/**
 * 2D index of a <code>Scene</code>'s objects projected to the scene's view plane
 * 
 * <p>
 * The index is constructed based on the current positions and orientations of the objects in the scene and the camera.
 * It is the responsability of the client code to create a new index to reflect an updated snapshot of that scene.
 * </p>
 */
public interface SceneViewPlaneIndex extends SceneIndex {

	/**
	 * Returns all the scene objects that <em>potentially</em> project onto the specified point on the view plane
	 * <p>
	 * To determine which (raytraceable) objects hit the <code>point</code>, the method
	 * {@link RaytraceableObject3D#intersectWithEyeRay} can be used, passing a line segment (the "ray") which offsets in
	 * the camera position (the "eye") and passes through the <code>point</code> on the view plane.
	 * </p>
	 * 
	 * @param pointOnViewPlane
	 *            A point on the view plane
	 * @param reusableObjects
	 *            Objects that can be reused in the context of the current thread
	 * @return An iterator over a filtered list of scene objects
	 */
	Iterator<Object3D> getViewPlaneObjects(Point3D pointOnViewPlane, ReusableObjectPack reusableObjects);

}