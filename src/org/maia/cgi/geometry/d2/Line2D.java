package org.maia.cgi.geometry.d2;

import org.maia.cgi.Metrics;

public class Line2D {

	private Point2D p1;

	private Point2D p2;

	public Line2D(Point2D p1, Point2D p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Line2D [\n");
		builder.append("\tp1=").append(p1).append("\n");
		builder.append("\tp2=").append(p2).append("\n");
		builder.append("]");
		return builder.toString();
	}

	public boolean contains(Point2D point) {
		double x = point.getX();
		double y = point.getY();
		double x1 = getP1().getX();
		double x2 = getP2().getX();
		double y1 = getP1().getY();
		double y2 = getP2().getY();
		if (x1 == x2) {
			// vertical line
			return x == x1 && y >= Math.min(y1, y2) && y <= Math.max(y1, y2);
		} else if (y1 == y2) {
			// horizontal line
			return y == y1 && x >= Math.min(x1, x2) && x <= Math.max(x1, x2);
		} else {
			// sloped line
			if ((x - x1) * (y2 - y1) != (y - y1) * (x2 - x1))
				return false;
			double r = (x - x1) / (x2 - x1);
			return containsPointAtRelativePosition(r);
		}
	}

	public Point2D intersect(Line2D other) {
		Point2D result = null;
		Metrics.getInstance().incrementLineWithLineIntersections();
		double p1x = getP1().getX();
		double p1y = getP1().getY();
		double pdx = getP2().getX() - p1x;
		double pdy = getP2().getY() - p1y;
		double q1x = other.getP1().getX();
		double q1y = other.getP1().getY();
		double qdx = other.getP2().getX() - q1x;
		double qdy = other.getP2().getY() - q1y;
		double det = pdx * qdy - pdy * qdx;
		if (det != 0) {
			double r = (qdy * (q1x - p1x) + qdx * (p1y - q1y)) / det;
			if (containsPointAtRelativePosition(r)) {
				double s = qdy != 0 ? (p1y - q1y + pdy * r) / qdy : (p1x - q1x + pdx * r) / qdx;
				if (other.containsPointAtRelativePosition(s)) {
					double xi = p1x + pdx * r;
					double yi = p1y + pdy * r;
					result = new Point2D(xi, yi);
				}
			}
		}
		return result;
	}

	protected boolean containsPointAtRelativePosition(double r) {
		return true; // open ended line, subclasses may override this
	}

	public Point2D getP1() {
		return p1;
	}

	public void setP1(Point2D p1) {
		this.p1 = p1;
	}

	public Point2D getP2() {
		return p2;
	}

	public void setP2(Point2D p2) {
		this.p2 = p2;
	}

}
