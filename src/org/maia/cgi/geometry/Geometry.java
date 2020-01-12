package org.maia.cgi.geometry;

public class Geometry {

	public static double degreesToRadians(double degrees) {
		return degrees / 180.0 * Math.PI;
	}

	public static double radiansToDegrees(double radians) {
		return radians / Math.PI * 180.0;
	}

}
