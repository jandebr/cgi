package org.maia.cgi.compose.d3;

public interface DepthFunction {

	/**
	 * Evaluates this function for a given depth.
	 * 
	 * @param depth
	 *            The depth, measured along the negative Z-axis; larger values mean further away from the eye
	 * @return The evaluated function value for <code>depth</code>.
	 */
	double eval(double depth);

}
