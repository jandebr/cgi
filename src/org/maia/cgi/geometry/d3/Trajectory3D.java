package org.maia.cgi.geometry.d3;

import org.maia.cgi.model.d3.camera.MovableCamera;

/**
 * Tracks the position and orientation when moving along a <code>Curve3D</code>
 * 
 * <p>
 * When created, the virtual position is at the start of the curve. There are methods to {@linkplain #advance(double)
 * advance} or {@linkplain #reverse(double) reverse} a certain distance along the curve. One can also move to a specific
 * position using the methods {@linkplain #locate(double) locate}, {@linkplain #moveToStart() moveToStart} and
 * {@linkplain #moveToEnd() moveToEnd}. The current position can be requested using {@link #getCurrentLocation()}. It
 * returns an instance of {@linkplain Location} which holds both a position and an {@linkplain Orientation} in 3D.
 * </p>
 * 
 * @see Curve3D
 */
public class Trajectory3D {

	private Curve3D curve;

	private Vector3D upAtStart;

	private Vector3D upAtEnd;

	private Location currentLocation;

	private Box3D approximateBoundingBox;

	private double approximateAbsoluteDistance = -1.0;

	private static final double DELTA_DISTANCE = 0.0001;

	private static final int CURVE_SCAN_SAMPLES = 1000;

	public Trajectory3D(Curve3D curve) {
		this(curve, new Vector3D(0, 1.0, 0));
	}

	public Trajectory3D(Curve3D curve, Vector3D upAtBoundaries) {
		this(curve, upAtBoundaries, upAtBoundaries);
	}

	public Trajectory3D(Curve3D curve, Vector3D upAtStart, Vector3D upAtEnd) {
		this.curve = curve;
		this.upAtStart = upAtStart;
		this.upAtEnd = upAtEnd;
		moveToStart();
	}

	public Location moveToStart() {
		Point3D from = getCurve().sample(0.0);
		Point3D to = getCurve().sample(DELTA_DISTANCE);
		Orientation orientation = computeOrientation(from, to, getUpAtStart());
		return updateCurrentLocation(new Location(0.0, from, orientation));
	}

	public Location moveToEnd() {
		Point3D from = getCurve().sample(1.0 - DELTA_DISTANCE);
		Point3D to = getCurve().sample(1.0);
		Orientation orientation = computeOrientation(from, to, getUpAtEnd());
		return updateCurrentLocation(new Location(1.0, to, orientation));
	}

	public Location advance(double distance) {
		double t = getCurrentLocation().getUnitDistanceAlongCurve();
		if (t == 1.0)
			return getCurrentLocation();
		double t1 = Math.min(t + distance, 1.0);
		Point3D from = getCurrentLocation().getPosition();
		Point3D to = getCurve().sample(t1);
		Orientation orientation = computeOrientation(from, to, getCurrentLocation().getOrientation());
		return updateCurrentLocation(new Location(t1, to, orientation));
	}

	public Location reverse(double distance) {
		double t = getCurrentLocation().getUnitDistanceAlongCurve();
		if (t == 0.0)
			return getCurrentLocation();
		double t1 = Math.max(t - distance, 0.0);
		Point3D from = getCurve().sample(t1);
		Point3D to = getCurrentLocation().getPosition();
		Orientation orientation = computeOrientation(from, to, getCurrentLocation().getOrientation());
		return updateCurrentLocation(new Location(t1, from, orientation));
	}

	public Location locate(double unitDistanceFromStart) {
		return locate(unitDistanceFromStart, getCurrentLocation().getOrientation().getUnitV());
	}

	public Location locate(double unitDistanceFromStart, Vector3D up) {
		if (unitDistanceFromStart <= 0.0) {
			return moveToStart();
		} else if (unitDistanceFromStart >= 1.0) {
			return moveToEnd();
		} else {
			double t1 = Math.min(unitDistanceFromStart + DELTA_DISTANCE, 1.0);
			Point3D from = getCurve().sample(unitDistanceFromStart);
			Point3D to = getCurve().sample(t1);
			Orientation orientation = computeOrientation(from, to, up);
			return updateCurrentLocation(new Location(unitDistanceFromStart, from, orientation));
		}
	}

	private Location updateCurrentLocation(Location location) {
		setCurrentLocation(location);
		return location;
	}

	private void estimateCurveGeometricalProperties() {
		Point3D p0 = null;
		for (int i = 0; i <= CURVE_SCAN_SAMPLES; i++) {
			Point3D p = getCurve().sample(i * 1.0 / CURVE_SCAN_SAMPLES);
			if (i == 0) {
				approximateBoundingBox = new Box3D(p.getX(), p.getX(), p.getY(), p.getY(), p.getZ(), p.getZ());
				approximateAbsoluteDistance = 0.0;
			} else {
				approximateBoundingBox.expandToContain(p);
				approximateAbsoluteDistance += p.distanceTo(p0);
			}
			p0 = p;
		}
	}

	private static Orientation computeOrientation(Point3D from, Point3D to, Orientation previousOrientation) {
		return computeOrientation(from, to, previousOrientation.getUnitV());
	}

	private static Orientation computeOrientation(Point3D from, Point3D to, Vector3D up) {
		Vector3D n = from.minus(to);
		Vector3D u = up.crossProduct(n);
		Vector3D v = n.crossProduct(u);
		u.makeUnitVector();
		v.makeUnitVector();
		n.makeUnitVector();
		return new Orientation(u, v, n);
	}

	private Curve3D getCurve() {
		return curve;
	}

	private Vector3D getUpAtStart() {
		return upAtStart;
	}

	private Vector3D getUpAtEnd() {
		return upAtEnd;
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	private void setCurrentLocation(Location location) {
		this.currentLocation = location;
	}

	public Box3D getApproximateBoundingBox() {
		if (approximateBoundingBox == null) {
			estimateCurveGeometricalProperties();
		}
		return approximateBoundingBox;
	}

	public double getApproximateAbsoluteDistance() {
		if (approximateAbsoluteDistance < 0) {
			estimateCurveGeometricalProperties();
		}
		return approximateAbsoluteDistance;
	}

	public static class Location {

		private double unitDistanceAlongCurve;

		private Point3D position;

		private Orientation orientation;

		private Location(double unitDistanceAlongCurve, Point3D position, Orientation orientation) {
			this.unitDistanceAlongCurve = unitDistanceAlongCurve;
			this.position = position;
			this.orientation = orientation;
		}

		public MovableCamera alignCamera(MovableCamera camera) {
			return camera.position(getPosition(), getPosition().minus(getOrientation().getUnitN()), getOrientation()
					.getUnitV());
		}

		public double getUnitDistanceAlongCurve() {
			return unitDistanceAlongCurve;
		}

		public Point3D getPosition() {
			return position;
		}

		public Orientation getOrientation() {
			return orientation;
		}

	}

	public static class Orientation {

		private Vector3D unitU;

		private Vector3D unitV;

		private Vector3D unitN;

		private Orientation(Vector3D unitU, Vector3D unitV, Vector3D unitN) {
			this.unitU = unitU;
			this.unitV = unitV;
			this.unitN = unitN;
		}

		public Vector3D getUnitU() {
			return unitU;
		}

		public Vector3D getUnitV() {
			return unitV;
		}

		public Vector3D getUnitN() {
			return unitN;
		}

	}

}