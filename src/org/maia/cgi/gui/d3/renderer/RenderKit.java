package org.maia.cgi.gui.d3.renderer;

import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.SceneRenderer;

public interface RenderKit {

	SceneRenderer createRenderer(Scene scene, RenderOptions options);

}