package org.maia.cgi.model.d2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import org.maia.cgi.geometry.d2.Point2D;
import org.maia.cgi.geometry.d2.Polygon2D;
import org.maia.cgi.geometry.d3.Path3D;
import org.maia.cgi.geometry.d3.Point3D;

public class ShapeContourTracer {

	private Perimeter neighbourhoodPerimeter;

	public ShapeContourTracer() {
		this.neighbourhoodPerimeter = Perimeter.createNeighbourhoodPerimeter();
	}

	/**
	 * Traces the contour of a shape in an image
	 * 
	 * @param image
	 *            The image
	 * @param shapeColor
	 *            The solid color of the shape
	 * @return The contour of the shape, as a polyline marking the shape's boundary
	 */
	public ShapeContour traceContour(BufferedImage image, Color shapeColor) {
		return traceContour(image, shapeColor, 5.0);
	}

	/**
	 * Traces the contour of a shape in an image
	 * 
	 * @param image
	 *            The image
	 * @param shapeColor
	 *            The solid color of the shape
	 * @param distanceBetweenPoints
	 *            The target distance between interpolating points on the contour
	 * @return The contour of the shape, as a polyline marking the shape's boundary
	 */
	public ShapeContour traceContour(BufferedImage image, Color shapeColor, double distanceBetweenPoints) {
		ShapeContour contour = null;
		int shapeRGB = shapeColor.getRGB();
		PixelCoords startPoint = findContourStartingPoint(image, shapeRGB);
		if (startPoint != null) {
			contour = new ShapeContour();
			contour.addPoint(startPoint);
			Perimeter perimeter = Perimeter.createCircularPerimeter((int) Math.ceil(distanceBetweenPoints));
			boolean proceed = true;
			do {
				PixelCoords nextPoint = findNextContourPoint(contour, image, shapeRGB, perimeter);
				if (nextPoint != null) {
					contour.addPoint(nextPoint);
					if (contour.getNumberOfPoints() >= 3 && nextPoint.distanceTo(startPoint) <= distanceBetweenPoints) {
						proceed = false;
					}
				} else {
					proceed = false;
				}
			} while (proceed);
		}
		return contour;
	}

