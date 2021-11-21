package org.maia.cgi.geometry.d3;

import java.text.NumberFormat;

import org.maia.cgi.Metrics;

public class Vector3D {

	private static NumberFormat formatter;

	private double x;

	private double y;

	private double z;

	static {
		formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumFractionDigits(4);
		formatter.setMaximumFractionDigits(4);
	}

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D(Point3D from, Point3D to) {
		this(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());
	}

	public static Vector3D fromSphericalCoordinates(double longitudeInRadians, double latitudeInRadians) {
		return fromSphericalCoordinates(longitudeInRadians, latitudeInRadians, 1.0);
	}

	public static Vector3D fromSphericalCoordinates(double longitudeInRadians, double latitudeInRadians,
			double magnitude) {
		double lon_cs = Math.cos(longitudeInRadians);
		double lon_sn = Math.sin(longitudeInRadians);
		double lat_cs = Math.cos(latitudeInRadians);
		double lat_sn = Math.sin(latitudeInRadians);
		double x = magnitude * lat_cs * lon_cs;
		double y = magnitude * lat_sn;
		double z = magnitude * lat_cs * lon_sn;
		return new Vector3D(x, y, z);
	}

	@Override
	public Vector3D clone() {
		return new Vector3D(getX(), getY(), getZ());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("[ ").append(formatter.format(getX())).append(" ; ").append(formatter.format(getY())).append(" ; ")
				.append(formatter.format(getZ())).append(" ]");
		return sb.toString();
	}

	public double getMagnitude() {
		return Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
	}

	public Vector3D getUnitVector() {
		Metrics.getInstance().incrementVectorNormalizations();
		double m = getMagnitude();
		return new Vector3D(getX() / m, getY() / m, getZ() / m);
	}

	public void makeUnitVector() {
		Metrics.getInstance().incrementVectorNormalizations();
		double m = getMagnitude();
		setX(getX() / m);
		setY(getY() / m);
		setZ(getZ() / m);
	}

	public void scale(double scale) {
		setX(scale * getX());
		setY(scale * getY());
		setZ(scale * getZ());
	}

	public double dotProduct(Vector3D other) {
		Metrics.getInstance().incrementVectorDotProducts();
		return getX() * other.getX() + getY() * other.getY() + getZ() * other.getZ();
	}

	public Vector3D crossProduct(Vector3D other) {
		Metrics.getInstance().incrementVectorCrossProducts();
		double x = getY() * other.getZ() - getZ() * other.getY();
		double y = getZ() * other.getX() - getX() * other.getZ();
		double z = getX() * other.getY() - getY() * other.getX();
		return new Vector3D(x, y, z);
	}

	/**
	 * Returns the angle between this and a given vector.
	 * 
	 * @param other
	 *            The other vector
	 * @return The angle between this vector and the <code>other</code> vector, in radians between 0.0 and <i>Pi</i>.
	 */
	public double getAngleBetween(Vector3D other) {
		return Math.acos(getAngleCosineBetween(other));
	}

	public double getAngleCosineBetween(Vector3D other) {
		Metrics.getInstance().incrementVectorAnglesInBetween();
		return dotProduct(other) / (getMagnitude() * other.getMagnitude());
	}

	/**
	 * Synonym for longitude
	 * 
	 * @see #getLongitudeInRadians()
	 */
	public double getAzimuthInRadians() {
		return getLongitudeInRadians();
	}

	/**
	 * Returns the longitude of this vector
	 * 
	 * @return The longitude of this vector, in radians, as a number between 0 and 2*pi
	 */
	public double getLongitudeInRadians() {
		double lon = 0;
		double x = getX();
		double z = getZ();
		if (x == 0) {
			if (z > 0) {
				lon = Math.PI / 2;
			} else if (z < 0) {
				lon = -Math.PI / 2;
			}
		} else if (x > 0) {
			lon = Math.atan(z / x);
		} else {
			lon = Math.PI + Math.atan(z / x);
		}
		if (lon < 0) {
			lon += 2 * Math.PI;
		}
		return lon;
	}

	/**
	 * Returns the latitude of this vector
	 * 
	 * @return The latitude of this vector, in radians, as a number between -pi/2 and pi/2
	 */
	public double getLatitudeInRadians() {
		double lat = 0;
		double y = getY();
		double m = getMagnitude();
		if (m > 0) {
			lat = Math.asin(y / m);
		}
		return lat;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

}
