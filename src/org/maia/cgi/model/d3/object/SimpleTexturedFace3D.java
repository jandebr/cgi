package org.maia.cgi.model.d3.object;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import org.maia.cgi.compose.Compositing;
import org.maia.cgi.geometry.d2.Rectangle2D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d2.Mask;
import org.maia.cgi.render.d2.TextureMap;
import org.maia.cgi.render.d2.TextureMapHandle;
import org.maia.cgi.render.d2.TextureMapRegistry;
import org.maia.cgi.render.d3.RenderOptions;
import org.maia.cgi.render.d3.ReusableObjectPack;
import org.maia.cgi.render.d3.shading.FlatShadingModel;
import org.maia.cgi.transform.d3.TransformMatrix;
import org.maia.cgi.transform.d3.Transformation;
import org.maia.cgi.transform.d3.TwoWayCompositeTransform;

public class SimpleTexturedFace3D extends SimpleFace3D {

	private TransformMatrix objectToPictureTransformMatrix;

	private TransformMatrix pictureToObjectTransformMatrix;

	private TextureMapHandle pictureMapHandle;

	private TextureMapHandle luminanceMapHandle;

	private TextureMapHandle transparencyMapHandle;

	private Mask pictureMask;

	private Point3D positionInCamera; // cached from last transformation

	private Point3D positionInPicture; // cached from last transformation

	public SimpleTexturedFace3D(FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle,
			PictureRegion pictureRegion) {
		this(shadingModel, pictureMapHandle, pictureRegion, null, null, null);
	}

	public SimpleTexturedFace3D(FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle,
			PictureRegion pictureRegion, Mask pictureMask) {
		this(shadingModel, pictureMapHandle, pictureRegion, null, null, pictureMask);
	}

	public SimpleTexturedFace3D(FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle,
			PictureRegion pictureRegion, TextureMapHandle luminanceMapHandle) {
		this(shadingModel, pictureMapHandle, pictureRegion, luminanceMapHandle, null, null);
	}

	public SimpleTexturedFace3D(FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle,
			PictureRegion pictureRegion, TextureMapHandle luminanceMapHandle, TextureMapHandle transparencyMapHandle,
			Mask pictureMask) {
		this(null, shadingModel, pictureMapHandle, pictureRegion, luminanceMapHandle, transparencyMapHandle,
				pictureMask);
	}

	public SimpleTexturedFace3D(Color pictureColor, FlatShadingModel shadingModel, PictureRegion pictureRegion,
			TextureMapHandle luminanceMapHandle) {
		this(pictureColor, shadingModel, pictureRegion, luminanceMapHandle, null, null);
	}

	public SimpleTexturedFace3D(Color pictureColor, FlatShadingModel shadingModel, PictureRegion pictureRegion,
			TextureMapHandle luminanceMapHandle, TextureMapHandle transparencyMapHandle, Mask pictureMask) {
		this(pictureColor, shadingModel, null, pictureRegion, luminanceMapHandle, transparencyMapHandle, pictureMask);
	}

	private SimpleTexturedFace3D(Color pictureColor, FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle,
			PictureRegion pictureRegion, TextureMapHandle luminanceMapHandle, TextureMapHandle transparencyMapHandle,
			Mask pictureMask) {
		super(pictureColor, shadingModel, createCanonicalVertices());
		this.objectToPictureTransformMatrix = pictureRegion.createObjectToPictureTransformMatrix();
		this.pictureToObjectTransformMatrix = pictureRegion.createPictureToObjectTransformMatrix();
		this.pictureMapHandle = pictureMapHandle;
		this.luminanceMapHandle = luminanceMapHandle;
		this.transparencyMapHandle = transparencyMapHandle;
		this.pictureMask = pictureMask;
	}

	private static List<Point3D> createCanonicalVertices() {
		// Vertices in XZ-plane
		List<Point3D> vertices = new Vector<Point3D>(4);
		vertices.add(new Point3D(-1.0, 0, -1.0));
		vertices.add(new Point3D(-1.0, 0, 1.0));
		vertices.add(new Point3D(1.0, 0, 1.0));
		vertices.add(new Point3D(1.0, 0, -1.0));
		return vertices;
	}

	private static TransformMatrix createObjectToPictureTransformMatrix(int pictureWidth, int pictureHeight) {
		TwoWayCompositeTransform ct = new TwoWayCompositeTransform();
		ct.then(Transformation.getScalingMatrix(2.0 / pictureWidth, 1.0, 2.0 / pictureHeight));
		ct.then(Transformation.getTranslationMatrix(-1.0, 0, -1.0));
		return ct.getReverseCompositeMatrix();
	}

	@Override
	protected boolean containsPointOnPlane(Point3D positionInCamera, Scene scene) {
		if (!super.containsPointOnPlane(positionInCamera, scene))
			return false;
		if (getPictureMask() == null)
			return true;
		Point3D picturePosition = fromCameraToPictureCoordinates(positionInCamera, scene.getCamera());
		return !getPictureMask().isMasked(picturePosition.getX(), picturePosition.getZ());
	}

	@Override
	protected void colorSurfacePointHitByRay(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options,
			ReusableObjectPack reusableObjects, boolean applyShading) {
		super.colorSurfacePointHitByRay(surfacePoint, scene, options, reusableObjects, applyShading);
		applyTransparency(surfacePoint, scene);
	}

