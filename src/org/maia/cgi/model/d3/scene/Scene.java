package org.maia.cgi.model.d3.scene;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.metrics.Metrics;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.camera.CameraObserver;
import org.maia.cgi.model.d3.light.LightSource;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.render.d3.view.ColorDepthBuffer;

public class Scene implements CameraObserver {

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

	private Map<CoordinateFrame, Box3D> boundingBoxes = new HashMap<CoordinateFrame, Box3D>(5);

	private double distanceOutsideScene = -1.0;

	private SceneSpatialIndex spatialIndex;

	private ColorDepthBuffer backdrop;

	private SceneRenderParameters renderParameters;

	public Scene(Camera camera) {
		this(null, camera);
	}

	public Scene(String name, Camera camera) {
		this.name = name;
		this.renderParameters = new SceneRenderParameters();
		if (camera == null)
			throw new NullPointerException("A scene must always have a main camera");
		changeCamera(camera);
	}

	public void addTopLevelObject(Object3D object) {
		invalidateBoundingBoxes();
		invalidateSpatialIndex();
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
		invalidateSpatialIndex();
		// Objects
		for (Object3D object : getTopLevelObjects()) {
			object.cameraHasChanged(camera);
		}
		// Light sources
		for (LightSource light : getLightSources()) {
			light.cameraHasChanged(camera);
		}
	}

	public Box3D getBoundingBox(CoordinateFrame cframe) {
		Box3D bbox = boundingBoxes.get(cframe);
		if (bbox == null) {
			bbox = deriveBoundingBox(cframe);
			boundingBoxes.put(cframe, bbox);
		}
		return bbox;
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
		boundingBoxes.clear();
		distanceOutsideScene = -1.0;
	}

	private void invalidateCameraBoundingBox() {
		boundingBoxes.remove(CoordinateFrame.CAMERA);
	}

	private void invalidateSpatialIndex() {
		spatialIndex = null;
	}

	public double getDistanceOutsideScene() {
		if (distanceOutsideScene < 0) {
			Box3D bbox = getBoundingBox(CoordinateFrame.WORLD);
			distanceOutsideScene = 2.0 * Math.max(bbox.getDepth(), Math.max(bbox.getWidth(), bbox.getHeight()));
		}
		return distanceOutsideScene;
	}

	public SceneSpatialIndex getSpatialIndex() {
		if (spatialIndex == null) {
			spatialIndex = SceneSpatialIndex.createIndex(this);
		}
		return spatialIndex;
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

	public SceneRenderParameters getRenderParameters() {
		return renderParameters;
	}

}
