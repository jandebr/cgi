package org.maia.cgi.model.d3.object;

import java.awt.Color;

import org.maia.cgi.geometry.d3.Point3D;

public interface ObjectSurfacePoint3D {

	Object3D getObject();
	
	Point3D getPositionInCamera();
	
	Color getColor();

}
