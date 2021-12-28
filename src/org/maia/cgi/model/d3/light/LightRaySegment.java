package org.maia.cgi.model.d3.light;

import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Point3D;

public class LightRaySegment extends LineSegment3D {

	private LightSource lightSource;

	public LightRaySegment() {
		super(Point3D.origin(), Point3D.origin());
	}

	public LightSource getLightSource() {
		return lightSource;
	}

	public void setLightSource(LightSource lightSource) {
		this.lightSource = lightSource;
	}

}