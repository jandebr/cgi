package org.maia.cgi.gui.d3.renderer;

import org.maia.cgi.compose.d3.DepthBlurParameters;
import org.maia.cgi.gui.d3.renderer.RenderOptions.RenderMode;
import org.maia.cgi.model.d3.scene.Scene;
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
		return new WireframeRenderer(options.getWireframeColorNear(), options.getWireframeColorFar());
	}

	protected SceneRenderer createRealisticSceneRenderer(Scene scene, RenderOptions options) {
		scene.getRenderParameters().setShadowsEnabled(options.isShadowsEnabled());
		scene.getRenderParameters().setBackdropEnabled(options.isBackdropEnabled());
		int spp = options.isSuperSamplingEnabled() ? 3 : 1; // samples per pixel (in either direction)
		DepthBlurParameters depthBlur = options.isDepthBlurEnabled() ? scene.getRenderParameters()
				.getDepthBlurParameters() : null;
		return new RaytraceRenderer(options.getRenderWidth(), options.getRenderHeight(), spp, spp, depthBlur);
	}

}