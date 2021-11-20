package org.maia.cgi.model.d3.object;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.maia.cgi.geometry.d2.Point2D;
import org.maia.cgi.geometry.d2.Polygon2D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.metrics.Metrics;
import org.maia.cgi.model.d3.OrthographicProjection;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.shading.d3.FlatShadingModel;

/**
 * A <em>simple face</em> being a finite area in a plane enclosed by a convex polygon
 * 
 * <p>
 * A <em>simple face</em> is specified by a set of vertices, representing the vertices of the polygon which encloses the
 * face. For that matter it extends the <code>PolygonalObject3D</code> class. The traversal order of the vertices is
 * important. When perceived in clockwise direction the face is front-facing, in counter-clockwise direction the face is
 * back-facing.
 * </p>
 * <p>
 * A solid color applies to a <em>simple face</em>, although there can be a separate color for the front and back side
 * of the face, and also the color is subject to a <code>FlatShadingModel</code>.
 * </p>
 * 
 * @see FlatShadingModel
 */
public class SimpleFace3D extends PolygonalObject3D {

	private Color frontColor;

	private Color backColor;

	private FlatShadingModel shadingModel;

	private ProjectionState projectionState;

	public SimpleFace3D(Color color, FlatShadingModel shadingModel, Point3D... vertices) {
		this(color, color, shadingModel, vertices);
	}

	public SimpleFace3D(Color color, FlatShadingModel shadingModel, List<Point3D> vertices) {
		this(color, color, shadingModel, vertices);
	}

	public SimpleFace3D(Color frontColor, Color backColor, FlatShadingModel shadingModel, Point3D... vertices) {
		this(frontColor, backColor, shadingModel, Arrays.asList(vertices));
	}

	public SimpleFace3D(Color frontColor, Color backColor, FlatShadingModel shadingModel, List<Point3D> vertices) {
		super(vertices);
		this.frontColor = frontColor;
		this.backColor = backColor;
		this.shadingModel = shadingModel;
		this.projectionState = new ProjectionState();
	}

	@Override
	protected ObjectSurfacePoint3D sampleSurfacePoint(Point3D positionInCamera, Scene scene, boolean applyShading) {
		ObjectSurfacePoint3D surfacePoint = null;
		if (containsPointInCameraCoordinates(positionInCamera, scene)) {
			Color color = sampleBaseColor(positionInCamera, scene);
			if (color != null) {
				surfacePoint = new ObjectSurfacePoint3DImpl(this, positionInCamera, color);
				if (applyShading) {
					getShadingModel().applyShading(surfacePoint, scene);
				}
			}
		}
		return surfacePoint;
	}

	protected boolean containsPointInCameraCoordinates(Point3D positionInCamera, Scene scene) {
		Metrics.getInstance().incrementPointInsideSimpleFaceChecks();
		ProjectionState ps = getProjectionState();
		ps.setScene(scene);
		return ps.getPolygon().contains(ps.project(positionInCamera)); // inside-test with 2D-projected polygon
	}

	protected Color sampleBaseColor(Point3D positionInCamera, Scene scene) {
		return isFrontFacing(scene) ? getFrontColor() : getBackColor();
	}

	protected boolean isFrontFacing(Scene scene) {
		return getPlaneInCameraCoordinates(scene.getCamera()).getNormalUnitVector().getZ() >= 0;
	}

	@Override
	public void notifySelfHasTransformed() {
		super.notifySelfHasTransformed();
		invalidateProjectionState();
	}

	@Override
	public void notifyAncestorHasTransformed() {
		super.notifyAncestorHasTransformed();
		invalidateProjectionState();
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		super.cameraHasChanged(camera);
		invalidateProjectionState();
	}

	private void invalidateProjectionState() {
		getProjectionState().invalidate();
	}

	public Color getFrontColor() {
		return frontColor;
	}

	public Color getBackColor() {
		return backColor;
	}

	public FlatShadingModel getShadingModel() {
		return shadingModel;
	}

	private ProjectionState getProjectionState() {
		return projectionState;
	}

	private class ProjectionState {

		private OrthographicProjection projection;

		private Polygon2D polygon;

		private Scene scene;

		public ProjectionState() {
			invalidate();
		}

		public void invalidate() {
			projection = null;
			polygon = null;
		}

		public Point2D project(Point3D point) {
			OrthographicProjection projection = getProjection();
			if (OrthographicProjection.ONTO_XY_PLANE.equals(projection)) {
				return new Point2D(point.getX(), point.getY());
			} else if (OrthographicProjection.ONTO_XZ_PLANE.equals(projection)) {
				return new Point2D(point.getX(), point.getZ());
			} else if (OrthographicProjection.ONTO_YZ_PLANE.equals(projection)) {
				return new Point2D(-point.getZ(), point.getY());
			}
			return null;
		}

		public List<Point2D> project(List<Point3D> points) {
			List<Point2D> projected = new Vector<Point2D>(points.size());
			for (Point3D point : points) {
				projected.add(project(point));
			}
			return projected;
		}

		public OrthographicProjection getProjection() {
			if (projection == null) {
				projection = deriveProjection();
			}
			return projection;
		}

		private OrthographicProjection deriveProjection() {
			Vector3D n = getPlaneInCameraCoordinates(getScene().getCamera()).getNormalUnitVector();
			if (Math.abs(n.getLatitudeInRadians()) >= Math.PI / 4)
				return OrthographicProjection.ONTO_XZ_PLANE;
			double lon = n.getLongitudeInRadians();
			if (Math.abs(lon - Math.PI / 2) <= Math.PI / 4)
				return OrthographicProjection.ONTO_XY_PLANE;
			if (Math.abs(lon - 1.5 * Math.PI) <= Math.PI / 4)
				return OrthographicProjection.ONTO_XY_PLANE;
			return OrthographicProjection.ONTO_YZ_PLANE;
		}

		public Polygon2D getPolygon() {
			if (polygon == null) {
				polygon = derivePolygon();
			}
			return polygon;
		}

		private Polygon2D derivePolygon() {
			return new Polygon2D(project(getVerticesInCameraCoordinates(getScene().getCamera())));
		}

		public Scene getScene() {
			return scene;
		}

		public void setScene(Scene scene) {
			if (this.scene != null && !this.scene.equals(scene)) {
				invalidate();
			}
			this.scene = scene;
		}

	}

}