package org.maia.cgi.model.d3.camera;

import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.transform.d3.TransformMatrix;

public interface Camera {

	TransformMatrix getViewingMatrix();

	ViewVolume getViewVolume();

	Point3D getPosition();

	void addObserver(CameraObserver observer);

	void removeObserver(CameraObserver observer);

}