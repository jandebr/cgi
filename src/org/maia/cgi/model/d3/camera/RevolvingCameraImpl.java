package org.maia.cgi.model.d3.camera;

import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.transform.d3.CompositeTransform;
import org.maia.cgi.transform.d3.Transformation;

public class RevolvingCameraImpl extends MovableCameraImpl implements RevolvingCamera {

	private Point3D pivotPoint;

	private double longitudeInRadians;

	private double latitudeInRadians;

	public RevolvingCameraImpl(Point3D pivotPoint, Point3D position, ViewVolume viewVolume) {
		super(viewVolume);
		setPivotPoint(pivotPoint);
		setPosition(position);
		updateSphericalCoordinates();
		updatePositionAndOrientation();
	}

	@Override
	public RevolvingCamera position(Point3D position) {
		super.position(position);
		return updateSphericalCoordinates();
	}

	@Override
	public RevolvingCamera position(Point3D position, Point3D lookAt) {
		super.position(position, lookAt);
		return updateSphericalCoordinates();
	}

	@Override
	public RevolvingCamera position(Point3D position, Point3D lookAt, Vector3D up) {
		super.position(position, lookAt, up);
		return updateSphericalCoordinates();
	}

	@Override
	public RevolvingCamera slide(double du, double dv, double dn) {
		super.slide(du, dv, dn);
		return updateSphericalCoordinates();
	}

	@Override
	public RevolvingCamera pitch(double angleInRadians) {
		return (RevolvingCamera) super.pitch(angleInRadians);
	}

	@Override
	public RevolvingCamera yaw(double angleInRadians) {
		return (RevolvingCamera) super.yaw(angleInRadians);
	}

	@Override
	public RevolvingCamera roll(double angleInRadians) {
		return (RevolvingCamera) super.roll(angleInRadians);
	}

	@Override
	public RevolvingCamera revolveLongitudinal(double angleInRadians) {
		setLongitudeInRadians(getLongitudeInRadians() + angleInRadians);
		return updatePositionAndOrientation();
	}

	@Override
	public RevolvingCamera revolveLatitudinal(double angleInRadians) {
		setLatitudeInRadians(getLatitudeInRadians() + angleInRadians);
		return updatePositionAndOrientation();
	}

	@Override
	public RevolvingCamera alterDistance(double distance) {
		if (distance < 0 && -distance >= getDistance()) {
			throw new IllegalArgumentException("Not allowed to pass the pivot point");
		}
		Point3D p = getPosition();
		Vector3D r = getOutwardRadialVector().getUnitVector();
		double x = p.getX() + distance * r.getX();
		double y = p.getY() + distance * r.getY();
		double z = p.getZ() + distance * r.getZ();
		doPosition(new Point3D(x, y, z));
		return this;
	}

	private RevolvingCamera updatePositionAndOrientation() {
		double distance = getDistance();
		Point3D pivot = getPivotPoint();
		CompositeTransform ct = new CompositeTransform();
		ct.then(Transformation.getRotationZrollMatrix(getLatitudeInRadians()));
		ct.then(Transformation.getRotationYrollMatrix(-getLongitudeInRadians()));
		ct.then(Transformation.getScalingMatrix(distance, distance, distance));
		ct.then(Transformation.getTranslationMatrix(pivot.getX(), pivot.getY(), pivot.getZ()));
		Point3D p0 = ct.transform(new Point3D(1.0, 0, 0));
		Point3D p1 = ct.transform(new Point3D(1.0, 1.0, 0));
		doPosition(p0, getPivotPoint(), p1.minus(p0));
		return this;
	}

	private RevolvingCamera updateSphericalCoordinates() {
		Vector3D radialOut = getOutwardRadialVector();
		setLongitudeInRadians(radialOut.getLongitudeInRadians());
		setLatitudeInRadians(radialOut.getLatitudeInRadians());
		return this;
	}

	@Override
	public double getDistance() {
		return getOutwardRadialVector().getMagnitude();
	}

	protected Vector3D getOutwardRadialVector() {
		return getPosition().minus(getPivotPoint());
	}

	@Override
	public Point3D getPivotPoint() {
		return pivotPoint;
	}

	@Override
	public void setPivotPoint(Point3D pivotPoint) {
		this.pivotPoint = pivotPoint;
	}

	private double getLongitudeInRadians() {
		return longitudeInRadians;
	}

	private void setLongitudeInRadians(double longitudeInRadians) {
		this.longitudeInRadians = longitudeInRadians;
	}

	private double getLatitudeInRadians() {
		return latitudeInRadians;
	}

	private void setLatitudeInRadians(double latitudeInRadians) {
		this.latitudeInRadians = latitudeInRadians;
	}

}