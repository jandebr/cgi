package org.maia.cgi.geometry.d3;

public class LineSegment3D extends Line3D {

	private boolean closedAtP1;

	private boolean closedAtP2;

	public LineSegment3D(Point3D p1, Point3D p2) {
		this(p1, p2, true, true);
	}

	public LineSegment3D(Point3D p1, Point3D p2, boolean closedAtP1, boolean closedAtP2) {
		super(p1, p2);
		this.closedAtP1 = closedAtP1;
		this.closedAtP2 = closedAtP2;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineSegment3D other = (LineSegment3D) obj;
		if (getP1().equals(other.getP1()) && getP2().equals(other.getP2()) && isClosedAtP1() == other.isClosedAtP1()
				&& isClosedAtP2() == other.isClosedAtP2())
			return true;
		if (getP1().equals(other.getP2()) && getP2().equals(other.getP1()) && isClosedAtP1() == other.isClosedAtP2()
				&& isClosedAtP2() == other.isClosedAtP1())
			return true;
		return false;
	}

	@Override
	protected boolean containsPointAtRelativePosition(double r) {
		if (r < 0) {
			return !isClosedAtP1();
		} else if (r > 1.0) {
			return !isClosedAtP2();
		} else {
			return true;
		}
	}

	public boolean isClosedAtP1() {
		return closedAtP1;
	}

	public void setClosedAtP1(boolean closedAtP1) {
		this.closedAtP1 = closedAtP1;
	}

	public boolean isClosedAtP2() {
		return closedAtP2;
	}

	public void setClosedAtP2(boolean closedAtP2) {
		this.closedAtP2 = closedAtP2;
	}

}
