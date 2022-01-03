package org.maia.cgi.model.d3.scene.index;

import java.util.Collection;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.model.d3.scene.SceneUtils;

public abstract class BaseSceneSpatialIndex implements SceneSpatialIndex {

	private Scene scene;

	protected BaseSceneSpatialIndex(Scene scene) {
		this.scene = scene;
	}

	@Override
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
		return getScene().getBoundingBoxInCameraCoordinates();
	}

	protected Box3D getObjectBox(Object3D object) {
		Box3D box = null;
		if (object.isBounded()) {
			box = object.asBoundedObject().getBoundingBoxInCameraCoordinates(getScene().getCamera());
		}
		return box;
	}

}