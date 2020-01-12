package org.maia.cgi.model.d3.object;

import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;

public interface MeshObject3D extends Object3D {

	Mesh3D getMesh(CoordinateFrame cframe, Camera camera);

}
