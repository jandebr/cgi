package org.maia.cgi.model.d3;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.model.d3.object.BaseObject3D;
import org.maia.cgi.model.d3.object.MultipartObject3D;
import org.maia.cgi.model.d3.object.PolygonalObject3D;
import org.maia.cgi.model.d3.object.SimpleFace3D;
import org.maia.cgi.render.d3.shading.FlatShadingModel;

public class ModelBuilderUtils {

	public static PolygonalObject3D loadShapeXY(String filePath) {
		return new PolygonalObject3D(loadVerticesXY(filePath));
	}

	public static List<Point3D> loadVerticesXY(String filePath) {
		List<Point3D> vertices = new Vector<Point3D>(50);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				int i = line.indexOf(',');
				double x = Double.parseDouble(line.substring(0, i));
				double y = -Double.parseDouble(line.substring(i + 1));
				vertices.add(new Point3D(x, y, 0));
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return vertices;
	}

	public static void centerAtOrigin(BaseObject3D object) {
		Box3D box = object.getBoundingBox(CoordinateFrame.WORLD, null);
		Point3D center = box.getCenter();
		object.translate(-center.getX(), -center.getY(), -center.getZ());
	}

	public static void centerAtOriginXY(BaseObject3D object) {
		Box3D box = object.getBoundingBox(CoordinateFrame.WORLD, null);
		Point3D center = box.getCenter();
		object.translate(-center.getX(), -center.getY(), 0);
	}

	public static void centerAtOriginXZ(BaseObject3D object) {
		Box3D box = object.getBoundingBox(CoordinateFrame.WORLD, null);
		Point3D center = box.getCenter();
		object.translate(-center.getX(), 0, -center.getZ());
	}

	public static void centerAtOriginYZ(BaseObject3D object) {
		Box3D box = object.getBoundingBox(CoordinateFrame.WORLD, null);
		Point3D center = box.getCenter();
		object.translate(0, -center.getY(), -center.getZ());
	}

	public static void stretchToUnityBoundingBox(BaseObject3D object) {
		Box3D box = object.getBoundingBox(CoordinateFrame.WORLD, null);
		Point3D center = box.getCenter();
		object.translate(-center.getX(), -center.getY(), -center.getZ());
		if (box.getWidth() > 0)
			object.scaleX(1.0 / box.getWidth());
		if (box.getHeight() > 0)
			object.scaleY(1.0 / box.getHeight());
		if (box.getDepth() > 0)
			object.scaleZ(1.0 / box.getDepth());
	}

	public static BaseObject3D buildUnityCube(Color color, FlatShadingModel shadingModel) {
		List<Point3D> v = new Vector<Point3D>(8);
		// bottom vertices
		v.add(new Point3D(-1.0, -1.0, +1.0));
		v.add(new Point3D(+1.0, -1.0, +1.0));
		v.add(new Point3D(+1.0, -1.0, -1.0));
		v.add(new Point3D(-1.0, -1.0, -1.0));
		// top vertices
		v.add(new Point3D(-1.0, +1.0, +1.0));
		v.add(new Point3D(+1.0, +1.0, +1.0));
		v.add(new Point3D(+1.0, +1.0, -1.0));
		v.add(new Point3D(-1.0, +1.0, -1.0));
		// cube
		MultipartObject3D<SimpleFace3D> cube = new MultipartObject3D<SimpleFace3D>();
		cube.addPart(new SimpleFace3D(color, shadingModel, v.get(0), v.get(1), v.get(5), v.get(4))); // front
		cube.addPart(new SimpleFace3D(color, shadingModel, v.get(0), v.get(4), v.get(7), v.get(3))); // left
		cube.addPart(new SimpleFace3D(color, shadingModel, v.get(1), v.get(2), v.get(6), v.get(5))); // right
		cube.addPart(new SimpleFace3D(color, shadingModel, v.get(2), v.get(3), v.get(7), v.get(6))); // back
		cube.addPart(new SimpleFace3D(color, shadingModel, v.get(0), v.get(3), v.get(2), v.get(1))); // bottom
		cube.addPart(new SimpleFace3D(color, shadingModel, v.get(4), v.get(5), v.get(6), v.get(7))); // top
		return cube;
	}

