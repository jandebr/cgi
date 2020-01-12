package org.maia.cgi.geometry.d3;

import org.maia.cgi.metrics.Metrics;

public class Line3D {

	private Point3D p1;

	private Point3D p2;

	private Vector3D directionVector;

	public Line3D(Point3D p1, Point3D p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Line3D [\n");
		builder.append("\tp1=").append(p1).append("\n");
		builder.append("\tp2=").append(p2).append("\n");
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return p1.hashCode() + p2.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Line3D other = (Line3D) obj;
		if (getP1().equals(other.getP1()) && getP2().equals(other.getP2()))
			return true;
		if (getP1().equals(other.getP2()) && getP2().equals(other.getP1()))
			return true;
		return false;
	}

	public Vector3D getDirection() {
		if (directionVector == null) {
			double dx = getP2().getX() - getP1().getX();
			double dy = getP2().getY() - getP1().getY();
			double dz = getP2().getZ() - getP1().getZ();
			directionVector = new Vector3D(dx, dy, dz);
		}
		return directionVector;
	}

	public Point3D intersect(Plane3D plane) {
		Metrics.getInstance().incrementLineWithPlaneIntersections();
		Point3D result = null;
		Vector3D v = getDirection();
		Vector3D n = plane.getNormalUnitVector();
		double b = v.dotProduct(n);
		if (b != 0) {
			// line is not parallel to the plane, so there is 1 intersecting point
			Point3D q = getP1();
			Point3D p = plane.getP1();
			double a = q.getX() * n.getX() - p.getX() * n.getX() + q.getY() * n.getY() - p.getY() * n.getY() + q.getZ()
					* n.getZ() - p.getZ() * n.getZ();
			double r = -a / b;
			if (containsPointAtRelativePosition(r)) {
				result = getPointAtRelativePosition(r);
			}
		}
		return result;
	}

	public Point3D getPointAtRelativePosition(double r) {
		Point3D p1 = getP1();
		Point3D p2 = getP2();
		if (r == 0) {
			return p1;
		} else if (r == 1.0) {
			return p2;
		} else {
			double x = (1.0 - r) * p1.getX() + r * p2.getX();
			double y = (1.0 - r) * p1.getY() + r * p2.getY();
			double z = (1.0 - r) * p1.getZ() + r * p2.getZ();
			return new Point3D(x, y, z);
		}
	}

	protected boolean containsPointAtRelativePosition(double r) {
		return true; // open ended line, subclasses may override this
	}

	public Point3D getP1() {
		return p1;
	}

	public void setP1(Point3D p1) {
		this.p1 = p1;
		invalidateDerivedProperties();
	}

	public Point3D getP2() {
		return p2;
	}

	public void setP2(Point3D p2) {
		this.p2 = p2;
		invalidateDerivedProperties();
	}

	private void invalidateDerivedProperties() {
		directionVector = null;
	}

}
