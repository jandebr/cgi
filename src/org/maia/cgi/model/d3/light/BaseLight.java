package org.maia.cgi.model.d3.light;

import org.maia.cgi.model.d3.camera.Camera;

public abstract class BaseLight implements LightSource {

	private double brightness;

	protected BaseLight(double brightness) {
		this.brightness = brightness;
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		// Subclasses may override this method
	}

	@Override
	public double getBrightness() {
		return brightness;
	}

	@Override
	public boolean isPositional() {
		// Subclasses may override this method
		return false;
	}

	@Override
	public boolean isDirectional() {
		// Subclasses may override this method
		return false;
	}

}