	public static BaseObject3D buildCube(double side, Color color, FlatShadingModel shadingModel) {
		return buildBox(side, side, side, color, shadingModel);
	}

	public static BaseObject3D buildBox(double width, double height, double depth, Color color,
			FlatShadingModel shadingModel) {
		return (BaseObject3D) buildUnityCube(color, shadingModel).scale(width / 2.0, height / 2.0, depth / 2.0);
	}

	public static PolygonalObject3D buildRoundedRectangleXY(double width, double height, double cornerRadiusX,
			double cornerRadiusY, double precision) {
		int nc = 3 + (int) Math.ceil(precision / 0.2);
		List<Point3D> vertices = new Vector<Point3D>(nc * 4);
		for (int q = 0; q < 4; q++) {
			double xc = (width / 2.0 - cornerRadiusX) * (q <= 1 ? 1 : -1);
			double yc = (height / 2.0 - cornerRadiusY) * (q == 0 || q == 3 ? 1 : -1);
			double startAngle = Math.PI / 2.0 * (1.0 - q);
			for (int i = 0; i < nc; i++) {
				double angle = startAngle - Math.PI / 2.0 * i / (nc - 1);
				double x = xc + cornerRadiusX * Math.cos(angle);
				double y = yc + cornerRadiusY * Math.sin(angle);
				vertices.add(new Point3D(x, y, 0));
			}
		}
		return new PolygonalObject3D(vertices);
	}

	public static BaseObject3D buildCylinder(double radius, double depth, int vertexCount, Color color,
			FlatShadingModel shadingModel) {
		PolygonalObject3D base = buildCircularShapeXY(radius, vertexCount);
		return buildExtrusion(base, depth, color, shadingModel);
	}

	public static BaseObject3D buildPyramid(double radius, double depth, int vertexCount, Color color,
			FlatShadingModel shadingModel, boolean fillBase) {
		MultipartObject3D<SimpleFace3D> pyramid = new MultipartObject3D<SimpleFace3D>();
		// Base
		PolygonalObject3D base = buildCircularShapeXY(radius, vertexCount);
		if (fillBase) {
			pyramid.addPart(convertToFace(base, color, shadingModel));
		}
		// Hull
		List<Point3D> vertices = base.getVerticesInWorldCoordinates();
		Point3D top = new Point3D(0, 0, depth);
		int n = vertices.size();
		for (int i = 0; i < n; i++) {
			Point3D p0 = vertices.get(i);
			Point3D p1 = vertices.get((i + 1) % n);
			pyramid.addPart(new SimpleFace3D(color, shadingModel, p0, p1, top));
		}
		return pyramid;
	}

	public static BaseObject3D buildRing(double innerRadius, double outerRadius, double depth, int vertexCount,
			Color color, FlatShadingModel shadingModel) {
		MultipartObject3D<BaseObject3D> ring = new MultipartObject3D<BaseObject3D>();
		// Edges
		PolygonalObject3D outerRingBack = buildCircularShapeXY(outerRadius, vertexCount);
		PolygonalObject3D outerRingFront = (PolygonalObject3D) cloneShape(outerRingBack).translateZ(depth);
		PolygonalObject3D innerRingBack = buildCircularShapeXY(innerRadius, vertexCount);
		PolygonalObject3D innerRingFront = (PolygonalObject3D) cloneShape(innerRingBack).translateZ(depth);
		// Outer hull
		ring.addPart(buildLayeredObject(outerRingBack, outerRingFront, false, false, color, shadingModel));
		// Inner hull
		ring.addPart(buildLayeredObject(innerRingBack, innerRingFront, false, false, color, shadingModel));
		// Back side
		ring.addPart(buildLayeredObject(outerRingBack, innerRingBack, false, false, color, shadingModel));
		// Front side
		ring.addPart(buildLayeredObject(outerRingFront, innerRingFront, false, false, color, shadingModel));
		return ring;
	}

