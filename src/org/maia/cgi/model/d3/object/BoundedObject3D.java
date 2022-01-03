package org.maia.cgi.model.d3.object;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;

public interface BoundedObject3D extends Object3D {

	Box3D getBoundingBox(CoordinateFrame cframe, Camera camera);

	Box3D getBoundingBoxInObjectCoordinates();

	Box3D getBoundingBoxInWorldCoordinates();

	Box3D getBoundingBoxInCameraCoordinates(Camera camera);

	Box3D getBoundingBoxInViewVolumeCoordinates(Camera camera);

}