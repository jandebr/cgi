package org.maia.cgi.model.d3.object;

import java.util.List;

import org.maia.cgi.geometry.d3.Point3D;

public class Mesh3DImpl implements Mesh3D {

	private List<Point3D> vertices;

	private List<Edge> edges;

	public Mesh3DImpl(List<Point3D> vertices, List<Edge> edges) {
		this.vertices = vertices;
		this.edges = edges;
	}

	@Override
	public List<Point3D> getVertices() {
		return vertices;
	}

	@Override
	public List<Edge> getEdges() {
		return edges;
	}

	public static class EdgeImpl implements Edge {

		private int firstVertexIndex;

		private int secondVertexIndex;

		public EdgeImpl(int firstVertexIndex, int secondVertexIndex) {
			this.firstVertexIndex = firstVertexIndex;
			this.secondVertexIndex = secondVertexIndex;
		}

		@Override
		public int getFirstVertexIndex() {
			return firstVertexIndex;
		}

		@Override
		public int getSecondVertexIndex() {
			return secondVertexIndex;
		}

	}

}