	public static BaseObject3D buildSphere(double radius, int vertexCount, Color color, FlatShadingModel shadingModel) {
		double e = 0.005;
		int nlayers = vertexCount / 2;
		List<PolygonalObject3D> layers = new Vector<PolygonalObject3D>(nlayers);
		for (int li = 0; li < nlayers; li++) {
			double z = li / (double) (nlayers - 1) * (2 * radius - 2 * e) - radius + e;
			double r = radius * Math.cos(Math.asin(z / radius));
			PolygonalObject3D polygon = (PolygonalObject3D) buildCircularShapeXY(r, vertexCount).translateZ(z);
			layers.add(polygon);
		}
		return buildLayeredObject(layers, true, false, color, shadingModel);
	}

	public static PolygonalObject3D buildCircularShapeXY(double radius, int vertexCount) {
		double angleFrom = 0;
		double angleTo = 2 * Math.PI * (1.0 - 1.0 / vertexCount);
		return buildCircularSegmentXY(radius, angleFrom, angleTo, vertexCount);
	}

	public static PolygonalObject3D buildCircularSegmentXY(double radius, double angleFrom, double angleTo,
			int vertexCount) {
		List<Point3D> vertices = new Vector<Point3D>(vertexCount);
		for (int i = 0; i < vertexCount; i++) {
			double radians = angleFrom + (angleTo - angleFrom) * i / (vertexCount - 1);
			double x = radius * Math.cos(radians);
			double y = radius * Math.sin(radians);
			vertices.add(new Point3D(x, y, 0));
		}
		return new PolygonalObject3D(vertices);
	}

	public static PolygonalObject3D buildSqueezedCircularShapeXY(double radius, double squeezeFactorX,
			double squeezeFactorY, int vertexCount) {
		List<Point3D> vertices = new Vector<Point3D>(vertexCount);
		for (int i = 0; i < vertexCount; i++) {
			double angle = 2.0 * Math.PI * i / vertexCount;
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			double x = radius * Math.signum(cos) * Math.pow(Math.abs(cos), squeezeFactorX);
			double y = radius * Math.signum(sin) * Math.pow(Math.abs(sin), squeezeFactorY);
			vertices.add(new Point3D(x, y, 0));
		}
		return new PolygonalObject3D(vertices);
	}

	public static BaseObject3D buildExtrusion(PolygonalObject3D shapeXY, double depth, Color color,
			FlatShadingModel shadingModel) {
		return buildSlantedExtrusion(shapeXY, depth, 0, 0, color, shadingModel);
	}

	public static BaseObject3D buildSlantedExtrusion(PolygonalObject3D shapeXY, double depth, double xOffset,
			double yOffset, Color color, FlatShadingModel shadingModel) {
		PolygonalObject3D slantedShapeXY = (PolygonalObject3D) cloneShape(shapeXY).translate(xOffset, yOffset, depth);
		return buildLayeredObject(shapeXY, slantedShapeXY, true, false, color, shadingModel);
	}

	public static BaseObject3D buildExtrusionAlongPath(PolygonalObject3D shapeXY, List<Point3D> path, Color color,
			FlatShadingModel shadingModel) {
		int n = path.size();
		List<PolygonalObject3D> layers = new Vector<PolygonalObject3D>(n);
		for (int i = 0; i < n; i++) {
			Point3D p = path.get(i);
			Vector3D normalVector = null;
			if (i == 0) {
				normalVector = path.get(1).minus(p);
			} else if (i < n - 1) {
				Vector3D n1 = p.minus(path.get(i - 1));
				Vector3D n2 = path.get(i + 1).minus(p);
				normalVector = new Vector3D((n1.getX() + n2.getX()) / 2, (n1.getY() + n2.getY()) / 2,
						(n1.getZ() + n2.getZ()) / 2);
			} else {
				normalVector = p.minus(path.get(i - 1));
			}
			double lon = normalVector.getLongitudeInRadians();
			double lat = normalVector.getLatitudeInRadians();
			layers.add((PolygonalObject3D) cloneShape(shapeXY).rotateY(Math.PI / 2).rotateZ(lat).rotateY(-lon)
					.translate(p.getX(), p.getY(), p.getZ()));
		}
		return buildLayeredObject(layers, true, true, color, shadingModel);
	}

