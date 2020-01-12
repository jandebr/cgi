package org.maia.cgi.geometry.d2;

public class Rectangle2D {

	private double x1;

	private double x2;

	private double y1;

	private double y2;

	public Rectangle2D(double width, double height) {
		this(0, width, 0, height);
	}

	public Rectangle2D(double x1, double x2, double y1, double y2) {
		if (x1 > x2)
			throw new IllegalArgumentException("X boundaries out of order: " + x1 + " > " + x2);
		if (y1 > y2)
			throw new IllegalArgumentException("Y boundaries out of order: " + y1 + " > " + y2);
		setX1(x1);
		setX2(x2);
		setY1(y1);
		setY2(y2);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Rectangle2D [x1=");
		builder.append(x1);
		builder.append(", x2=");
		builder.append(x2);
		builder.append(", y1=");
		builder.append(y1);
		builder.append(", y2=");
		builder.append(y2);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Rectangle2D clone() {
		return new Rectangle2D(getX1(), getX2(), getY1(), getY2());
	}

	public void expandToContain(Rectangle2D other) {
		setX1(Math.min(getX1(), other.getX1()));
		setX2(Math.max(getX2(), other.getX2()));
		setY1(Math.min(getY1(), other.getY1()));
		setY2(Math.max(getY2(), other.getY2()));
	}

	public void expandToContain(Point2D point) {
		setX1(Math.min(getX1(), point.getX()));
		setX2(Math.max(getX2(), point.getX()));
		setY1(Math.min(getY1(), point.getY()));
		setY2(Math.max(getY2(), point.getY()));
	}

	public boolean overlaps(Rectangle2D other) {
		if (other.getX2() < getX1() || other.getX1() > getX2())
			return false;
		if (other.getY2() < getY1() || other.getY1() > getY2())
			return false;
		return true;
	}

	public Rectangle2D intersect(Rectangle2D other) {
		if (!overlaps(other)) {
			return null;
		} else {
			double x1 = Math.max(getX1(), other.getX1());
			double x2 = Math.min(getX2(), other.getX2());
			double y1 = Math.max(getY1(), other.getY1());
			double y2 = Math.min(getY2(), other.getY2());
			return new Rectangle2D(x1, x2, y1, y2);
		}
	}

	public double getWidth() {
		return getX2() - getX1();
	}

	public double getHeight() {
		return getY2() - getY1();
	}

	public double getLeft() {
		return getX1();
	}

	public double getRight() {
		return getX2();
	}

	public double getBottom() {
		return getY1();
	}

	public double getTop() {
		return getY2();
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

}