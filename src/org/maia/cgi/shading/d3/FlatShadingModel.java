package org.maia.cgi.shading.d3;

import java.awt.Color;

import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.object.PolygonalObject3D;
import org.maia.cgi.model.d3.scene.Scene;

public interface FlatShadingModel extends ShadingModel {

	Color applyShading(Color surfaceColor, Point3D surfacePositionInCamera, PolygonalObject3D object, Scene scene);

}