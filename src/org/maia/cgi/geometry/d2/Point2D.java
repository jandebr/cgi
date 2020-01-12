package org.maia.cgi.geometry.d2;

public class Point2D {

	private double x;

	private double y;

	public Point2D() {
		this(0, 0);
	}

	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("[ ").append(getX()).append(" ; ").append(getY()).append(" ]");
		return sb.toString();
	}

	@Override
	public Point2D clone() {
		return new Point2D(getX(), getY());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point2D other = (Point2D) obj;
		return getX() == other.getX() && getY() == other.getY();
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

}