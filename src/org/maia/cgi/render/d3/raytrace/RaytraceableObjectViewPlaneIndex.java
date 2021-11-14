package org.maia.cgi.render.d3.raytrace;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.maia.cgi.geometry.d2.Point2D;
import org.maia.cgi.geometry.d2.Rectangle2D;
import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Plane3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.camera.ViewVolume;
import org.maia.cgi.model.d3.object.RaytraceableObject3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.model.d3.scene.SceneUtils;

public class RaytraceableObjectViewPlaneIndex {

	private Camera camera;

	private int xBins;

	private int yBins;

	private Map<SpatialBin, Collection<RaytraceableObject3D>> index;

	public RaytraceableObjectViewPlaneIndex(Camera camera) {
		this(camera, 20, 20);
	}

	public RaytraceableObjectViewPlaneIndex(Camera camera, int xBins, int yBins) {
		this.camera = camera;
		this.xBins = xBins;
		this.yBins = yBins;
		this.index = new HashMap<SpatialBin, Collection<RaytraceableObject3D>>(xBins * yBins);
	}

	public BinStatistics getBinStatistics() {
		return new BinStatistics();
	}

	public void addAllRaytraceableObjectsFromScene(Scene scene) {
		for (RaytraceableObject3D object : SceneUtils.getAllRaytraceableObjectsInScene(scene)) {
			addObject(object);
		}
	}

	public void addObject(RaytraceableObject3D object) {
		Rectangle2D bounds = getObjectBoundsClippedOnViewPlane(object);
		if (bounds != null) {
			int x1 = mapToXbin(bounds.getLeft());
			int x2 = mapToXbin(bounds.getRight());
			int y1 = mapToYbin(bounds.getBottom());
			int y2 = mapToYbin(bounds.getTop());
			for (int y = y1; y <= y2; y++) {
				for (int x = x1; x <= x2; x++) {
					indexObject(object, x, y);
				}
			}
		}
	}

	public Collection<RaytraceableObject3D> getObjects(double xView, double yView) {
		Collection<RaytraceableObject3D> objects = null;
		int xBin = mapToXbin(xView);
		int yBin = mapToYbin(yView);
		if (xBin >= 0 && yBin >= 0) {
			objects = getObjectsInBin(xBin, yBin);
		}
		return objects;
	}

	private Collection<RaytraceableObject3D> getObjectsInBin(int xBin, int yBin) {
		return getIndex().get(SpatialBin.create(xBin, yBin));
	}

	private Rectangle2D getObjectBoundsClippedOnViewPlane(RaytraceableObject3D object) {
		Rectangle2D bounds = null;
		if (object.isBounded()) {
			bounds = projectAndClipOntoViewPlane(object.asBoundedObject().getBoundingBox(CoordinateFrame.CAMERA,
					getCamera()));
		} else {
			bounds = getViewVolume().getViewPlaneRectangle(); // suppose it covers the entire view plane
		}
		return bounds;
	}

	private Rectangle2D projectAndClipOntoViewPlane(Box3D box) {
		Rectangle2D projection = null;
		Rectangle2D vpr = getViewVolume().getViewPlaneRectangle();
		double vpz = getViewVolume().getViewPlaneZ();
		Point3D p1 = new Point3D(vpr.getX1(), vpr.getY1(), vpz);
		Point3D p2 = new Point3D(vpr.getX1(), vpr.getY2(), vpz);
		Point3D p3 = new Point3D(vpr.getX2(), vpr.getY1(), vpz);
		Plane3D vp = new Plane3D(p1, p2, p3);
		Point3D eye = Point3D.origin();
		for (Point3D vertex : box.getVertices()) {
			LineSegment3D ray = new LineSegment3D(eye, vertex, true, false);
			Point3D intersect = ray.intersect(vp);
			if (intersect != null) {
				double vpx = intersect.getX();
				double vpy = intersect.getY();
				if (projection == null) {
					projection = new Rectangle2D(vpx, vpx, vpy, vpy);
				} else {
					projection.expandToContain(new Point2D(vpx, vpy));
				}
			}
		}
		// Clipping
		if (projection != null) {
			projection = projection.intersect(vpr);
		}
		return projection;
	}

	private void indexObject(RaytraceableObject3D object, int xBin, int yBin) {
		SpatialBin bin = SpatialBin.create(xBin, yBin);
		Collection<RaytraceableObject3D> collection = getIndex().get(bin);
		if (collection == null) {
			collection = new HashSet<RaytraceableObject3D>();
			getIndex().put(bin, collection);
		}
		collection.add(object);
	}

