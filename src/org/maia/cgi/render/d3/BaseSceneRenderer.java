package org.maia.cgi.render.d3;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.view.ViewPort;

public abstract class BaseSceneRenderer implements SceneRenderer {

	private Collection<SceneRendererProgressTracker> progressTrackers;

	protected BaseSceneRenderer() {
		this.progressTrackers = new Vector<SceneRendererProgressTracker>();
	}

	@Override
	public final void render(Scene scene, ViewPort output, RenderOptions options) {
		render(scene, Collections.singleton(output), options);
	}

	@Override
	public final void render(Scene scene, Collection<ViewPort> outputs, RenderOptions options) {
		for (ViewPort output : outputs) {
			output.startRendering();
			output.clear();
		}
		for (SceneRendererProgressTracker tracker : getProgressTrackers()) {
			tracker.renderingStarted(this, scene);
		}
		renderImpl(scene, outputs, options);
		for (ViewPort output : outputs) {
			output.stopRendering();
		}
		for (SceneRendererProgressTracker tracker : getProgressTrackers()) {
			tracker.renderingCompleted(this, scene);
		}
	}

	protected abstract void renderImpl(Scene scene, Collection<ViewPort> outputs, RenderOptions options);

	protected void fireRenderingProgressUpdate(Scene scene, int totalSteps, int stepIndex, double stepProgress,
			String stepLabel) {
		for (SceneRendererProgressTracker tracker : getProgressTrackers()) {
			tracker.renderingProgressUpdate(this, scene, totalSteps, stepIndex, stepProgress, stepLabel);
		}
	}

	@Override
	public void addProgressTracker(SceneRendererProgressTracker tracker) {
		getProgressTrackers().add(tracker);
	}

	@Override
	public void removeProgressTracker(SceneRendererProgressTracker tracker) {
		getProgressTrackers().remove(tracker);
	}

	protected Collection<SceneRendererProgressTracker> getProgressTrackers() {
		return progressTrackers;
	}

}