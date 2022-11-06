package org.maia.cgi.render.d3;

import java.awt.Color;

public class RenderOptions {

	private RenderMode renderMode;

	private SamplingMode samplingMode;

	private int renderWidth;

	private int renderHeight;

	private boolean shadowsEnabled;

	private boolean backdropEnabled;

	private boolean depthBlurEnabled;

	private boolean depthDarknessEnabled;

	private Color sceneBackgroundColor;

	private Color wireframeColorNear;

	private Color wireframeColorFar;

	private int numberOfRenderThreads;

	private static final String PROPERTY_RENDER_THREADS = "renderThreads";

	private RenderOptions() {
	}

	public static RenderOptions createDefaultOptions() {
		RenderOptions options = new RenderOptions();
		options.setRenderMode(RenderMode.PROTOTYPE);
		options.setSamplingMode(SamplingMode.DIRECT);
		options.setRenderWidth(1280);
		options.setRenderHeight(720);
		options.setShadowsEnabled(false);
		options.setBackdropEnabled(false);
		options.setDepthBlurEnabled(false);
		options.setDepthDarknessEnabled(false);
		options.setSceneBackgroundColor(Color.WHITE);
		options.setWireframeColorNear(Color.BLACK);
		options.setWireframeColorFar(Color.LIGHT_GRAY);
		options.setNumberOfRenderThreads(Integer.parseInt(System.getProperty(PROPERTY_RENDER_THREADS, "1")));
		return options;
	}

	public double getAspectRatio() {
		return getRenderWidth() / (double) getRenderHeight();
	}

	public RenderMode getRenderMode() {
		return renderMode;
	}

	public void setRenderMode(RenderMode mode) {
		this.renderMode = mode;
	}

	public SamplingMode getSamplingMode() {
		return samplingMode;
	}

	public void setSamplingMode(SamplingMode samplingMode) {
		this.samplingMode = samplingMode;
	}

	public int getRenderWidth() {
		return renderWidth;
	}

	public void setRenderWidth(int width) {
		this.renderWidth = width;
	}

	public int getRenderHeight() {
		return renderHeight;
	}

	public void setRenderHeight(int height) {
		this.renderHeight = height;
	}

	public boolean isShadowsEnabled() {
		return shadowsEnabled;
	}

	public void setShadowsEnabled(boolean shadowsEnabled) {
		this.shadowsEnabled = shadowsEnabled;
	}

	public boolean isBackdropEnabled() {
		return backdropEnabled;
	}

	public void setBackdropEnabled(boolean backdropEnabled) {
		this.backdropEnabled = backdropEnabled;
	}

	public boolean isDepthBlurEnabled() {
		return depthBlurEnabled;
	}

	public void setDepthBlurEnabled(boolean enabled) {
		this.depthBlurEnabled = enabled;
	}

	public boolean isDepthDarknessEnabled() {
		return depthDarknessEnabled;
	}

	public void setDepthDarknessEnabled(boolean depthDarknessEnabled) {
		this.depthDarknessEnabled = depthDarknessEnabled;
	}

	public Color getSceneBackgroundColor() {
		return sceneBackgroundColor;
	}

	public void setSceneBackgroundColor(Color sceneBackgroundColor) {
		this.sceneBackgroundColor = sceneBackgroundColor;
	}

	public Color getWireframeColorNear() {
		return wireframeColorNear;
	}

	public void setWireframeColorNear(Color wireframeColorNear) {
		this.wireframeColorNear = wireframeColorNear;
	}

	public Color getWireframeColorFar() {
		return wireframeColorFar;
	}

	public void setWireframeColorFar(Color wireframeColorFar) {
		this.wireframeColorFar = wireframeColorFar;
	}

	public int getSafeNumberOfRenderThreads() {
		int cores = Runtime.getRuntime().availableProcessors();
		return Math.max(Math.min(getNumberOfRenderThreads(), cores), 1);
	}

	public int getNumberOfRenderThreads() {
		return numberOfRenderThreads;
	}

	public void setNumberOfRenderThreads(int numberOfRenderThreads) {
		this.numberOfRenderThreads = numberOfRenderThreads;
	}

	public static enum RenderMode {

		PROTOTYPE,

		REALISTIC;

	}

	public static enum SamplingMode {

		DIRECT(1, 1),

		SUPER(2, 2),

		ULTRA(3, 3);

		private int samplesPerPixelX;

		private int samplesPerPixelY;

		private SamplingMode(int samplesPerPixelX, int samplesPerPixelY) {
			this.samplesPerPixelX = samplesPerPixelX;
			this.samplesPerPixelY = samplesPerPixelY;
		}

		public int getSamplesPerPixelX() {
			return samplesPerPixelX;
		}

		public int getSamplesPerPixelY() {
			return samplesPerPixelY;
		}

	}

}