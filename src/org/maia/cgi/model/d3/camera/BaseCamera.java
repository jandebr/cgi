package org.maia.cgi.model.d3.camera;

import java.util.Collection;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Point3D;

public abstract class BaseCamera implements Camera {

	private Point3D position;

	private ViewVolume viewVolume;

	private Collection<CameraObserver> observers = new Vector<CameraObserver>();

	protected BaseCamera(Point3D position, ViewVolume viewVolume) {
		this.position = position;
		this.viewVolume = viewVolume;
	}

	@Override
	public void addObserver(CameraObserver observer) {
		getObservers().add(observer);
	}

	@Override
	public void removeObserver(CameraObserver observer) {
		getObservers().remove(observer);
	}

	protected void fireCameraHasChanged() {
		for (CameraObserver observer : getObservers()) {
			observer.cameraHasChanged(this);
		}
	}

	@Override
	public Point3D getPosition() {
		return position;
	}

	protected void setPosition(Point3D position) {
		this.position = position;
	}

	@Override
	public ViewVolume getViewVolume() {
		return viewVolume;
	}

	protected Collection<CameraObserver> getObservers() {
		return observers;
	}

}