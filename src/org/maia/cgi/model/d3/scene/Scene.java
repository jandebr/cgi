package org.maia.cgi.model.d3.scene;

import java.util.Collection;
import java.util.Vector;

import org.maia.cgi.Memoise;
import org.maia.cgi.Metrics;
import org.maia.cgi.compose.d3.DepthBlurParameters;
import org.maia.cgi.compose.d3.DepthFunction;
import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.camera.CameraObserver;
import org.maia.cgi.model.d3.light.LightSource;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.scene.index.SceneSpatialIndex;
import org.maia.cgi.model.d3.scene.index.SceneSpatialIndexFactory;
import org.maia.cgi.model.d3.scene.index.SceneViewPlaneIndex;
import org.maia.cgi.render.d3.view.ColorDepthBuffer;

public class Scene implements CameraObserver, Memoise {

	/**
	 * A descriptive name for the scene.
	 */
	private String name;

	/**
	 * A scene always has one (main) camera. The camera can be switched at any given time so to quickly change viewpoint
	 * for instance.
	 */
	private Camera camera;

	private Collection<Object3D> topLevelObjects = new Vector<Object3D>(100);

	private Collection<LightSource> lightSources = new Vector<LightSource>();

	private Box3D boundingBoxInObjectCoordinates; // cached bounding box

	private Box3D boundingBoxInWorldCoordinates; // cached bounding box

	private Box3D boundingBoxInCameraCoordinates; // cached bounding box

	private Box3D boundingBoxInViewVolumeCoordinates; // cached bounding box

	private double distanceOutsideScene = -1.0;

	private SceneSpatialIndex spatialIndex;

	private SceneViewPlaneIndex viewPlaneIndex;

	private ColorDepthBuffer backdrop;

	private DepthFunction darknessDepthFunction;

	private DepthBlurParameters depthBlurParameters;

	public Scene(Camera camera) {
		this(null, camera);
	}

	public Scene(String name, Camera camera) {
		this.name = name;
		if (camera == null)
			throw new NullPointerException("A scene must always have a main camera");
		changeCamera(camera);
	}

	public void addTopLevelObject(Object3D object) {
		invalidateBoundingBoxes();
		invalidateSpatialIndices();
		getTopLevelObjects().add(object);
		object.cameraHasChanged(getCamera());
	}

	public void addLightSource(LightSource lightSource) {
		getLightSources().add(lightSource);
		lightSource.cameraHasChanged(getCamera());
	}

	public void changeCamera(Camera camera) {
		if (getCamera() != null) {
			getCamera().removeObserver(this);
		}
		camera.addObserver(this);
		setCamera(camera);
		cameraHasChanged(camera);
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		invalidateCameraBoundingBox();
		invalidateSpatialIndices();
		// Objects
		for (Object3D object : getTopLevelObjects()) {
			object.cameraHasChanged(camera);
		}
		// Light sources
		for (LightSource light : getLightSources()) {
			light.cameraHasChanged(camera);
		}
	}

	@Override
	public void releaseMemory() {
		invalidateSpatialIndices();
		for (Object3D object : getTopLevelObjects()) {
			object.releaseMemory();
		}
	}

	public Box3D getBoundingBoxInObjectCoordinates() {
		if (boundingBoxInObjectCoordinates == null) {
			boundingBoxInObjectCoordinates = deriveBoundingBox(CoordinateFrame.OBJECT);
		}
		return boundingBoxInObjectCoordinates;
	}

	public Box3D getBoundingBoxInWorldCoordinates() {
		if (boundingBoxInWorldCoordinates == null) {
			boundingBoxInWorldCoordinates = deriveBoundingBox(CoordinateFrame.WORLD);
		}
		return boundingBoxInWorldCoordinates;
	}

	public Box3D getBoundingBoxInCameraCoordinates() {
		if (boundingBoxInCameraCoordinates == null) {
			boundingBoxInCameraCoordinates = deriveBoundingBox(CoordinateFrame.CAMERA);
		}
		return boundingBoxInCameraCoordinates;
	}

	public Box3D getBoundingBoxInViewVolumeCoordinates() {
		if (boundingBoxInViewVolumeCoordinates == null) {
			boundingBoxInViewVolumeCoordinates = deriveBoundingBox(CoordinateFrame.VIEWVOLUME);
		}
		return boundingBoxInViewVolumeCoordinates;
	}

	private Box3D deriveBoundingBox(CoordinateFrame cframe) {
		Metrics.getInstance().incrementBoundingBoxComputations();
		Box3D bbox = null;
		for (Object3D object : getTopLevelObjects()) {
			if (object.isBounded()) {
				Box3D objectBox = object.asBoundedObject().getBoundingBox(cframe, getCamera());
				if (bbox == null) {
					bbox = objectBox.clone();
				} else {
					bbox.expandToContain(objectBox);
				}
			}
		}
		return bbox;
	}

	private void invalidateBoundingBoxes() {
		boundingBoxInObjectCoordinates = null;
		boundingBoxInWorldCoordinates = null;
		invalidateCameraBoundingBox();
		distanceOutsideScene = -1.0;
	}

	private void invalidateCameraBoundingBox() {
		boundingBoxInCameraCoordinates = null;
		boundingBoxInViewVolumeCoordinates = null;
	}

	private void invalidateSpatialIndices() {
		spatialIndex = null;
		viewPlaneIndex = null;
	}

	public double getDistanceOutsideScene() {
		if (distanceOutsideScene < 0) {
			Box3D bbox = getBoundingBoxInWorldCoordinates();
			distanceOutsideScene = 2.0 * Math.max(bbox.getDepth(), Math.max(bbox.getWidth(), bbox.getHeight()));
		}
		return distanceOutsideScene;
	}

	public SceneSpatialIndex getSpatialIndex() {
		if (spatialIndex == null) {
			spatialIndex = SceneSpatialIndexFactory.getInstance().createSpatialIndex(this);
		}
		return spatialIndex;
	}

	public SceneViewPlaneIndex getViewPlaneIndex() {
		if (viewPlaneIndex == null) {
			viewPlaneIndex = SceneSpatialIndexFactory.getInstance().createViewPlaneIndex(this);
		}
		return viewPlaneIndex;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Camera getCamera() {
		return camera;
	}

	private void setCamera(Camera camera) {
		this.camera = camera;
	}

	public Collection<Object3D> getTopLevelObjects() {
		return topLevelObjects;
	}

	public Collection<LightSource> getLightSources() {
		return lightSources;
	}

	public ColorDepthBuffer getBackdrop() {
		return backdrop;
	}

	public void setBackdrop(ColorDepthBuffer backdrop) {
		this.backdrop = backdrop;
	}

	public DepthFunction getDarknessDepthFunction() {
		return darknessDepthFunction;
	}

	public void setDarknessDepthFunction(DepthFunction darknessDepthFunction) {
		this.darknessDepthFunction = darknessDepthFunction;
	}

	public DepthBlurParameters getDepthBlurParameters() {
		return depthBlurParameters;
	}

	public void setDepthBlurParameters(DepthBlurParameters depthBlurParameters) {
		this.depthBlurParameters = depthBlurParameters;
	}

}