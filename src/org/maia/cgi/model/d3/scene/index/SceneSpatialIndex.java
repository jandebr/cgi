package org.maia.cgi.model.d3.scene.index;

import java.util.Iterator;

import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.render.d3.ReusableObjectPack;

/**
 * 3D index of a <code>Scene</code>'s objects in camera coordinates
 * 
 * <p>
 * The spatial index is constructed based on the current positions and orientations of the objects in the scene and the
 * camera. It is the responsability of the client code to create a new index to reflect an updated snapshot of that
 * scene.
 * </p>
 */
public interface SceneSpatialIndex extends SceneIndex {

	/**
	 * Returns the scene objects that intersect with the given line segment
	 * 
	 * @param line
	 *            The line segment, in camera coordinates. The segment is assumed to be <em>closed</em> on both ends
	 *            <em>and</em> the first point {@link LineSegment3D#getP1()} is assumed to lie within the scene's
	 *            bounding box, in camera coordinates
	 * @param reusableObjects
	 *            Objects that can be reused in the context of the current thread
	 * @return An iterator over the scene objects intersecting with <code>line</code>. The order of the objects is
	 *         undefined
	 */
	Iterator<ObjectSurfacePoint3D> getObjectIntersections(LineSegment3D line, ReusableObjectPack reusableObjects);

}