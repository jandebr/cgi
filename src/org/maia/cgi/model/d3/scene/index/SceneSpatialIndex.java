package org.maia.cgi.model.d3.scene.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.maia.cgi.MemorizedCompute;
import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.model.d3.scene.SceneUtils;

public abstract class SceneSpatialIndex implements MemorizedCompute {

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

	protected Collection<Object3D> getIndexedObjects() {
		Collection<Object3D> sceneObjects = SceneUtils.getAllIndividualObjectsInScene(getScene());
		Collection<Object3D> indexedObjects = new Vector<Object3D>(sceneObjects.size());
		Box3D sceneBox = getSceneBox();
		for (Object3D object : sceneObjects) {
			boolean overlaps = true;
			if (object.isBounded()) {
				Box3D objectBox = getObjectBox(object);
				overlaps = objectBox != null && objectBox.overlaps(sceneBox);
			}
			if (overlaps) {
				indexedObjects.add(object);
			}
		}
		return indexedObjects;
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

}