	private PixelCoords findContourStartingPoint(BufferedImage image, int shapeRGB) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int yi = 0; yi < height; yi++) {
			for (int xi = 0; xi < width; xi++) {
				if (image.getRGB(xi, yi) == shapeRGB) {
					return new PixelCoords(xi, yi);
				}
			}
		}
		return null;
	}

	private PixelCoords findNextContourPoint(ShapeContour contour, BufferedImage image, int shapeRGB,
			Perimeter perimeter) {
		PixelCoords previousPoint = contour.getPreviousPoint();
		PixelCoords currentPoint = contour.getCurrentPoint();
		PixelCoords nextPoint = null;
		double highScore = -2.0;
		int dx1 = 0, dy1 = 0;
		double m1 = 0;
		if (previousPoint != null) {
			dx1 = currentPoint.getX() - previousPoint.getX();
			dy1 = currentPoint.getY() - previousPoint.getY();
			m1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
		}
		for (PixelCoords peri : perimeter.getPoints()) {
			int xi = currentPoint.getX() + peri.getX();
			int yi = currentPoint.getY() + peri.getY();
			if (onEdge(xi, yi, image, shapeRGB)) {
				double score = 1.0;
				if (previousPoint != null) {
					int dx2 = xi - currentPoint.getX();
					int dy2 = yi - currentPoint.getY();
					double m2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
					score = (dx1 * dx2 + dy1 * dy2) / (m1 * m2);
				}
				if (score > highScore) {
					highScore = score;
					nextPoint = new PixelCoords(xi, yi);
				}
			}
		}
		return nextPoint;
	}

	private boolean onEdge(int xi, int yi, BufferedImage image, int shapeRGB) {
		int width = image.getWidth();
		int height = image.getHeight();
		if (xi >= 0 && xi < width && yi >= 0 && yi < height) {
			// (xi,yi) inside shape
			if (image.getRGB(xi, yi) == shapeRGB) {
				// At least one neighbouring pixel outside shape (or image)
				for (PixelCoords point : getNeighbourhoodPerimeter().getPoints()) {
					int xn = xi + point.getX();
					int yn = yi + point.getY();
					if (xn >= 0 && xn < width && yn >= 0 && yn < height) {
						if (image.getRGB(xn, yn) != shapeRGB)
							return true;
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	private Perimeter getNeighbourhoodPerimeter() {
		return neighbourhoodPerimeter;
	}

	public static class ShapeContour {

		private List<PixelCoords> points;

		private ShapeContour() {
			this(new Vector<PixelCoords>(100));
		}

		private ShapeContour(List<PixelCoords> points) {
			this.points = points;
		}

		public int getNumberOfPoints() {
			return getPoints().size();
		}

		public ShapeContour compact() {
			List<PixelCoords> compactPoints = new Vector<PixelCoords>(getNumberOfPoints());
			for (int i = 0; i < getNumberOfPoints(); i++) {
				PixelCoords previousPoint = i > 0 ? getPoints().get(i - 1) : null;
				PixelCoords currentPoint = getPoints().get(i);
				PixelCoords nextPoint = i < getNumberOfPoints() - 1 ? getPoints().get(i + 1) : null;
				boolean includeCurrent = true;
				if (previousPoint != null && nextPoint != null) {
					boolean isVertical = previousPoint.getX() == currentPoint.getX()
							&& nextPoint.getX() == currentPoint.getX();
					boolean isHorizontal = previousPoint.getY() == currentPoint.getY()
							&& nextPoint.getY() == currentPoint.getY();
					if (isVertical || isHorizontal) {
						includeCurrent = false; // leave out currentPoint
					}
				}
				if (includeCurrent) {
					compactPoints.add(currentPoint);
				}
			}
			return new ShapeContour(compactPoints);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ShapeContour [\n");
			for (PixelCoords point : getPoints()) {
				builder.append('\t').append(point.getX()).append('\t').append(point.getY()).append('\n');
			}
			builder.append("]");
			return builder.toString();
		}

		public List<Point2D> getContourPoints2D() {
			List<Point2D> points = new Vector<Point2D>(getNumberOfPoints());
			for (PixelCoords pc : getPoints()) {
				points.add(new Point2D(pc.getX(), -pc.getY()));
			}
			return points;
		}

		public List<Point3D> getContourPoints3D() {
			List<Point3D> points = new Vector<Point3D>(getNumberOfPoints());
			for (PixelCoords pc : getPoints()) {
				points.add(new Point3D(pc.getX(), -pc.getY(), 0));
			}
			return points;
		}

		public Polygon2D asPolygon2D() {
			return new Polygon2D(getContourPoints2D());
		}

		public Path3D asPath3D() {
			return Path3D.createFromPoints(getContourPoints3D());
		}

		private void addPoint(PixelCoords point) {
			getPoints().add(point);
		}

		private PixelCoords getCurrentPoint() {
			if (getNumberOfPoints() > 0)
				return getPoints().get(getNumberOfPoints() - 1);
			else
				return null;
		}

		private PixelCoords getPreviousPoint() {
			if (getNumberOfPoints() > 1)
				return getPoints().get(getNumberOfPoints() - 2);
			else
				return null;
		}

		private List<PixelCoords> getPoints() {
			return points;
		}

	}

	private static class Perimeter {

		private List<PixelCoords> points;

		private Perimeter() {
			points = new Vector<PixelCoords>();
		}

		private void addPoint(PixelCoords point) {
			getPoints().add(point);
		}

		public static Perimeter createNeighbourhoodPerimeter() {
			Perimeter perimeter = new Perimeter();
			for (int yi = -1; yi <= 1; yi++) {
				for (int xi = -1; xi <= 1; xi++) {
					if (xi != 0 || yi != 0) {
						perimeter.addPoint(new PixelCoords(xi, yi));
					}
				}
			}
			return perimeter;
		}

		public static Perimeter createCircularPerimeter(int radius) {
			Perimeter perimeter = new Perimeter();
			double r2 = (radius + 0.5) * (radius + 0.5);
			int yi = 0;
			int previous_yi = 0;
			for (int xi = 0; xi <= radius; xi++) {
				double x = xi;
				double d2 = 0;
				do {
					double y = radius - yi;
					d2 = x * x + y * y;
					if (d2 > r2)
						yi++;
				} while (d2 > r2);
				if (xi == radius)
					yi = radius;
				for (int k = Math.min(previous_yi + 1, yi); k <= yi; k++) {
					perimeter.addPoint(new PixelCoords(xi, k - radius));
					if (k < radius)
						perimeter.addPoint(new PixelCoords(xi, radius - k));
					if (xi > 0) {
						perimeter.addPoint(new PixelCoords(-xi, k - radius));
						if (k < radius)
							perimeter.addPoint(new PixelCoords(-xi, radius - k));
					}
				}
				previous_yi = yi;
			}
			return perimeter;
		}

		public List<PixelCoords> getPoints() {
			return points;
		}

	}

	private static class PixelCoords {

		private int x;

		private int y;

		public PixelCoords(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(").append(x).append(",").append(y).append(")");
			return builder.toString();
		}

		public double distanceTo(PixelCoords other) {
			double dx = other.getX() - getX();
			double dy = other.getY() - getY();
			return Math.sqrt(dx * dx + dy * dy);
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

	}

}