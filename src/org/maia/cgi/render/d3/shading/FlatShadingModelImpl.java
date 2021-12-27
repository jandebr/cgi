package org.maia.cgi.render.d3.shading;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.maia.cgi.Metrics;
import org.maia.cgi.compose.Compositing;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;
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

	private ThreadLocal<LightRay> reusableLightRay;

	private ThreadLocal<List<ObjectSurfacePoint3D>> reusableIntersectionsList;

	private static ThreadLocal<ObscuredObjectsCache> obscuredObjectsCache;

	private static final double APPROXIMATE_ZERO = 0.000001;

	static {
		obscuredObjectsCache = new ThreadLocal<ObscuredObjectsCache>();
	}

	public FlatShadingModelImpl() {
		this(1.0, 3.0);
	}

	public FlatShadingModelImpl(double lightReflectionFactor, double lightGlossFactor) {
		this.lightReflectionFactor = lightReflectionFactor;
		this.lightGlossFactor = lightGlossFactor;
		this.reusableLightRay = new ThreadLocal<LightRay>();
		this.reusableIntersectionsList = new ThreadLocal<List<ObjectSurfacePoint3D>>();
	}

	@Override
	public void applyShading(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options) {
		Object3D object = surfacePoint.getObject();
		if (object instanceof PolygonalObject3D) {
			Color surfaceColor = surfacePoint.getColor();
			Color shadedColor = applyShading(surfaceColor, surfacePoint.getPositionInCamera(),
					(PolygonalObject3D) object, scene, options);
			surfacePoint.setColor(shadedColor);
		}
	}

	protected Color applyShading(Color surfaceColor, Point3D surfacePositionInCamera, PolygonalObject3D object,
			Scene scene, RenderOptions options) {
		double brightness = computeBrightnessFactor(surfacePositionInCamera, object, scene, options);
		return Compositing.adjustBrightness(surfaceColor, brightness);
	}

	protected double computeBrightnessFactor(Point3D surfacePositionInCamera, PolygonalObject3D object, Scene scene,
			RenderOptions options) {
		double product = 1.0;
		Iterator<LightSource> it = scene.getLightSources().iterator();
		while (it.hasNext()) {
			LightSource lightSource = it.next();
			double lightFactor = computeLightSourceBrightnessFactor(lightSource, surfacePositionInCamera, object,
					scene, options);
			product *= 1.0 - (lightFactor + 1.0) / 2.0;
		}
		return (1.0 - product) * 2.0 - 1.0;
	}

	protected double computeLightSourceBrightnessFactor(LightSource lightSource, Point3D surfacePositionInCamera,
			PolygonalObject3D object, Scene scene, RenderOptions options) {
		LightRay ray = getRayFromSurfacePositionToLightSource(surfacePositionInCamera, lightSource, scene);
		if (ray != null) {
			return computeLightRayBrightnessFactor(ray, object, scene, options);
		} else {
			return computeAmbientLightBrightnessFactor(lightSource);
		}
	}

	protected double computeAmbientLightBrightnessFactor(LightSource light) {
		return light.getBrightness() * getLightReflectionFactor() - 1.0;
	}

	protected double computeLightRayBrightnessFactor(LightRay ray, PolygonalObject3D object, Scene scene,
			RenderOptions options) {
		double lightFactor = -1.0;
		double brightness = ray.getLightSource().getBrightness() * getLightReflectionFactor();
		if (options.isShadowsEnabled()) {
			brightness *= getLightRayTranslucency(ray, object, scene);
		} else {
			brightness *= 0.7; // compensate unrealistic over-lighting of a scene in the absence of shadows
		}
		if (brightness > 0) {
			brightness *= computeLightRayGloss(ray, object, scene, options);
			lightFactor = brightness * 2.0 - 1.0;
		}
		return lightFactor;
	}

	protected double computeLightRayGloss(LightRay ray, PolygonalObject3D object, Scene scene, RenderOptions options) {
		Vector3D rayUnit = ray.getUnitDirection();
		Vector3D normal = object.getPlaneInCameraCoordinates(scene.getCamera()).getNormalUnitVector();
		double alfa = Math.abs(rayUnit.getAngleBetweenUnitVectors(normal) / Math.PI * 2.0 - 1.0);
		return Math.pow(alfa, getLightGlossFactor());
	}

	protected double getLightRayTranslucency(LightRay ray, Object3D object, Scene scene) {
		if (isObscuredFromMemory(ray, object, scene)) {
			return 0; // can exploit local invariance
		} else {
			return computeLightRayTranslucency(ray, object, scene);
		}
	}

	protected boolean isObscuredFromMemory(LightRay ray, Object3D object, Scene scene) {
		boolean obscured = false;
		Object3D candidateObscuringObject = getObscuredObjectsCache().getObscuringObject(object, ray.getLightSource());
		if (candidateObscuringObject != null && candidateObscuringObject.isRaytraceable()) {
			List<ObjectSurfacePoint3D> intersections = getReusableIntersectionsList();
			intersections.clear();
			candidateObscuringObject.asRaytraceableObject().intersectWithLightRay(ray, scene, intersections);
			if (!intersections.isEmpty()) {
				obscured = Compositing.isFullyOpaque(intersections.get(0).getColor());
			}
		}
		return obscured;
	}

	protected double computeLightRayTranslucency(LightRay ray, Object3D object, Scene scene) {
		double translucency = 1.0;
		Point3D surfacePosition = ray.getP1();
		Metrics.getInstance().incrementSurfacePositionToLightSourceTraversals();
		Iterator<ObjectSurfacePoint3D> intersectionsWithRay = scene.getSpatialIndex().getObjectIntersections(ray);
		while (translucency > 0 && intersectionsWithRay.hasNext()) {
			ObjectSurfacePoint3D intersection = intersectionsWithRay.next();
			if (intersection.getObject() != object) {
				double squareDistance = intersection.getPositionInCamera().squareDistanceTo(surfacePosition);
				if (squareDistance >= APPROXIMATE_ZERO) {
					double transparency = Compositing.getTransparency(intersection.getColor());
					translucency *= transparency;
					if (transparency == 0) {
						getObscuredObjectsCache().addToCache(object, ray.getLightSource(), intersection.getObject());
					}
				}
			}
		}
		return translucency;
	}

	private LightRay getRayFromSurfacePositionToLightSource(Point3D surfacePositionInCamera, LightSource lightSource,
			Scene scene) {
		LightRay ray = null;
		if (lightSource.isPositional()) {
			ray = getReusableLightRay();
			ray.setP1(surfacePositionInCamera);
			ray.setP2(((PositionalLightSource) lightSource).getPositionInCamera(scene));
			ray.setLightSource(lightSource);
		} else if (lightSource.isDirectional()) {
			Vector3D v = ((DirectionalLightSource) lightSource).getScaledDirectionOutsideOfScene(scene);
			ray = getReusableLightRay();
			ray.setP1(surfacePositionInCamera);
			ray.setP2(surfacePositionInCamera.minus(v));
			ray.setLightSource(lightSource);
		}
		return ray;
	}

	private LightRay getReusableLightRay() {
		LightRay ray = this.reusableLightRay.get();
		if (ray == null) {
			ray = new LightRay();
			this.reusableLightRay.set(ray);
		}
		return ray;
	}

	private List<ObjectSurfacePoint3D> getReusableIntersectionsList() {
		List<ObjectSurfacePoint3D> list = reusableIntersectionsList.get();
		if (list == null) {
			list = new Vector<ObjectSurfacePoint3D>();
			reusableIntersectionsList.set(list);
		}
		return list;
	}

	public double getLightReflectionFactor() {
		return lightReflectionFactor;
	}

	public double getLightGlossFactor() {
		return lightGlossFactor;
	}

	private static ObscuredObjectsCache getObscuredObjectsCache() {
		ObscuredObjectsCache cache = obscuredObjectsCache.get();
		if (cache == null) {
			cache = new ObscuredObjectsCache();
			obscuredObjectsCache.set(cache);
		}
		return cache;
	}

	private static class LightRay extends LineSegment3D {

		private LightSource lightSource;

		public LightRay() {
			super(Point3D.origin(), Point3D.origin());
		}

		public LightSource getLightSource() {
			return lightSource;
		}

		public void setLightSource(LightSource lightSource) {
			this.lightSource = lightSource;
		}

	}

	private static class ObscuredObjectsCache {

		private Map<Object3D, ObscuredObject3D> objectIndex;

		private int maxObjectSize;

		public ObscuredObjectsCache() {
			this(100);
		}

		public ObscuredObjectsCache(int maxObjectSize) {
			this.objectIndex = new HashMap<Object3D, ObscuredObject3D>(maxObjectSize);
			this.maxObjectSize = maxObjectSize;
		}

		public void addToCache(Object3D obscuredObject, LightSource lightSource, Object3D obscuringObject) {
			ObscuredObject3D entry = getObjectIndex().get(obscuredObject);
			if (entry == null) {
				if (getCurrentObjectSize() == getMaxObjectSize()) {
					getObjectIndex().clear(); // simple reset proved more optimal than LRU overhead
				}
				entry = new ObscuredObject3D(obscuredObject);
				getObjectIndex().put(obscuredObject, entry);
			}
			entry.setObscuringObject(lightSource, obscuringObject);
		}

		public Object3D getObscuringObject(Object3D obscuredObject, LightSource lightSource) {
			Object3D obscuringObject = null;
			ObscuredObject3D entry = getObjectIndex().get(obscuredObject);
			if (entry != null) {
				obscuringObject = entry.getObscuringObject(lightSource);
			}
			return obscuringObject;
		}

		private Map<Object3D, ObscuredObject3D> getObjectIndex() {
			return objectIndex;
		}

		private int getMaxObjectSize() {
			return maxObjectSize;
		}

		private int getCurrentObjectSize() {
			return getObjectIndex().size();
		}

	}

	private static class ObscuredObject3D {

		private Object3D object;

		private Map<LightSource, Object3D> lightIndex;

		public ObscuredObject3D(Object3D object) {
			this.object = object;
			this.lightIndex = new HashMap<LightSource, Object3D>();
		}

		public void setObscuringObject(LightSource lightSource, Object3D obscuringObject) {
			getLightIndex().put(lightSource, obscuringObject);
		}

		public Object3D getObscuringObject(LightSource lightSource) {
			return getLightIndex().get(lightSource);
		}

		public Object3D getObject() {
			return object;
		}

		private Map<LightSource, Object3D> getLightIndex() {
			return lightIndex;
		}

	}

}