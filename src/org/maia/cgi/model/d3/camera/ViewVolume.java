package org.maia.cgi.model.d3.camera;

import org.maia.cgi.geometry.d2.Rectangle2D;
import org.maia.cgi.transform.d3.TransformMatrix;

public interface ViewVolume {

	TransformMatrix getProjectionMatrix();

	double getViewAngleInDegrees();

	double getAspectRatio();

	double getViewPlaneZ();
	
	double getFarPlaneZ();

	Rectangle2D getViewPlaneRectangle();

}