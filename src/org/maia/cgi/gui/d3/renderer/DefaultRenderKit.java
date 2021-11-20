package org.maia.cgi.gui.d3.renderer;

import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.RenderOptions;
import org.maia.cgi.render.d3.RenderOptions.RenderMode;
import org.maia.cgi.render.d3.SceneRenderer;
import org.maia.cgi.render.d3.raytrace.RaytraceRenderer;
import org.maia.cgi.render.d3.wireframe.WireframeRenderer;

public class DefaultRenderKit implements RenderKit {

	public DefaultRenderKit() {
	}

	@Override
	public SceneRenderer createRenderer(Scene scene, RenderOptions options) {
		SceneRenderer renderer = null;
		if (RenderMode.PROTOTYPE.equals(options.getRenderMode())) {
			renderer = createPrototypeSceneRenderer(scene, options);
		} else if (RenderMode.REALISTIC.equals(options.getRenderMode())) {
			renderer = createRealisticSceneRenderer(scene, options);
		}
		return renderer;
	}

	protected SceneRenderer createPrototypeSceneRenderer(Scene scene, RenderOptions options) {
		return new WireframeRenderer();
	}

	protected SceneRenderer createRealisticSceneRenderer(Scene scene, RenderOptions options) {
		return new RaytraceRenderer();
	}

}