	@Override
	protected Color sampleBaseColor(Point3D positionInCamera, Scene scene) {
		TextureMap map = getPictureMap();
		if (map != null) {
			Point3D picturePosition = fromCameraToPictureCoordinates(positionInCamera, scene.getCamera());
			return map.sampleColor(picturePosition.getX(), picturePosition.getZ());
		} else {
			return getFrontColor();
		}
	}

	@Override
	protected void applySurfacePointShading(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options,
			ReusableObjectPack reusableObjects) {
		super.applySurfacePointShading(surfacePoint, scene, options, reusableObjects);
		applyLuminance(surfacePoint, scene);
	}

	protected void applyLuminance(ObjectSurfacePoint3D surfacePoint, Scene scene) {
		double luminance = sampleLuminance(surfacePoint, scene);
		if (!Double.isNaN(luminance)) {
			surfacePoint.setColor(Compositing.adjustBrightness(surfacePoint.getColor(), luminance));
		}
	}

	protected double sampleLuminance(ObjectSurfacePoint3D surfacePoint, Scene scene) {
		TextureMap map = getLuminanceMap();
		if (map != null) {
			Point3D picturePosition = fromCameraToPictureCoordinates(surfacePoint.getPositionInCamera(),
					scene.getCamera());
			double luminance = map.sampleDouble(picturePosition.getX(), picturePosition.getZ());
			return luminance * 2.0 - 1.0;
		}
		return Double.NaN;
	}

	protected void applyTransparency(ObjectSurfacePoint3D surfacePoint, Scene scene) {
		double transparency = sampleTransparency(surfacePoint, scene);
		if (!Double.isNaN(transparency)) {
			surfacePoint.setColor(Compositing.setTransparency(surfacePoint.getColor(), transparency));
		}
	}

	protected double sampleTransparency(ObjectSurfacePoint3D surfacePoint, Scene scene) {
		TextureMap map = getTransparencyMap();
		if (map != null) {
			Point3D picturePosition = fromCameraToPictureCoordinates(surfacePoint.getPositionInCamera(),
					scene.getCamera());
			return map.sampleDouble(picturePosition.getX(), picturePosition.getZ());
		}
		return Double.NaN;
	}

	protected Point3D fromCameraToPictureCoordinates(Point3D point, Camera camera) {
		if (!point.equals(positionInCamera)) {
			positionInCamera = point.clone();
			positionInPicture = fromObjectToPictureCoordinates(fromCameraToObjectCoordinates(point, camera));
		}
		return positionInPicture;
	}

	protected Point3D fromObjectToPictureCoordinates(Point3D point) {
		return getObjectToPictureTransformMatrix().transform(point);
	}

	@Override
	public void notifySelfHasTransformed() {
		super.notifySelfHasTransformed();
		invalidateCachedPositionMapping();
	}

	@Override
	public void notifyAncestorHasTransformed() {
		super.notifyAncestorHasTransformed();
		invalidateCachedPositionMapping();
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		super.cameraHasChanged(camera);
		invalidateCachedPositionMapping();
	}

	private void invalidateCachedPositionMapping() {
		this.positionInCamera = null;
		this.positionInPicture = null;
	}

	protected TextureMap getPictureMap() {
		return getPictureMapHandle() == null ? null
				: TextureMapRegistry.getInstance().getTextureMap(getPictureMapHandle());
	}

	protected TextureMap getLuminanceMap() {
		return getLuminanceMapHandle() == null ? null
				: TextureMapRegistry.getInstance().getTextureMap(getLuminanceMapHandle());
	}

	protected TextureMap getTransparencyMap() {
		return getTransparencyMapHandle() == null ? null
				: TextureMapRegistry.getInstance().getTextureMap(getTransparencyMapHandle());
	}

	private TransformMatrix getObjectToPictureTransformMatrix() {
		return objectToPictureTransformMatrix;
	}

	protected TransformMatrix getPictureToObjectTransformMatrix() {
		return pictureToObjectTransformMatrix;
	}

	private TextureMapHandle getPictureMapHandle() {
		return pictureMapHandle;
	}

	private TextureMapHandle getLuminanceMapHandle() {
		return luminanceMapHandle;
	}

	private TextureMapHandle getTransparencyMapHandle() {
		return transparencyMapHandle;
	}

	protected Mask getPictureMask() {
		return pictureMask;
	}

	public static class PictureRegion extends Rectangle2D {

		public PictureRegion(int width, int height) {
			super(width, height);
		}

		public PictureRegion(double width, double height) {
			super(width, height);
		}

		public PictureRegion(int x1, int x2, int y1, int y2) {
			super(x1, x2, y1, y2);
		}

		public PictureRegion(double x1, double x2, double y1, double y2) {
			super(x1, x2, y1, y2);
		}

		public TransformMatrix createObjectToPictureTransformMatrix() {
			return createPictureToObjectTransform().getReverseCompositeMatrix();
		}

		public TransformMatrix createPictureToObjectTransformMatrix() {
			return createPictureToObjectTransform().getForwardCompositeMatrix();
		}

		private TwoWayCompositeTransform createPictureToObjectTransform() {
			TwoWayCompositeTransform ct = new TwoWayCompositeTransform();
			// picture in XZ-plane (iso XY)
			double x1 = getX1();
			double x2 = getX2();
			double z1 = getY1();
			double z2 = getY2();
			ct.then(Transformation.getTranslationMatrix(-(x1 + x2) / 2.0, 0, -(z1 + z2) / 2.0));
			ct.then(Transformation.getScalingMatrix(2.0 / (x2 - x1), 1.0, 2.0 / (z2 - z1)));
			return ct;
		}

	}

}