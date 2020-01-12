package org.maia.cgi.geometry.d3;

public class Plane3D {

	private Point3D p1;

	private Point3D p2;

	private Point3D p3;

	private Vector3D normalUnitVector;

	public Plane3D(Point3D p1, Point3D p2, Point3D p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Plane3D [\n");
		builder.append("\tp1=").append(p1).append("\n");
		builder.append("\tp2=").append(p2).append("\n");
		builder.append("\tp3=").append(p3).append("\n");
		builder.append("]");
		return builder.toString();
	}

	public Vector3D getNormalUnitVector() {
		if (normalUnitVector == null) {
			Vector3D va = new Vector3D(getP1(), getP2());
			Vector3D vb = new Vector3D(getP1(), getP3());
			normalUnitVector = va.crossProduct(vb).getUnitVector();
		}
		return normalUnitVector;
	}

	public Point3D intersect(Line3D line) {
		return line.intersect(this);
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

	public Point3D getP3() {
		return p3;
	}

	public void setP3(Point3D p3) {
		this.p3 = p3;
		invalidateDerivedProperties();
	}

	private void invalidateDerivedProperties() {
		normalUnitVector = null;
	}

}
