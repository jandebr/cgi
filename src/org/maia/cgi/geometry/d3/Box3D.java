package org.maia.cgi.geometry.d3;

import java.util.List;
import java.util.Vector;

public class Box3D {

	private double x1;

	private double x2;

	private double y1;

	private double y2;

	private double z1;

	private double z2;

	public Box3D(double x1, double x2, double y1, double y2, double z1, double z2) {
		if (x1 > x2)
			throw new IllegalArgumentException("X boundaries out of order: " + x1 + " > " + x2);
		if (y1 > y2)
			throw new IllegalArgumentException("Y boundaries out of order: " + y1 + " > " + y2);
		if (z1 > z2)
			throw new IllegalArgumentException("Z boundaries out of order: " + z1 + " > " + z2);
		setX1(x1);
		setX2(x2);
		setY1(y1);
		setY2(y2);
		setZ1(z1);
		setZ2(z2);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Box3D [x1=");
		builder.append(x1);
		builder.append(", x2=");
		builder.append(x2);
		builder.append(", y1=");
		builder.append(y1);
		builder.append(", y2=");
		builder.append(y2);
		builder.append(", z1=");
		builder.append(z1);
		builder.append(", z2=");
		builder.append(z2);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Box3D clone() {
		return new Box3D(getX1(), getX2(), getY1(), getY2(), getZ1(), getZ2());
	}

	public void expandToContain(Box3D other) {
		setX1(Math.min(getX1(), other.getX1()));
		setX2(Math.max(getX2(), other.getX2()));
		setY1(Math.min(getY1(), other.getY1()));
		setY2(Math.max(getY2(), other.getY2()));
		setZ1(Math.min(getZ1(), other.getZ1()));
		setZ2(Math.max(getZ2(), other.getZ2()));
	}

	public void expandToContain(Point3D point) {
		setX1(Math.min(getX1(), point.getX()));
		setX2(Math.max(getX2(), point.getX()));
		setY1(Math.min(getY1(), point.getY()));
		setY2(Math.max(getY2(), point.getY()));
		setZ1(Math.min(getZ1(), point.getZ()));
		setZ2(Math.max(getZ2(), point.getZ()));
	}

	public Box3D getIntersection(Box3D other) {
		Box3D intersection = null;
		if (overlaps(other)) {
			double x1 = Math.max(getX1(), other.getX1());
			double x2 = Math.min(getX2(), other.getX2());
			double y1 = Math.max(getY1(), other.getY1());
			double y2 = Math.min(getY2(), other.getY2());
			double z1 = Math.max(getZ1(), other.getZ1());
			double z2 = Math.min(getZ2(), other.getZ2());
			intersection = new Box3D(x1, x2, y1, y2, z1, z2);
		}
		return intersection;
	}

	public boolean isCollapsed() {
		return getWidth() == 0 || getHeight() == 0 || getDepth() == 0;
	}

	public double getWidth() {
		return getX2() - getX1();
	}

	public double getHeight() {
		return getY2() - getY1();
	}

	public double getDepth() {
		return getZ2() - getZ1();
	}

	public Point3D getCenter() {
		return new Point3D((getX1() + getX2()) / 2, (getY1() + getY2()) / 2, (getZ1() + getZ2()) / 2);
	}

	public boolean contains(Point3D point) {
		if (point.getX() < getX1() || point.getX() > getX2())
			return false;
		if (point.getY() < getY1() || point.getY() > getY2())
			return false;
		if (point.getZ() < getZ1() || point.getZ() > getZ2())
			return false;
		return true;
	}

	public boolean overlaps(Box3D other) {
		if (other.getX2() <= getX1() || other.getX1() >= getX2())
			return false;
		if (other.getY2() <= getY1() || other.getY1() >= getY2())
			return false;
		if (other.getZ2() <= getZ1() || other.getZ1() >= getZ2())
			return false;
		return true;
	}

	public List<Point3D> getVertices() {
		List<Point3D> vertices = new Vector<Point3D>(8);
		vertices.add(new Point3D(getX1(), getY1(), getZ1()));
		vertices.add(new Point3D(getX1(), getY2(), getZ1()));
		vertices.add(new Point3D(getX2(), getY2(), getZ1()));
		vertices.add(new Point3D(getX2(), getY1(), getZ1()));
		vertices.add(new Point3D(getX1(), getY1(), getZ2()));
		vertices.add(new Point3D(getX1(), getY2(), getZ2()));
		vertices.add(new Point3D(getX2(), getY2(), getZ2()));
		vertices.add(new Point3D(getX2(), getY1(), getZ2()));
		return vertices;
	}

	public double getX1() {
		return x1;
	}

	private void setX1(double x1) {
		this.x1 = x1;
	}

	public double getX2() {
		return x2;
	}

	private void setX2(double x2) {
		this.x2 = x2;
	}

	public double getY1() {
		return y1;
	}

	private void setY1(double y1) {
		this.y1 = y1;
	}

	public double getY2() {
		return y2;
	}

	private void setY2(double y2) {
		this.y2 = y2;
	}

	public double getZ1() {
		return z1;
	}

	private void setZ1(double z1) {
		this.z1 = z1;
	}

	public double getZ2() {
		return z2;
	}

	private void setZ2(double z2) {
		this.z2 = z2;
	}

}
