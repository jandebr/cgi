package org.maia.cgi.render.d3;

import java.util.Collection;

import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.view.ViewPort;

public interface SceneRenderer {

	void render(Scene scene, ViewPort output);

	void render(Scene scene, Collection<ViewPort> outputs);

	void addProgressTracker(SceneRendererProgressTracker tracker);
	
	void removeProgressTracker(SceneRendererProgressTracker tracker);
	
}
