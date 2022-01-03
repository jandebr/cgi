package org.maia.cgi.model.d3;

/**
 * Enumeration of the different coordinate frames
 */
public enum CoordinateFrame {

	/**
	 * The <code>OBJECT</code> coordinate frame refers to the object's canonical coordinate space, meaning prior to any
	 * transformations applied to it
	 */
	OBJECT,

	/**
	 * The <code>WORLD</code> coordinate frame refers to the object's coordinate space after all transformations are
	 * applied to it
	 */
	WORLD,

	/**
	 * The <code>CAMERA</code> coordinate frame refers to the object's coordinate space after all transformations are
	 * applied to it, as well as the transformation to camera coordinates
	 */
	CAMERA,

	/**
	 * The <code>VIEWVOLUME</code> coordinate frame refers to the camera's <em>canonical view volume</em>, in which
	 * every dimension ranges from -1 to +1. The <code>Z</code> coordinate represents the so-called
	 * <em>pseudo depth</em>, ranging from -1 (the <em>near plane</em>) to +1 (the <em>far plane</em>)
	 */
	VIEWVOLUME;

}