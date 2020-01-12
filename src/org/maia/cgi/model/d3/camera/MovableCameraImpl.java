package org.maia.cgi.model.d3.camera;

import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.transform.d3.TransformMatrix;
import org.maia.cgi.transform.d3.Transformation;

public class MovableCameraImpl extends BaseCamera implements MovableCamera {

	private Vector3D u;

	private Vector3D v;

	private Vector3D n;

	private TransformMatrix viewingMatrix;

	public MovableCameraImpl(ViewVolume viewVolume) {
		this(Point3D.origin(), new Point3D(0, 0, -1.0), viewVolume);
	}

	public MovableCameraImpl(Point3D position, Point3D lookAt, ViewVolume viewVolume) {
		this(position, lookAt, new Vector3D(0, 1.0, 0), viewVolume);
	}

	public MovableCameraImpl(Point3D position, Point3D lookAt, Vector3D up, ViewVolume viewVolume) {
		super(position, viewVolume);
		doPosition(position, lookAt, up);
	}

	public MovableCamera position(Point3D position) {
		doPosition(position);
		return this;
	}

	public MovableCamera position(Point3D position, Point3D lookAt) {
		return position(position, lookAt, new Vector3D(0, 1.0, 0));
	}

	public MovableCamera position(Point3D position, Point3D lookAt, Vector3D up) {
		doPosition(position, lookAt, up);
		return this;
	}

	protected void doPosition(Point3D position) {
		setPosition(position);
		updateViewingMatrix();
	}

	protected void doPosition(Point3D position, Point3D lookAt, Vector3D up) {
		setPosition(position);
		setN(position.minus(lookAt));
		setU(up.crossProduct(getN()));
		setV(getN().crossProduct(getU()));
		updateViewingMatrix();
	}

	public MovableCamera slide(double du, double dv, double dn) {
		Point3D pos = getPosition();
		pos.setX(pos.getX() + du * getU().getX() + dv * getV().getX() + dn * getN().getX());
		pos.setY(pos.getY() + du * getU().getY() + dv * getV().getY() + dn * getN().getY());
		pos.setZ(pos.getZ() + du * getU().getZ() + dv * getV().getZ() + dn * getN().getZ());
		updateViewingMatrix();
		return this;
	}

	public MovableCamera pitch(double angleInRadians) {
		double cs = Math.cos(angleInRadians);
		double sn = Math.sin(angleInRadians);
		Vector3D V = getV();
		Vector3D N = getN();
		Vector3D oldV = V.clone();
		Vector3D oldN = N.clone();
		V.setX(cs * oldV.getX() + sn * oldN.getX());
		V.setY(cs * oldV.getY() + sn * oldN.getY());
		V.setZ(cs * oldV.getZ() + sn * oldN.getZ());
		N.setX(-sn * oldV.getX() + cs * oldN.getX());
		N.setY(-sn * oldV.getY() + cs * oldN.getY());
		N.setZ(-sn * oldV.getZ() + cs * oldN.getZ());
		updateViewingMatrix();
		return this;
	}

	public MovableCamera yaw(double angleInRadians) {
		double cs = Math.cos(angleInRadians);
		double sn = Math.sin(angleInRadians);
		Vector3D U = getU();
		Vector3D N = getN();
		Vector3D oldU = U.clone();
		Vector3D oldN = N.clone();
		U.setX(cs * oldU.getX() - sn * oldN.getX());
		U.setY(cs * oldU.getY() - sn * oldN.getY());
		U.setZ(cs * oldU.getZ() - sn * oldN.getZ());
		N.setX(sn * oldU.getX() + cs * oldN.getX());
		N.setY(sn * oldU.getY() + cs * oldN.getY());
		N.setZ(sn * oldU.getZ() + cs * oldN.getZ());
		updateViewingMatrix();
		return this;
	}

	public MovableCamera roll(double angleInRadians) {
		double cs = Math.cos(angleInRadians);
		double sn = Math.sin(angleInRadians);
		Vector3D U = getU();
		Vector3D V = getV();
		Vector3D oldU = U.clone();
		Vector3D oldV = V.clone();
		U.setX(cs * oldU.getX() + sn * oldV.getX());
		U.setY(cs * oldU.getY() + sn * oldV.getY());
		U.setZ(cs * oldU.getZ() + sn * oldV.getZ());
		V.setX(-sn * oldU.getX() + cs * oldV.getX());
		V.setY(-sn * oldU.getY() + cs * oldV.getY());
		V.setZ(-sn * oldU.getZ() + cs * oldV.getZ());
		updateViewingMatrix();
		return this;
	}

	private void updateViewingMatrix() {
		getU().makeUnitVector();
		getV().makeUnitVector();
		getN().makeUnitVector();
		setViewingMatrix(Transformation.getCameraViewingMatrix(getPosition(), getU(), getV(), getN()));
		fireCameraHasChanged();
	}

	@Override
	public TransformMatrix getViewingMatrix() {
		return viewingMatrix;
	}

	private void setViewingMatrix(TransformMatrix viewingMatrix) {
		this.viewingMatrix = viewingMatrix;
	}

	private Vector3D getU() {
		return u;
	}

	private void setU(Vector3D u) {
		this.u = u;
	}

	private Vector3D getV() {
		return v;
	}

	private void setV(Vector3D v) {
		this.v = v;
	}

	private Vector3D getN() {
		return n;
	}

	private void setN(Vector3D n) {
		this.n = n;
	}

}