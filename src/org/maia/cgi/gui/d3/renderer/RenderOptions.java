package org.maia.cgi.gui.d3.renderer;

import java.awt.Color;

public class RenderOptions {

	private RenderMode renderMode;

	private int renderWidth;

	private int renderHeight;

	private boolean shadowsEnabled;

	private boolean backdropEnabled;

	private boolean superSamplingEnabled;

	private boolean depthBlurEnabled;

	private Color sceneBackgroundColor;

	private Color wireframeColorNear;

	private Color wireframeColorFar;

	private RenderOptions() {
	}

	public static RenderOptions createDefaultOptions() {
		RenderOptions options = new RenderOptions();
		options.setRenderMode(RenderMode.PROTOTYPE);
		options.setRenderWidth(1280);
		options.setRenderHeight(720);
		options.setShadowsEnabled(false);
		options.setBackdropEnabled(false);
		options.setSuperSamplingEnabled(false);
		options.setDepthBlurEnabled(false);
		options.setSceneBackgroundColor(Color.WHITE);
		options.setWireframeColorNear(Color.BLACK);
		options.setWireframeColorFar(Color.LIGHT_GRAY);
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

	public boolean isSuperSamplingEnabled() {
		return superSamplingEnabled;
	}

	public void setSuperSamplingEnabled(boolean enabled) {
		this.superSamplingEnabled = enabled;
	}

	public boolean isDepthBlurEnabled() {
		return depthBlurEnabled;
	}

	public void setDepthBlurEnabled(boolean enabled) {
		this.depthBlurEnabled = enabled;
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

	public static enum RenderMode {

		PROTOTYPE,

		REALISTIC;

	}
}