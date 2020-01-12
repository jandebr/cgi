package org.maia.cgi.model.d3.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.metrics.Metrics;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.object.Mesh3D.Edge;
import org.maia.cgi.transform.d3.Transformation;

public abstract class VertexObject3D extends BaseObject3D implements MeshObject3D {

	private Map<CoordinateFrame, Mesh3D> meshes = new HashMap<CoordinateFrame, Mesh3D>(5);

	protected VertexObject3D(List<Point3D> vertices, List<Edge> edges) {
		meshes.put(CoordinateFrame.OBJECT, new Mesh3DImpl(vertices, edges));
	}

	@Override
	public final boolean isComposite() {
		return false;
	}

	@Override
	public final <T extends ComposableObject3D> CompositeObject3D<T> asCompositeObject() {
		throw new ClassCastException();
	}

	@Override
	protected Box3D deriveBoundingBox(CoordinateFrame cframe, Camera camera) {
		Box3D bbox = null;
		List<Point3D> vertices = getVertices(cframe, camera);
		if (!vertices.isEmpty()) {
			Metrics.getInstance().incrementBoundingBoxComputations();
			Point3D vertex = vertices.get(0);
			double x1 = vertex.getX();
			double x2 = x1;
			double y1 = vertex.getY();
			double y2 = y1;
			double z1 = vertex.getZ();
			double z2 = z1;
			for (int i = 1; i < vertices.size(); i++) {
				vertex = vertices.get(i);
				double x = vertex.getX();
				double y = vertex.getY();
				double z = vertex.getZ();
				x1 = Math.min(x1, x);
				x2 = Math.max(x2, x);
				y1 = Math.min(y1, y);
				y2 = Math.max(y2, y);
				z1 = Math.min(z1, z);
				z2 = Math.max(z2, z);
			}
			bbox = new Box3D(x1, x2, y1, y2, z1, z2);
		}
		return bbox;
	}

	public int getVertexCount() {
		return getVerticesInObjectCoordinates().size();
	}

	@Override
	public Mesh3D getMesh(CoordinateFrame cframe, Camera camera) {
		Mesh3D mesh = meshes.get(cframe);
		if (mesh == null) {
			getVertices(cframe, camera); // as a side effect, also sets the mesh
			mesh = meshes.get(cframe);
		}
		return mesh;
	}

	protected List<Point3D> getVertices(CoordinateFrame cframe, Camera camera) {
		List<Point3D> vertices = null;
		if (cframe.equals(CoordinateFrame.OBJECT)) {
			vertices = getVerticesInObjectCoordinates();
		} else if (cframe.equals(CoordinateFrame.WORLD)) {
			vertices = getVerticesInWorldCoordinates();
		} else if (cframe.equals(CoordinateFrame.CAMERA)) {
			vertices = getVerticesInCameraCoordinates(camera);
		}
		return vertices;
	}

	public List<Point3D> getVerticesInObjectCoordinates() {
		return meshes.get(CoordinateFrame.OBJECT).getVertices();
	}

	public List<Point3D> getVerticesInWorldCoordinates() {
		if (!meshes.containsKey(CoordinateFrame.WORLD)) {
			List<Point3D> vertices = deriveVerticesInWorldCoordinates();
			meshes.put(CoordinateFrame.WORLD, new Mesh3DImpl(vertices, getEdges()));
		}
		return meshes.get(CoordinateFrame.WORLD).getVertices();
	}

	private List<Point3D> deriveVerticesInWorldCoordinates() {
		return getSelfToRootCompositeTransform().getForwardCompositeMatrix()
				.transform(getVerticesInObjectCoordinates());
	}

	protected List<Point3D> getVerticesInCameraCoordinates(Camera camera) {
		if (!meshes.containsKey(CoordinateFrame.CAMERA)) {
			List<Point3D> vertices = deriveVerticesInCameraCoordinates(camera);
			meshes.put(CoordinateFrame.CAMERA, new Mesh3DImpl(vertices, getEdges()));
		}
		return meshes.get(CoordinateFrame.CAMERA).getVertices();
	}

	private List<Point3D> deriveVerticesInCameraCoordinates(Camera camera) {
		return camera.getViewingMatrix().transform(getVerticesInWorldCoordinates());
	}

	protected List<Edge> getEdges() {
		return meshes.get(CoordinateFrame.OBJECT).getEdges();
	}

	protected Point3D fromCameraToObjectCoordinates(Point3D point, Camera camera) {
		return fromWorldToObjectCoordinates(fromCameraToWorldCoordinates(point, camera));
	}

	protected Point3D fromCameraToWorldCoordinates(Point3D point, Camera camera) {
		return Transformation.getInverseMatrix(camera.getViewingMatrix()).transform(point);
	}

	protected Point3D fromWorldToObjectCoordinates(Point3D point) {
		return getSelfToRootCompositeTransform().reverseTransform(point);
	}

	@Override
	public void notifySelfHasTransformed() {
		super.notifySelfHasTransformed();
		invalidateWorldAndCameraMesh();
	}

	@Override
	public void notifyAncestorHasTransformed() {
		super.notifyAncestorHasTransformed();
		invalidateWorldAndCameraMesh();
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		super.cameraHasChanged(camera);
		invalidateCameraMesh();
	}

	private void invalidateWorldAndCameraMesh() {
		invalidateWorldMesh();
		invalidateCameraMesh();
	}

	private void invalidateWorldMesh() {
		meshes.remove(CoordinateFrame.WORLD);
	}

	private void invalidateCameraMesh() {
		meshes.remove(CoordinateFrame.CAMERA);
	}

}
