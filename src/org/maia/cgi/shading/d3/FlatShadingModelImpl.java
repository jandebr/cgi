package org.maia.cgi.shading.d3;

import java.awt.Color;
import java.util.Iterator;

import org.maia.cgi.compose.Compositing;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.model.d3.light.AmbientLight;
import org.maia.cgi.model.d3.light.DirectionalLightSource;
import org.maia.cgi.model.d3.light.LightSource;
import org.maia.cgi.model.d3.light.PositionalLightSource;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.object.PolygonalObject3D;
import org.maia.cgi.model.d3.scene.Scene;

public class FlatShadingModelImpl implements FlatShadingModel {

	/**
	 * Shading parameter expressing the <em>reflection</em> of light on a surface, ranging from 0 (no reflection) to 1
	 * (maximum reflection).
	 */
	private double lightReflectionFactor;

	/**
	 * Shading parameter expressing the <em>light gloss</em> of a surface, a strictly positive number (&gt; 0) where
	 * higher values imply a more glossy appearance.
	 */
	private double lightGlossFactor;

	public FlatShadingModelImpl() {
		this(1.0, 3.0);
	}

	public FlatShadingModelImpl(double lightReflectionFactor, double lightGlossFactor) {
		this.lightReflectionFactor = lightReflectionFactor;
		this.lightGlossFactor = lightGlossFactor;
	}

	@Override
	public Color applyShading(Color surfaceColor, Point3D surfacePositionInCamera, PolygonalObject3D object, Scene scene) {
		return Compositing.adjustBrightness(surfaceColor, getBrightnessFactor(surfacePositionInCamera, object, scene));
	}

	protected double getBrightnessFactor(Point3D surfacePositionInCamera, PolygonalObject3D object, Scene scene) {
		double factor = -1.0;
		Iterator<LightSource> it = scene.getLightSources().iterator();
		while (factor < 1.0 && it.hasNext()) {
			factor = Math.max(factor, getBrightnessFactor(it.next(), surfacePositionInCamera, object, scene));
		}
		return (factor + 1.0) * getLightReflectionFactor() - 1.0;
	}

	protected double getBrightnessFactor(LightSource lightSource, Point3D surfacePositionInCamera,
			PolygonalObject3D object, Scene scene) {
		double factor = -1.0;
		double brightness = lightSource.getBrightness();
		if (lightSource instanceof AmbientLight) {
			factor = brightness - 1.0;
		} else {
			LineSegment3D ray = getRayFromSurfacePositionToLightSource(lightSource, surfacePositionInCamera, scene);
			if (ray != null) {
				if (scene.getRenderParameters().isShadowsEnabled()) {
					brightness *= getLightTranslucency(ray, object, scene);
				}
				if (brightness > 0) {
					Vector3D normal = object.getPlaneInCameraCoordinates(scene.getCamera()).getNormalUnitVector();
					brightness *= Math.pow(Math.abs(ray.getDirection().getAngleBetween(normal) / Math.PI * 2.0 - 1.0),
							getLightGlossFactor());
					factor = brightness * 2.0 - 1.0;
				}
			}
		}
		return factor;
	}

	private LineSegment3D getRayFromSurfacePositionToLightSource(LightSource lightSource,
			Point3D surfacePositionInCamera, Scene scene) {
		LineSegment3D ray = null;
		if (lightSource instanceof PositionalLightSource) {
			ray = new LineSegment3D(surfacePositionInCamera,
					((PositionalLightSource) lightSource).getPositionInCamera(scene));
		} else if (lightSource instanceof DirectionalLightSource) {
			Vector3D v = ((DirectionalLightSource) lightSource).getScaledDirectionOutsideOfScene(scene);
			ray = new LineSegment3D(surfacePositionInCamera, surfacePositionInCamera.minus(v));
		}
		return ray;
	}

	protected double getLightTranslucency(LineSegment3D rayFromSurfacePositionToLightSource, Object3D self, Scene scene) {
		double translucency = 1.0;
		Iterator<ObjectSurfacePoint3D> intersectionsWithRay = scene.getSpatialIndex().getObjectIntersections(
				rayFromSurfacePositionToLightSource, true);
		while (translucency > 0 && intersectionsWithRay.hasNext()) {
			ObjectSurfacePoint3D intersection = intersectionsWithRay.next();
			if (intersection.getObject() != self) {
				translucency *= Compositing.getTransparency(intersection.getColor());
			}
		}
		return translucency;
	}

	public double getLightReflectionFactor() {
		return lightReflectionFactor;
	}

	public double getLightGlossFactor() {
		return lightGlossFactor;
	}

}
