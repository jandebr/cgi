package org.maia.cgi.model.d3.scene;

import java.util.Collection;

import org.maia.cgi.compose.d3.DepthBlurParameters;
import org.maia.cgi.compose.d3.DepthFunction;
import org.maia.cgi.compose.d3.SigmoidDepthFunction;
import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.camera.PerspectiveViewVolume;
import org.maia.cgi.model.d3.camera.RevolvingCamera;
import org.maia.cgi.model.d3.camera.RevolvingCameraImpl;
import org.maia.cgi.model.d3.camera.ViewVolume;
import org.maia.cgi.model.d3.light.LightSource;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.render.d3.RenderOptions;
import org.maia.cgi.render.d3.view.ColorDepthBuffer;

public abstract class SceneBuilder {

	protected SceneBuilder() {
	}

	public RenderOptions getDefaultRenderOptions() {
		return RenderOptions.createDefaultOptions();
	}

	public Scene build(RenderOptions options) {
		Scene scene = createEmptyScene(options);
		for (Object3D object : createTopLevelObjects(options)) {
			scene.addTopLevelObject(object);
		}
		for (LightSource light : createLightSources(scene, options)) {
			scene.addLightSource(light);
		}
		scene.setBackdrop(createBackdrop(scene, options));
		scene.setDarknessDepthFunction(createDarknessDepthFunction(scene, options));
		scene.setDepthBlurParameters(createDepthBlurParameters(scene, options));
		return scene;
	}

	protected Scene createEmptyScene(RenderOptions options) {
		return new Scene(getSceneName(), createCamera(options));
	}

	protected abstract String getSceneName();

	protected abstract Camera createCamera(RenderOptions options);

	protected abstract Collection<Object3D> createTopLevelObjects(RenderOptions options);

	protected abstract Collection<LightSource> createLightSources(Scene scene, RenderOptions options);

	protected ColorDepthBuffer createBackdrop(Scene scene, RenderOptions options) {
		return null;
	}

	protected DepthFunction createDarknessDepthFunction(Scene scene, RenderOptions options) {
		Box3D bbox = scene.getBoundingBoxInCameraCoordinates();
		double nearDepth = -bbox.getZ2();
		double farDepth = -bbox.getZ1();
		return SigmoidDepthFunction.createFilter(nearDepth, farDepth, 0.6, 0.4);
	}

	protected DepthBlurParameters createDepthBlurParameters(Scene scene, RenderOptions options) {
		return new DepthBlurParameters(0.6, 1.0);
	}

	protected RevolvingCamera createRevolvingCamera(Point3D pivotPoint, Point3D position, double viewAngleInDegrees,
			double aspectRatio, double viewPlaneNegativeZ, double farPlaneNegativeZ) {
		ViewVolume viewVolume = PerspectiveViewVolume.createFromParameters(viewAngleInDegrees, aspectRatio,
				viewPlaneNegativeZ, farPlaneNegativeZ);
		return new RevolvingCameraImpl(pivotPoint, position, viewVolume);
	}

}