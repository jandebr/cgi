package org.maia.cgi.geometry.d2;

public class LineSegment2D extends Line2D {

	private boolean closedAtP1;

	private boolean closedAtP2;

	public LineSegment2D(Point2D p1, Point2D p2) {
		this(p1, p2, true, true);
	}

	public LineSegment2D(Point2D p1, Point2D p2, boolean closedAtP1, boolean closedAtP2) {
		super(p1, p2);
		this.closedAtP1 = closedAtP1;
		this.closedAtP2 = closedAtP2;
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