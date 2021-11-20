package org.maia.cgi.shading.d3;

import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.RenderOptions;

public interface FlatShadingModel extends ShadingModel {

	void applyShading(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options);

}