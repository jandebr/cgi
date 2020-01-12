package org.maia.cgi.model.d3.scene;

import java.awt.Color;

import org.maia.cgi.compose.d3.DepthBlurParameters;
import org.maia.cgi.compose.d3.DepthFunction;

public class SceneRenderParameters {

	private DepthFunction darknessDepthFunction;

	private DepthBlurParameters depthBlurParameters;

	private boolean shadowsEnabled;

	private Color ambientColor;

	public SceneRenderParameters() {
	}

	public DepthFunction getDarknessDepthFunction() {
		return darknessDepthFunction;
	}

	public void setDarknessDepthFunction(DepthFunction darknessDepthFunction) {
		this.darknessDepthFunction = darknessDepthFunction;
	}

	public DepthBlurParameters getDepthBlurParameters() {
		return depthBlurParameters;
	}

	public void setDepthBlurParameters(DepthBlurParameters depthBlurParameters) {
		this.depthBlurParameters = depthBlurParameters;
	}

	public boolean isShadowsEnabled() {
		return shadowsEnabled;
	}

	public void setShadowsEnabled(boolean shadowsEnabled) {
		this.shadowsEnabled = shadowsEnabled;
	}

	public Color getAmbientColor() {
		return ambientColor;
	}

	public void setAmbientColor(Color ambientColor) {
		this.ambientColor = ambientColor;
	}

}