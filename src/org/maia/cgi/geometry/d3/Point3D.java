package org.maia.cgi.geometry.d3;

import java.text.NumberFormat;

import org.maia.cgi.Metrics;

public class Point3D {

	private static NumberFormat formatter;

	private double x;

	private double y;

	private double z;

	private double w; // for homogeneous coordinates

	static {
		formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumFractionDigits(4);
		formatter.setMaximumFractionDigits(4);
	}

	public Point3D() {
		this(0, 0, 0);
	}

	public Point3D(double x, double y, double z) {
		this(x, y, z, 1.0);
	}

	public Point3D(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public static Point3D origin() {
		return new Point3D();
	}

	public static Point3D centerOfTwo(Point3D one, Point3D other) {
		return interpolateBetween(one, other, 0.5);
	}

	public static Point3D interpolateBetween(Point3D from, Point3D to, double r) {
		double s = 1.0 - r;
		double x = s * from.getX() + r * to.getX();
		double y = s * from.getY() + r * to.getY();
		double z = s * from.getZ() + r * to.getZ();
		return new Point3D(x, y, z);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("[ ").append(formatter.format(getX())).append(" ; ").append(formatter.format(getY())).append(" ; ")
				.append(formatter.format(getZ())).append(" ; ").append(formatter.format(getW())).append(" ]");
		return sb.toString();
	}

	@Override
	public Point3D clone() {
		return new Point3D(getX(), getY(), getZ(), getW());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point3D other = (Point3D) obj;
		if (Double.doubleToLongBits(w) != Double.doubleToLongBits(other.w))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(w);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public Point3D plus(Vector3D vector) {
		double x = getX() + vector.getX();
		double y = getY() + vector.getY();
		double z = getZ() + vector.getZ();
		return new Point3D(x, y, z);
	}

	public Point3D minus(Vector3D vector) {
		double x = getX() - vector.getX();
		double y = getY() - vector.getY();
		double z = getZ() - vector.getZ();
		return new Point3D(x, y, z);
	}

	public Vector3D minus(Point3D other) {
		double x = getX() - other.getX();
		double y = getY() - other.getY();
		double z = getZ() - other.getZ();
		return new Vector3D(x, y, z);
	}

	public double distanceTo(Point3D other) {
		return Math.sqrt(squareDistanceTo(other));
	}

	public double squareDistanceTo(Point3D other) {
		double dx = getX() - other.getX();
		double dy = getY() - other.getY();
		double dz = getZ() - other.getZ();
		return dx * dx + dy * dy + dz * dz;
	}

	public void normalizeToUnitW() {
		Metrics.getInstance().incrementPointNormalizations();
		double w = getW();
		if (w != 1.0) {
			setX(getX() / w);
			setY(getY() / w);
			setZ(getZ() / w);
			setW(1.0);
		}
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

	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
	}

}
