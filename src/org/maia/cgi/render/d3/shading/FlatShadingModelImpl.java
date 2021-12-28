package org.maia.cgi.render.d3.shading;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.maia.cgi.Metrics;
import org.maia.cgi.compose.Compositing;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.model.d3.light.DirectionalLightSource;
import org.maia.cgi.model.d3.light.LightRaySegment;
import org.maia.cgi.model.d3.light.LightSource;
import org.maia.cgi.model.d3.light.PositionalLightSource;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.object.PolygonalObject3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.RenderOptions;
import org.maia.cgi.render.d3.ReusableObjectPack;

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
	public void applyShading(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options,
			ReusableObjectPack reusableObjects) {
		Object3D object = surfacePoint.getObject();
		if (object instanceof PolygonalObject3D) {
			Color surfaceColor = surfacePoint.getColor();
			Color shadedColor = applyShading(surfaceColor, surfacePoint.getPositionInCamera(),
					(PolygonalObject3D) object, scene, options, reusableObjects);
			surfacePoint.setColor(shadedColor);
		}
	}

	protected Color applyShading(Color surfaceColor, Point3D surfacePositionInCamera, PolygonalObject3D object,
			Scene scene, RenderOptions options, ReusableObjectPack reusableObjects) {
		double brightness = computeBrightnessFactor(surfacePositionInCamera, object, scene, options, reusableObjects);
		return Compositing.adjustBrightness(surfaceColor, brightness);
	}

	protected double computeBrightnessFactor(Point3D surfacePositionInCamera, PolygonalObject3D object, Scene scene,
			RenderOptions options, ReusableObjectPack reusableObjects) {
		double product = 1.0;
		Iterator<LightSource> it = scene.getLightSources().iterator();
		while (it.hasNext()) {
			LightSource lightSource = it.next();
			double lightFactor = computeLightSourceBrightnessFactor(lightSource, surfacePositionInCamera, object,
					scene, options, reusableObjects);
			product *= 1.0 - (lightFactor + 1.0) / 2.0;
		}
		return (1.0 - product) * 2.0 - 1.0;
	}

	protected double computeLightSourceBrightnessFactor(LightSource lightSource, Point3D surfacePositionInCamera,
			PolygonalObject3D object, Scene scene, RenderOptions options, ReusableObjectPack reusableObjects) {
		LightRaySegment ray = getRayFromSurfacePositionToLightSource(surfacePositionInCamera, lightSource, scene,
				reusableObjects);
		if (ray != null) {
			return computeLightRayBrightnessFactor(ray, object, scene, options, reusableObjects);
		} else {
			return computeAmbientLightBrightnessFactor(lightSource);
		}
	}

	protected double computeAmbientLightBrightnessFactor(LightSource light) {
		return light.getBrightness() * getLightReflectionFactor() - 1.0;
	}

	protected double computeLightRayBrightnessFactor(LightRaySegment ray, PolygonalObject3D object, Scene scene,
			RenderOptions options, ReusableObjectPack reusableObjects) {
		double lightFactor = -1.0;
		double brightness = ray.getLightSource().getBrightness() * getLightReflectionFactor();
		if (options.isShadowsEnabled()) {
			brightness *= getLightRayTranslucency(ray, object, scene, reusableObjects);
		} else {
			brightness *= 0.7; // compensate unrealistic over-lighting of a scene in the absence of shadows
		}
		if (brightness > 0) {
			brightness *= computeLightRayGloss(ray, object, scene, options);
			lightFactor = brightness * 2.0 - 1.0;
		}
		return lightFactor;
	}

	protected double computeLightRayGloss(LightRaySegment ray, PolygonalObject3D object, Scene scene,
			RenderOptions options) {
		Vector3D rayUnit = ray.getUnitDirection();
		Vector3D normal = object.getPlaneInCameraCoordinates(scene.getCamera()).getNormalUnitVector();
		double alfa = Math.abs(rayUnit.getAngleBetweenUnitVectors(normal) / Math.PI * 2.0 - 1.0);
		return Math.pow(alfa, getLightGlossFactor());
	}

	protected double getLightRayTranslucency(LightRaySegment ray, Object3D object, Scene scene,
			ReusableObjectPack reusableObjects) {
		if (isObscuredFromMemory(ray, object, scene, reusableObjects)) {
			return 0; // can exploit local invariance
		} else {
			return computeLightRayTranslucency(ray, object, scene, reusableObjects);
		}
	}

	protected boolean isObscuredFromMemory(LightRaySegment ray, Object3D object, Scene scene,
			ReusableObjectPack reusableObjects) {
		boolean obscured = false;
		Object3D candidateObscuringObject = reusableObjects.getObscuredObjectsCache().getObscuringObject(object,
				ray.getLightSource());
		if (candidateObscuringObject != null && candidateObscuringObject.isRaytraceable()) {
			List<ObjectSurfacePoint3D> intersections = reusableObjects.getEmptiedIntersectionsList();
			candidateObscuringObject.asRaytraceableObject().intersectWithLightRay(ray, scene, intersections,
					reusableObjects);
			if (!intersections.isEmpty()) {
				obscured = Compositing.isFullyOpaque(intersections.get(0).getColor());
			}
		}
		return obscured;
	}

	protected double computeLightRayTranslucency(LightRaySegment ray, Object3D object, Scene scene,
			ReusableObjectPack reusableObjects) {
		double translucency = 1.0;
		Point3D surfacePosition = ray.getP1();
		Metrics.getInstance().incrementSurfacePositionToLightSourceTraversals();
		Iterator<ObjectSurfacePoint3D> intersectionsWithRay = scene.getSpatialIndex().getObjectIntersections(ray,
				reusableObjects);
		while (translucency > 0 && intersectionsWithRay.hasNext()) {
			ObjectSurfacePoint3D intersection = intersectionsWithRay.next();
			if (intersection.getObject() != object) {
				double squareDistance = intersection.getPositionInCamera().squareDistanceTo(surfacePosition);
				if (squareDistance >= APPROXIMATE_ZERO) {
					double transparency = Compositing.getTransparency(intersection.getColor());
					translucency *= transparency;
					if (transparency == 0) {
						reusableObjects.getObscuredObjectsCache().addToCache(object, ray.getLightSource(),
								intersection.getObject());
					}
				}
			}
		}
		return translucency;
	}

	private LightRaySegment getRayFromSurfacePositionToLightSource(Point3D surfacePositionInCamera,
			LightSource lightSource, Scene scene, ReusableObjectPack reusableObjects) {
		LightRaySegment ray = null;
		if (lightSource.isPositional()) {
			ray = reusableObjects.getLightRay();
			ray.setP1(surfacePositionInCamera);
			ray.setP2(((PositionalLightSource) lightSource).getPositionInCamera(scene));
			ray.setLightSource(lightSource);
		} else if (lightSource.isDirectional()) {
			Vector3D v = ((DirectionalLightSource) lightSource).getScaledDirectionOutsideOfScene(scene);
			ray = reusableObjects.getLightRay();
			ray.setP1(surfacePositionInCamera);
			ray.setP2(surfacePositionInCamera.minus(v));
			ray.setLightSource(lightSource);
		}
		return ray;
	}

	public double getLightReflectionFactor() {
		return lightReflectionFactor;
	}

	public double getLightGlossFactor() {
		return lightGlossFactor;
	}

}