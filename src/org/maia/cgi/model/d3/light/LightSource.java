package org.maia.cgi.model.d3.light;

import org.maia.cgi.model.d3.camera.CameraObserver;

public interface LightSource extends CameraObserver {

	/**
	 * The brightness of this light source
	 * 
	 * @return The brightness, ranging from 0 (dark, no light) to 1 (maximum brightness)
	 */
	double getBrightness();

	/**
	 * Tells whether this light source is originating from one position in space
	 * 
	 * @return <code>true</code> iff this light source is an instance of <code>PositionalLightSource</code>
	 * @see {@link PositionalLightSource}
	 */
	boolean isPositional();

	/**
	 * Tells whether this light source radiates out in space in one direction
	 * 
	 * @return <code>true</code> iff this light source is an instance of <code>DirectionalLightSource</code>
	 * @see {@link DirectionalLightSource}
	 */
	boolean isDirectional();

}