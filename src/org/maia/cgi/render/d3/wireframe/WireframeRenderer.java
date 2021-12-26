package org.maia.cgi.render.d3.wireframe;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.maia.cgi.compose.Compositing;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.object.Mesh3D;
import org.maia.cgi.model.d3.object.Mesh3D.Edge;
import org.maia.cgi.model.d3.object.Mesh3DImpl;
import org.maia.cgi.model.d3.object.MeshObject3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.model.d3.scene.SceneUtils;
import org.maia.cgi.render.d3.BaseSceneRenderer;
import org.maia.cgi.render.d3.RenderOptions;
import org.maia.cgi.render.d3.view.ViewPort;
import org.maia.cgi.transform.d3.TransformMatrix;

public class WireframeRenderer extends BaseSceneRenderer {

	private static final String STEP_LABEL_RENDER = "Render wireframe";

	public WireframeRenderer() {
	}

	@Override
	protected void renderImpl(Scene scene, Collection<ViewPort> outputs, RenderOptions options) {
		TransformMatrix projectionMatrix = scene.getCamera().getViewVolume().getProjectionMatrix();
		Collection<MeshObject3D> objects = SceneUtils.getAllMeshObjectsInScene(scene);
		int n = objects.size();
		int i = 0;
		for (MeshObject3D object : objects) {
			Mesh3D mesh = object.getMesh(CoordinateFrame.CAMERA, scene.getCamera());
			renderMesh(mesh, projectionMatrix, outputs, options);
			double progress = ++i / (double) n;
			fireRenderingProgressUpdate(scene, 1, 0, progress, STEP_LABEL_RENDER);
		}
	}

	private void renderMesh(Mesh3D mesh, TransformMatrix projectionMatrix, Collection<ViewPort> outputs,
			RenderOptions options) {
		List<Point3D> projectedVertices = projectionMatrix.transform(mesh.getVertices());
		Mesh3D projectedMesh = new Mesh3DImpl(projectedVertices, mesh.getEdges());
		Mesh3D clippedMesh = clipProjectedMesh(projectedMesh);
		applyPerspectiveDivision(clippedMesh.getVertices());
		renderProjectedAndClippedMesh(clippedMesh, outputs, options);
	}

	private Mesh3D clipProjectedMesh(Mesh3D mesh) {
		// TODO Clip against the canonical view volume
		return mesh;
	}

	private void applyPerspectiveDivision(List<Point3D> vertices) {
		for (Point3D vertex : vertices) {
			vertex.normalizeToUnitW();
		}
	}

	private void renderProjectedAndClippedMesh(Mesh3D mesh, Collection<ViewPort> outputs, RenderOptions options) {
		for (ViewPort output : outputs) {
			renderProjectedAndClippedMesh(mesh, output, options);
		}
	}

	private void renderProjectedAndClippedMesh(Mesh3D mesh, ViewPort output, RenderOptions options) {
		List<Point3D> vertices = mesh.getVertices();
		for (Edge edge : mesh.getEdges()) {
			Point3D p1 = vertices.get(edge.getFirstVertexIndex());
			Point3D p2 = vertices.get(edge.getSecondVertexIndex());
			renderProjectedAndClippedEdge(p1, p2, output, options);
		}
	}

	protected void renderProjectedAndClippedEdge(Point3D p1, Point3D p2, ViewPort output, RenderOptions options) {
		output.drawLineInViewCoordinates(p1.getX(), p1.getY(), p1.getZ(), getColorForVertex(p1, options), p2.getX(),
				p2.getY(), p2.getZ(), getColorForVertex(p2, options));
	}

	protected Color getColorForVertex(Point3D vertex, RenderOptions options) {
		double r = (vertex.getZ() + 1) / 2; // between 0 (near plane) and 1 (far plane)
		if (r < 0 || r > 1.0) {
			// Edge vertex lies outside view volume
			return Color.RED;
		} else {
			return Compositing.interpolateColors(options.getWireframeColorNear(), options.getWireframeColorFar(), r);
		}
	}

}
