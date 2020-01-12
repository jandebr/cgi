package org.maia.cgi.model.d3.light;

import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.scene.Scene;

public interface DirectionalLightSource extends LightSource {

	/**
	 * The direction in which light from this light source radiates out in space
	 * 
	 * @return The direction of this light source, represented in the <em>camera</em> coordinate frame
	 * @see CoordinateFrame
	 */
	Vector3D getDirection();

	/**
	 * Returns a scaled direction which is guaranteed to point out of the scene for any position inside the scene
	 * 
	 * @param scene
	 *            The scene
	 * @return A scaled direction of this light source
	 */
	Vector3D getScaledDirectionOutsideOfScene(Scene scene);

}