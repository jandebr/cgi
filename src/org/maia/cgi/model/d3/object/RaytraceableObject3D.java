package org.maia.cgi.model.d3.object;

import java.util.Collection;

import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.RenderOptions;
import org.maia.cgi.render.d3.ReusableObjectPack;

public interface RaytraceableObject3D extends Object3D {

	void intersectWithEyeRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			RenderOptions options, ReusableObjectPack reusableObjects);

	void intersectWithLightRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			ReusableObjectPack reusableObjects);

}