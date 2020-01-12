package org.maia.cgi.model.d3.object;

import java.util.List;

import org.maia.cgi.geometry.d3.Point3D;

public interface Mesh3D {

	List<Point3D> getVertices();

	List<Edge> getEdges();

	public static interface Edge {

		int getFirstVertexIndex();

		int getSecondVertexIndex();

	}

}