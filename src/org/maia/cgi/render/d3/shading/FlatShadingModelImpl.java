package org.maia.cgi.render.d3.shading;

import java.awt.Color;
import java.util.Iterator;

import org.maia.cgi.Metrics;
import org.maia.cgi.compose.Compositing;
import org.maia.cgi.compose.d3.DepthFunction;
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
import org.maia.cgi.render.d3.RenderOptions;

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

	private static final double APPROXIMATE_ZERO = 0.000001;

	public FlatShadingModelImpl() {
		this(1.0, 3.0);
	}

	public FlatShadingModelImpl(double lightReflectionFactor, double lightGlossFactor) {
		this.lightReflectionFactor = lightReflectionFactor;
		this.lightGlossFactor = lightGlossFactor;
	}

	@Override
	public void applyShading(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options) {
		Object3D object = surfacePoint.getObject();
		if (object instanceof PolygonalObject3D) {
			surfacePoint.setColor(recolor(surfacePoint.getColor(), surfacePoint.getPositionInCamera(),
					(PolygonalObject3D) object, scene, options));
		}
	}

	protected Color recolor(Color surfaceColor, Point3D surfacePositionInCamera, PolygonalObject3D object, Scene scene,
			RenderOptions options) {
		Color color = surfaceColor;
		color = Compositing.adjustBrightness(color,
				getBrightnessFactor(surfacePositionInCamera, object, scene, options));
		color = Compositing.adjustBrightness(color, -getDarknessFactorByDepth(surfacePositionInCamera, scene, options));
		return color;
	}

	protected double getBrightnessFactor(Point3D surfacePositionInCamera, PolygonalObject3D object, Scene scene,
			RenderOptions options) {
		Iterator<LightSource> it = scene.getLightSources().iterator();
		double product = 1.0;
		while (it.hasNext()) {
			double lightFactor = getBrightnessFactor(it.next(), surfacePositionInCamera, object, scene, options);
			product *= 1.0 - (lightFactor + 1.0) / 2.0;
		}
		return (1.0 - product) * 2.0 - 1.0;
	}

	protected double getBrightnessFactor(LightSource lightSource, Point3D surfacePositionInCamera,
			PolygonalObject3D object, Scene scene, RenderOptions options) {
		double factor = -1.0;
		double brightness = lightSource.getBrightness() * getLightReflectionFactor();
		if (lightSource instanceof AmbientLight) {
			factor = brightness - 1.0;
		} else {
			LineSegment3D ray = getRayFromSurfacePositionToLightSource(lightSource, surfacePositionInCamera, scene);
			if (ray != null) {
				if (options.isShadowsEnabled()) {
					brightness *= getLightTranslucency(ray, object, scene);
				} else {
					brightness *= 0.7; // compensate unrealistic over-lighting of a scene in the absence of shadows
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
		Metrics.getInstance().incrementSurfacePositionToLightSourceTraversals();
		Iterator<ObjectSurfacePoint3D> intersectionsWithRay = scene.getSpatialIndex().getObjectIntersections(
				rayFromSurfacePositionToLightSource, true);
		while (translucency > 0 && intersectionsWithRay.hasNext()) {
			Metrics.getInstance().incrementSurfacePositionToLightSourceObjectEncounters();
			ObjectSurfacePoint3D intersection = intersectionsWithRay.next();
			double distance = intersection.getPositionInCamera()
					.distanceTo(rayFromSurfacePositionToLightSource.getP1());
			if (intersection.getObject() != self && distance >= APPROXIMATE_ZERO) {
				translucency *= Compositing.getTransparency(intersection.getColor());
			}
		}
		return translucency;
	}

	protected double getDarknessFactorByDepth(Point3D surfacePositionInCamera, Scene scene, RenderOptions options) {
		double darkness = 0;
		DepthFunction df = scene.getDarknessDepthFunction();
		if (options.isDepthDarknessEnabled() && df != null) {
			double depth = -surfacePositionInCamera.getZ();
			darkness = Math.max(Math.min(df.eval(depth), 1.0), 0);
		}
		return darkness;
	}

	public double getLightReflectionFactor() {
		return lightReflectionFactor;
	}

	public double getLightGlossFactor() {
		return lightGlossFactor;
	}

}
