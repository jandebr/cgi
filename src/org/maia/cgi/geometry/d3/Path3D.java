package org.maia.cgi.geometry.d3;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.maia.cgi.geometry.d2.Point2D;
import org.maia.cgi.transform.d3.Transformation;

public class Path3D {

	private List<PathPoint> pathPoints;

	private List<PathPoint> orderedPathPoints;

	public Path3D() {
		this.pathPoints = new Vector<PathPoint>();
	}

	public static Path3D createFromPoints(List<Point3D> points) {
		int n = points.size();
		if (n < 2)
			throw new IllegalArgumentException("Should pass at least 2 points to create a path");
		Path3D path = new Path3D();
		double[] relativeDistances = new double[n];
		for (int i = 1; i < n; i++) {
			relativeDistances[i] = relativeDistances[i - 1] + points.get(i - 1).distanceTo(points.get(i));
		}
		double relativeDistanceMax = relativeDistances[n - 1];
		for (int i = 0; i < n; i++) {
			path.addPoint(points.get(i), relativeDistances[i] / relativeDistanceMax);
		}
		return path;
	}

	public void addPoint(Point3D position, double relativeDistance) {
		addPoint(new PathPoint(position, relativeDistance));
	}

	public void addPoint(PathPoint point) {
		getPathPoints().add(point);
		invalidateOrder();
	}

	public PointAlongPath interpolate(double relativeDistance) {
		PointAlongPath result = null;
		if (getPathPoints().size() >= 2) {
			Iterator<PathPoint> it = getOrderedPathPoints().iterator();
			PathPoint p0 = it.next();
			PathPoint p1 = p0;
			double r0 = p0.getRelativeDistance();
			double r1 = r0;
			do {
				p0 = p1;
				r0 = r1;
				p1 = it.next();
				r1 = p1.getRelativeDistance();
			} while (relativeDistance > r1 && it.hasNext());
			if (relativeDistance <= r1) {
				double r = r0 == r1 ? 0 : (relativeDistance - r0) / (r1 - r0);
				result = interpolateBetweenConsecutivePoints(p0, p1, r);
			}
		}
		return result;
	}

	private PointAlongPath interpolateBetweenConsecutivePoints(PathPoint p0, PathPoint p1, double r) {
		Point3D a = p0.getPositionOnPath();
		Point3D b = p1.getPositionOnPath();
		double s = 1.0 - r;
		double x = s * a.getX() + r * b.getX();
		double y = s * a.getY() + r * b.getY();
		double z = s * a.getZ() + r * b.getZ();
		double rd = s * p0.getRelativeDistance() + r * p1.getRelativeDistance();
		Vector3D direction = b.minus(a).getUnitVector();
		return new PointAlongPath(new Point3D(x, y, z), rd, direction);
	}

	public PointAroundPath sample(double relativeDistance, OrthogonalPathDistribution distribution) {
		PointAlongPath pp = interpolate(relativeDistance);
		Point2D d = distribution.sample(pp);
		Point3D q = new Point3D(0, d.getY(), -d.getX());
		q = Transformation.getRotationZrollMatrix(pp.getDirection().getLatitudeInRadians()).transform(q);
		q = Transformation.getRotationYrollMatrix(pp.getDirection().getAzimuthInRadians()).transform(q);
		q = q.plus(pp.getPositionOnPath().minus(Point3D.origin()));
		return new PointAroundPath(q, pp);
	}

	private void invalidateOrder() {
		orderedPathPoints = null;
	}

	private List<PathPoint> getOrderedPathPoints() {
		if (orderedPathPoints == null) {
			orderedPathPoints = new Vector<PathPoint>(getPathPoints());
			Collections.sort(orderedPathPoints);
		}
		return orderedPathPoints;
	}

	private List<PathPoint> getPathPoints() {
		return pathPoints;
	}

	public static class PathPoint implements Comparable<PathPoint> {

		private Point3D positionOnPath;

		private double relativeDistance;

		public PathPoint(Point3D positionOnPath, double relativeDistance) {
			this.positionOnPath = positionOnPath;
			this.relativeDistance = relativeDistance;
		}

		@Override
		public int compareTo(PathPoint other) {
			return (int) Math.signum(getRelativeDistance() - other.getRelativeDistance());
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PathPoint {\n");
			builder.append("\trelative distance: ").append(getRelativeDistance()).append("\n");
			builder.append("\tposition on path: ").append(getPositionOnPath()).append("\n");
			builder.append("}");
			return builder.toString();
		}

		public Point3D getPositionOnPath() {
			return positionOnPath;
		}

		public double getRelativeDistance() {
			return relativeDistance;
		}

	}

	public static class PointAlongPath extends PathPoint {

		private Vector3D direction;

		public PointAlongPath(Point3D positionOnPath, double relativeDistance, Vector3D direction) {
			super(positionOnPath, relativeDistance);
			this.direction = direction;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PointAlongPath {\n");
			builder.append("\trelative distance: ").append(getRelativeDistance()).append("\n");
			builder.append("\tposition on path: ").append(getPositionOnPath()).append("\n");
			builder.append("\tdirection: ").append(getDirection()).append("\n");
			builder.append("}");
			return builder.toString();
		}

		public Vector3D getDirection() {
			return direction;
		}

	}

	public static class PointAroundPath extends PointAlongPath {

		private Point3D position;

		public PointAroundPath(Point3D position, PointAlongPath p) {
			this(position, p.getPositionOnPath(), p.getRelativeDistance(), p.getDirection());
		}

		public PointAroundPath(Point3D position, Point3D positionOnPath, double relativeDistance, Vector3D direction) {
			super(positionOnPath, relativeDistance, direction);
			this.position = position;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PointAroundPath {\n");
			builder.append("\tposition: ").append(getPosition()).append("\n");
			builder.append("\trelative distance: ").append(getRelativeDistance()).append("\n");
			builder.append("\tposition on path: ").append(getPositionOnPath()).append("\n");
			builder.append("\tdirection: ").append(getDirection()).append("\n");
			builder.append("}");
			return builder.toString();
		}

		public Point3D getPosition() {
			return position;
		}

	}

	public static interface OrthogonalPathDistribution {

		Point2D sample(PointAlongPath point);

	}

	public static class GaussianPathDistribution implements OrthogonalPathDistribution {

		private ScalingFunction scalingFtX;

		private ScalingFunction scalingFtY;

		private Random randomizer;

		public GaussianPathDistribution(ScalingFunction scalingFtX, ScalingFunction scalingFtY) {
			this.scalingFtX = scalingFtX;
			this.scalingFtY = scalingFtY;
			this.randomizer = new Random();
		}

		@Override
		public Point2D sample(PointAlongPath point) {
			double x = getRandomizer().nextGaussian() * getScalingFtX().getScaleFactor(point);
			double y = getRandomizer().nextGaussian() * getScalingFtY().getScaleFactor(point);
			return new Point2D(x, y);
		}

		private ScalingFunction getScalingFtX() {
			return scalingFtX;
		}

		private ScalingFunction getScalingFtY() {
			return scalingFtY;
		}

		private Random getRandomizer() {
			return randomizer;
		}

	}

	public static interface ScalingFunction {

		double getScaleFactor(PointAlongPath point);

	}

	public static class ConstantScalingFunction implements ScalingFunction {

		private double scale;

		public ConstantScalingFunction(double scale) {
			this.scale = scale;
		}

		@Override
		public double getScaleFactor(PointAlongPath point) {
			return scale;
		}

	}

}