package org.maia.cgi.shading.d3;

import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.scene.Scene;

public interface FlatShadingModel extends ShadingModel {

	void applyShading(ObjectSurfacePoint3D surfacePoint, Scene scene);

}