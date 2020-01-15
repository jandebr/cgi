package org.maia.cgi.model.d3.object;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import org.maia.cgi.compose.Compositing;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.shading.d2.Mask;
import org.maia.cgi.shading.d2.TextureMap;
import org.maia.cgi.shading.d2.TextureMapHandle;
import org.maia.cgi.shading.d2.TextureMapRegistry;
import org.maia.cgi.shading.d3.FlatShadingModel;
import org.maia.cgi.transform.d3.TransformMatrix;
import org.maia.cgi.transform.d3.Transformation;
import org.maia.cgi.transform.d3.TwoWayCompositeTransform;

public class SimpleTexturedFace3D extends SimpleFace3D {

	private TransformMatrix objectToPictureTransformMatrix;

	private TextureMapHandle pictureMapHandle;

	private TextureMapHandle luminanceMapHandle;

	private TextureMapHandle transparencyMapHandle;

	private Mask pictureMask;

	private Point3D positionInCamera; // cached from last transformation

	private Point3D positionInPicture; // cached from last transformation

	public SimpleTexturedFace3D(FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle, int pictureWidth,
			int pictureHeight) {
		this(shadingModel, pictureMapHandle, pictureWidth, pictureHeight, null, null, null);
	}

	public SimpleTexturedFace3D(FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle, int pictureWidth,
			int pictureHeight, Mask pictureMask) {
		this(shadingModel, pictureMapHandle, pictureWidth, pictureHeight, null, null, pictureMask);
	}

	public SimpleTexturedFace3D(FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle, int pictureWidth,
			int pictureHeight, TextureMapHandle luminanceMapHandle) {
		this(shadingModel, pictureMapHandle, pictureWidth, pictureHeight, luminanceMapHandle, null, null);
	}

	public SimpleTexturedFace3D(FlatShadingModel shadingModel, TextureMapHandle pictureMapHandle, int pictureWidth,
			int pictureHeight, TextureMapHandle luminanceMapHandle, TextureMapHandle transparencyMapHandle,
			Mask pictureMask) {
		super(null, shadingModel, createCanonicalVertices());
		this.objectToPictureTransformMatrix = createObjectToPictureTransformMatrix(pictureWidth, pictureHeight);
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
	protected ObjectSurfacePoint3D sampleSurfacePoint(Point3D positionInCamera, Scene scene, boolean applyShading) {
		ObjectSurfacePoint3D surfacePoint = super.sampleSurfacePoint(positionInCamera, scene, applyShading);
		if (surfacePoint != null && applyShading) {
			applyLuminance(surfacePoint, scene);
			applyTransparency(surfacePoint, scene);
		}
		return surfacePoint;
	}

	@Override
	protected boolean containsPointInCameraCoordinates(Point3D positionInCamera, Scene scene) {
		if (!super.containsPointInCameraCoordinates(positionInCamera, scene))
			return false;
		if (getPictureMask() == null)
			return true;
		Point3D picturePosition = fromCameraToPictureCoordinates(positionInCamera, scene.getCamera());
		return !getPictureMask().isMasked(picturePosition.getX(), picturePosition.getZ());
	}

	@Override
	protected Color sampleBaseColor(Point3D positionInCamera, Scene scene) {
		Point3D picturePosition = fromCameraToPictureCoordinates(positionInCamera, scene.getCamera());
		return getPictureMap().sampleColor(picturePosition.getX(), picturePosition.getZ());
	}

	protected void applyLuminance(ObjectSurfacePoint3D surfacePoint, Scene scene) {
		TextureMap map = getLuminanceMap();
		if (map != null) {
			Point3D picturePosition = fromCameraToPictureCoordinates(surfacePoint.getPositionInCamera(),
					scene.getCamera());
			double luminance = map.sampleDouble(picturePosition.getX(), picturePosition.getZ());
			double factor = luminance * 2.0 - 1.0;
			surfacePoint.setColor(Compositing.adjustBrightness(surfacePoint.getColor(), factor));
		}
	}

	protected void applyTransparency(ObjectSurfacePoint3D surfacePoint, Scene scene) {
		TextureMap map = getTransparencyMap();
		if (map != null) {
			Point3D picturePosition = fromCameraToPictureCoordinates(surfacePoint.getPositionInCamera(),
					scene.getCamera());
			double transparency = map.sampleDouble(picturePosition.getX(), picturePosition.getZ());
			surfacePoint.setColor(Compositing.setTransparency(surfacePoint.getColor(), transparency));
		}
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
		return TextureMapRegistry.getInstance().getTextureMap(getPictureMapHandle());
	}

	protected TextureMap getLuminanceMap() {
		return getLuminanceMapHandle() == null ? null : TextureMapRegistry.getInstance().getTextureMap(
				getLuminanceMapHandle());
	}

	protected TextureMap getTransparencyMap() {
		return getTransparencyMapHandle() == null ? null : TextureMapRegistry.getInstance().getTextureMap(
				getTransparencyMapHandle());
	}

	private TransformMatrix getObjectToPictureTransformMatrix() {
		return objectToPictureTransformMatrix;
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

}