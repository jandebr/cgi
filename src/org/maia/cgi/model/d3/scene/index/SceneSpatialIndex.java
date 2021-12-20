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

	public abstract void dispose();

	public abstract Iterator<ObjectSurfacePoint3D> getObjectIntersections(LineSegment3D line);

	public Scene getScene() {
		return scene;
	}

	protected Box3D getSceneBox() {
		return getScene().getBoundingBox(CoordinateFrame.CAMERA);
	}

	protected Collection<Object3D> getSceneObjects() {
		return SceneUtils.getAllIndividualObjectsInScene(getScene());
	}

	protected Box3D getObjectBox(Object3D object) {
		Box3D box = null;
		if (object.isBounded()) {
			box = object.asBoundedObject().getBoundingBox(CoordinateFrame.CAMERA, getScene().getCamera());
		}
		return box;
	}

}