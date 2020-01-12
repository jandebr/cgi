package org.maia.cgi.model.d3;

/**
 * Enumeration of the different coordinate frames
 * 
 * <p>
 * These are the different values :
 * <ul>
 * <li>The <em>OBJECT</em> coordinate frame refers to the object's canonical coordinate space, meaning prior to any
 * transformations applied to it</li>
 * <li>The <em>WORLD</em> coordinate frame refers to the object's coordinate space after all transformations are applied
 * to it</li>
 * <li>The <em>CAMERA</em> coordinate frame refers to the object's coordinate space after all transformations are
 * applied to it, as well as the transformation to camera coordinates</li>
 * </ul>
 * </p>
 */
public enum CoordinateFrame {

	OBJECT,

	WORLD,

	CAMERA;

}