	private int mapToXbin(double xv) {
		Rectangle2D vpr = getViewVolume().getViewPlaneRectangle();
		if (xv < vpr.getLeft() || xv > vpr.getRight()) {
			return -1;
		} else if (xv == vpr.getRight()) {
			return getXbins() - 1;
		} else {
			return (int) Math.floor((xv - vpr.getLeft()) / vpr.getWidth() * getXbins());
		}
	}

	private int mapToYbin(double yv) {
		Rectangle2D vpr = getViewVolume().getViewPlaneRectangle();
		if (yv < vpr.getBottom() || yv > vpr.getTop()) {
			return -1;
		} else if (yv == vpr.getTop()) {
			return getYbins() - 1;
		} else {
			return (int) Math.floor((yv - vpr.getBottom()) / vpr.getHeight() * getYbins());
		}
	}

	private Camera getCamera() {
		return camera;
	}

	private ViewVolume getViewVolume() {
		return getCamera().getViewVolume();
	}

	private int getXbins() {
		return xBins;
	}

	private int getYbins() {
		return yBins;
	}

	private Map<SpatialBin, Collection<RaytraceableObject3D>> getIndex() {
		return index;
	}

	private static class SpatialBin {

		private int x;

		private int y;

		private SpatialBin(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public static SpatialBin create(int x, int y) {
			// Idea of caching bin objects proved no rendering time gain
			return new SpatialBin(x, y);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SpatialBin other = (SpatialBin) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

	}

	public class BinStatistics {

		public BinStatistics() {
		}

		public int getMaximumObjectsPerBinRow() {
			int max = 0;
			Collection<RaytraceableObject3D> rowObjects = new HashSet<RaytraceableObject3D>(100);
			for (int y = 0; y < getYbins(); y++) {
				rowObjects.clear();
				for (int x = 0; x < getXbins(); x++) {
					Collection<RaytraceableObject3D> objects = getObjectsInBin(x, y);
					if (objects != null) {
						rowObjects.addAll(objects);
					}
				}
				max = Math.max(max, rowObjects.size());
			}
			return max;
		}

		public int getMaximumObjectsPerBin() {
			int max = 0;
			for (int y = 0; y < getYbins(); y++) {
				for (int x = 0; x < getXbins(); x++) {
					Collection<RaytraceableObject3D> objects = getObjectsInBin(x, y);
					if (objects != null) {
						max = Math.max(max, objects.size());
					}
				}
			}
			return max;
		}

		public double getAverageObjectsPerBin() {
			int sum = 0;
			for (int y = 0; y < getYbins(); y++) {
				for (int x = 0; x < getXbins(); x++) {
					Collection<RaytraceableObject3D> objects = getObjectsInBin(x, y);
					if (objects != null) {
						sum += objects.size();
					}
				}
			}
			return (double) sum / (getYbins() * getXbins());
		}

		public ObjectsPerBinHistogram getObjectsPerBinHistogram(int classCount) {
			int classRangeSize = (int) Math.ceil(getMaximumObjectsPerBin() / (double) classCount);
			return new ObjectsPerBinHistogram(classCount, classRangeSize);
		}

	}

	public class ObjectsPerBinHistogram {

		private BinStatistics binStatistics;

		private int classCount;

		private int classRangeSize;

		public ObjectsPerBinHistogram(int classCount, int classRangeSize) {
			this.classCount = classCount;
			this.classRangeSize = classRangeSize;
		}

		public String toCsvString() {
			StringBuilder sb = new StringBuilder(getClassCount() * 8);
			sb.append("objects,count\n");
			int[] lowerBounds = getClassLowerBounds();
			int[] values = getClassValues();
			for (int i = 0; i < lowerBounds.length; i++) {
				sb.append(lowerBounds[i] + "+");
				sb.append(',');
				sb.append(values[i]);
				sb.append('\n');
			}
			return sb.toString();
		}

		public int[] getClassLowerBounds() {
			int n = getClassCount();
			int size = getClassRangeSize();
			int[] lowerBounds = new int[n];
			for (int i = 0; i < n; i++) {
				lowerBounds[i] = i * size;
			}
			return lowerBounds;
		}

		public int[] getClassValues() {
			int n = getClassCount();
			int size = getClassRangeSize();
			int[] values = new int[n];
			for (int y = 0; y < getYbins(); y++) {
				for (int x = 0; x < getXbins(); x++) {
					Collection<RaytraceableObject3D> objects = getObjectsInBin(x, y);
					if (objects != null) {
						int ci = Math.min((int) Math.floor(objects.size() / (double) size), n - 1);
						values[ci]++;
					}
				}
			}
			return values;
		}

		public int getClassCount() {
			return classCount;
		}

		public int getClassRangeSize() {
			return classRangeSize;
		}

	}

}