	public static BaseObject3D buildExtrusionWithRoundedSides(PolygonalObject3D shapeXY, double roundnessFactor,
			double precision, Color color, FlatShadingModel shadingModel) {
		stretchToUnityBoundingBox(shapeXY);
		double t = 0.5;
		double r = roundnessFactor * t;
		double d = t - r;
		int nr = 3 + (int) Math.ceil(precision / 0.33);
		List<PolygonalObject3D> layers = new Vector<PolygonalObject3D>(2 * nr);
		for (int i = 0; i < nr; i++) {
			double angle = Math.PI / 2 / (nr - 1) * i;
			double z = d + r * Math.cos(angle);
			double scale = 1.0 - 2 * r * (1.0 - Math.sin(angle));
			layers.add((PolygonalObject3D) cloneShape(shapeXY).scale(scale).translateZ(z));
		}
		for (int i = 0; i < nr; i++) {
			double angle = Math.PI / 2 - Math.PI / 2 / (nr - 1) * i;
			double z = -(d + r * Math.cos(angle));
			double scale = 1.0 - 2 * r * (1.0 - Math.sin(angle));
			layers.add((PolygonalObject3D) cloneShape(shapeXY).scale(scale).translateZ(z));
		}
		return buildLayeredObject(layers, true, false, color, shadingModel);
	}

	public static BaseObject3D buildLayeredObject(PolygonalObject3D oneLayer, PolygonalObject3D otherLayer,
			boolean fillSides, boolean triangularFaces, Color color, FlatShadingModel shadingModel) {
		List<PolygonalObject3D> layers = new Vector<PolygonalObject3D>(2);
		layers.add(oneLayer);
		layers.add(otherLayer);
		return buildLayeredObject(layers, fillSides, triangularFaces, color, shadingModel);
	}

	public static BaseObject3D buildLayeredObject(List<PolygonalObject3D> layers, boolean fillSides,
			boolean triangularFaces, Color color, FlatShadingModel shadingModel) {
		MultipartObject3D<SimpleFace3D> object = new MultipartObject3D<SimpleFace3D>();
		int lc = layers.size();
		// Sides
		if (fillSides) {
			object.addPart(convertToFace(layers.get(0), color, shadingModel));
			object.addPart(convertToFace(layers.get(lc - 1), color, shadingModel));
		}
		// Surface
		for (int li = 0; li < lc - 1; li++) {
			List<Point3D> verticesP = layers.get(li).getVerticesInWorldCoordinates();
			List<Point3D> verticesQ = layers.get(li + 1).getVerticesInWorldCoordinates();
			int n = verticesP.size();
			for (int i = 0; i < n; i++) {
				Point3D p0 = verticesP.get(i);
				Point3D p1 = verticesP.get((i + 1) % n);
				Point3D q0 = verticesQ.get(i);
				Point3D q1 = verticesQ.get((i + 1) % n);
				if (triangularFaces) {
					object.addPart(new SimpleFace3D(color, shadingModel, p0, p1, q1));
					object.addPart(new SimpleFace3D(color, shadingModel, p0, q1, q0));
				} else {
					object.addPart(new SimpleFace3D(color, shadingModel, p0, p1, q1, q0));
				}
			}
		}
		return object;
	}

	public static SimpleFace3D convertToFace(PolygonalObject3D polygon, Color color, FlatShadingModel shadingModel) {
		return new SimpleFace3D(color, shadingModel, polygon.getVerticesInWorldCoordinates());
	}

	public static PolygonalObject3D cloneShape(PolygonalObject3D polygon) {
		return new PolygonalObject3D(polygon.getVerticesInWorldCoordinates());
	}

}