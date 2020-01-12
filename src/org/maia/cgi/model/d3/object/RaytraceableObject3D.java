package org.maia.cgi.model.d3.object;

import java.util.Collection;

import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.model.d3.scene.Scene;

public interface RaytraceableObject3D extends Object3D {

	void intersectWithRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			boolean applyShading);

}