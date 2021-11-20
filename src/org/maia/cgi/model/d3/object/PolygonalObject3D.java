package org.maia.cgi.model.d3.object;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Plane3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.object.Mesh3D.Edge;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.RenderOptions;

/**
 * An object in 3D space that has the geometrical shape of a convex polygon
 *
 * <p>
 * A polygon is made up of <em>n</em> vertices and <em>n</em> edges, connecting the vertices by line segments.
 * </p>
 * <p>
 * The following assumptions hold for a <code>PolygonalObject3D</code>
 * <ul>
 * <li>The vertices all lie in the same plane</li>
 * <li>The polygon is <em>convex</em>, meaning the angle between adjacent edges must be &lt;= 180 degrees. This implies
 * it is a <em>simple</em> polygon, which does not intersect itself and has no holes</li>
 * </ul>
 * </p>
 */
public class PolygonalObject3D extends VertexObject3D {

	private Plane3D planeCamera; // in camera coordinates

	private static Map<Integer, List<Edge>> reusableEdgesMap = new HashMap<Integer, List<Edge>>();

	private static final double APPROXIMATE_ZERO = 0.000001;

	public PolygonalObject3D(Point3D... vertices) {
		this(Arrays.asList(vertices));
	}

	public PolygonalObject3D(List<Point3D> vertices) {
		super(vertices, getPolygonEdges(vertices));
	}

	private static List<Edge> getPolygonEdges(List<Point3D> vertices) {
		Integer cacheKey = vertices.size();
		List<Edge> edges = reusableEdgesMap.get(cacheKey);
		if (edges == null) {
			edges = createPolygonEdges(vertices);
			reusableEdgesMap.put(cacheKey, edges);
		}
		return edges;
	}

	private static List<Edge> createPolygonEdges(List<Point3D> vertices) {
		int n = vertices.size();
		List<Edge> edges = new Vector<Edge>(n);
		for (int i = 0; i < n; i++) {
			int j = (i + 1) % n;
			edges.add(new Mesh3DImpl.EdgeImpl(i, j));
		}
		return edges;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("PolygonalObject3D {\n");
		for (Point3D vertex : getVerticesInObjectCoordinates()) {
			sb.append('\t').append(vertex).append('\n');
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	protected void intersectSelfWithRayImpl(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			RenderOptions options, boolean applyShading) {
		Point3D position = ray.intersect(getPlaneInCameraCoordinates(scene.getCamera()));
		if (position != null) {
			if (insideBoundingBox(position, scene)) {
				ObjectSurfacePoint3D point = sampleSurfacePoint(position, scene, options, applyShading);
				if (point != null) {
					intersections.add(point);
				}
			}
		}
	}

	protected boolean insideBoundingBox(Point3D positionInCamera, Scene scene) {
		Box3D bbox = getBoundingBox(CoordinateFrame.CAMERA, scene.getCamera());
		if (bbox.getWidth() <= APPROXIMATE_ZERO || bbox.getHeight() <= APPROXIMATE_ZERO
				|| bbox.getDepth() <= APPROXIMATE_ZERO) {
			// For planes perpendicular to a side of the view volume, finite precision computation requires a more
			// conservative bounding box insideness check
			double x1 = positionInCamera.getX() - APPROXIMATE_ZERO;
			double x2 = positionInCamera.getX() + APPROXIMATE_ZERO;
			double y1 = positionInCamera.getY() - APPROXIMATE_ZERO;
			double y2 = positionInCamera.getY() + APPROXIMATE_ZERO;
			double z1 = positionInCamera.getZ() - APPROXIMATE_ZERO;
			double z2 = positionInCamera.getZ() + APPROXIMATE_ZERO;
			return bbox.overlaps(new Box3D(x1, x2, y1, y2, z1, z2));
		}
		return bbox.contains(positionInCamera);
	}

	protected ObjectSurfacePoint3D sampleSurfacePoint(Point3D positionInCamera, Scene scene, RenderOptions options,
			boolean applyShading) {
		return null; // Subclasses should override this for ray tracing
	}

	public Plane3D getPlaneInCameraCoordinates(Camera camera) {
		if (planeCamera == null) {
			planeCamera = derivePlaneInCameraCoordinates(camera);
		}
		return planeCamera;
	}

	private Plane3D derivePlaneInCameraCoordinates(Camera camera) {
		List<Point3D> vertices = getVerticesInCameraCoordinates(camera);
		return new Plane3D(vertices.get(0), vertices.get(1), vertices.get(2));
	}

	@Override
	public void notifySelfHasTransformed() {
		super.notifySelfHasTransformed();
		invalidatePlane();
	}

	@Override
	public void notifyAncestorHasTransformed() {
		super.notifyAncestorHasTransformed();
		invalidatePlane();
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		super.cameraHasChanged(camera);
		invalidatePlane();
	}

	private void invalidatePlane() {
		planeCamera = null;
	}

}
