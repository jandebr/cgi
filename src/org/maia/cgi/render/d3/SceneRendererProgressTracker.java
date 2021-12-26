package org.maia.cgi.render.d3;

import org.maia.cgi.model.d3.scene.Scene;

public interface SceneRendererProgressTracker {

	void renderingStarted(SceneRenderer renderer, Scene scene);

	void renderingProgressUpdate(SceneRenderer renderer, Scene scene, int totalSteps, int stepIndex,
			double stepProgress, String stepLabel);

	void renderingCompleted(SceneRenderer renderer, Scene scene);

}
