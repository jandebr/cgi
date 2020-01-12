package org.maia.cgi.model.d3.light;

import org.maia.cgi.model.d3.camera.CameraObserver;

public interface LightSource extends CameraObserver {

	/**
	 * The brightness of this light source
	 * 
	 * @return The brightness, ranging from 0 (dark, no light) to 1 (maximum brightness)
	 */
	double getBrightness();

}