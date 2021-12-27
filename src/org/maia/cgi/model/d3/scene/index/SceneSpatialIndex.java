package org.maia.cgi.model.d3.scene.index;

import java.util.Collection;
import java.util.Iterator;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.model.d3.scene.SceneUtils;

public abstract class SceneSpatialIndex {

	private Scene scene;

	protected SceneSpatialIndex(Scene scene) {
		this.scene = scene;
	}

	public abstract void buildIndex();

	public abstract void dispose();

	/**
	 * Returns the scene objects that intersect with the given line segment
	 * 
	 * @param line
	 *            The line segment, in camera coordinates. The segment is assumed to be <i>closed</i> on both ends AND
	 *            the first point {@link LineSegment3D#getP1()} is assumed to lie within the scene's bounding box, in
	 *            camera coordinates
	 * @return An iterator over the scene objects intersecting with <code>line</code>. The order of the objects is
	 *         undefined
	 */
	public abstract Iterator<ObjectSurfacePoint3D> getObjectIntersections(LineSegment3D line);

	public Scene getScene() {
		return scene;
	}

	protected Box3D getSceneBox() {
		return getScene().getBoundingBox(CoordinateFrame.CAMERA);
	}

	protected Box3D getObjectBox(Object3D object) {
		Box3D box = null;
		if (object.isBounded()) {
			box = object.asBoundedObject().getBoundingBox(CoordinateFrame.CAMERA, getScene().getCamera());
		}
		return box;
	}

	protected Collection<Object3D> getSceneObjects() {
		return SceneUtils.getAllIndividualObjectsInScene(getScene());
	}

}