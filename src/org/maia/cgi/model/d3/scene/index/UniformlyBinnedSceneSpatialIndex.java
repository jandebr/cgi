package org.maia.cgi.model.d3.scene.index;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.scene.Scene;

/**
 * Spatial index of a Scene's objects in camera coordinates as a Cartesian grid of unit cubes called "bins"
 * 
 * <p>
 * The spatial index is constructed based on the current positions and orientations of the objects in the scene and the
 * camera. It is the responsability of the client code to create a new index to reflect an updated snapshot of that
 * scene.
 * </p>
 * 
 * @see NonUniformlyBinnedSceneSpatialIndex
 * @see SceneSpatialIndexFactory
 */
public class UniformlyBinnedSceneSpatialIndex extends BinnedSceneSpatialIndex {

	private int xBins;

	private int yBins;

	private int zBins;

	private Map<SpatialBin, Collection<Object3D>> index;

	private Box3D firstBinBoundingBox;

	public UniformlyBinnedSceneSpatialIndex(Scene scene, int xBins, int yBins, int zBins) {
		super(scene);
		this.xBins = xBins;
		this.yBins = yBins;
		this.zBins = zBins;
		this.index = new HashMap<SpatialBin, Collection<Object3D>>(xBins * yBins);
	}

	@Override
	public void buildIndex() {
		setFirstBinBoundingBox(deriveFirstBinBoundingBox());
		for (Object3D object : getIndexedObjects()) {
			addObject(object);
		}
	}

	@Override
	public void dispose() {
		getIndex().clear();
	}

	@Override
	public BinStatistics getBinStatistics() {
		return new UniformBinStatistics();
	}

	@Override
	public Iterator<ObjectSurfacePoint3D> getObjectIntersections(LineSegment3D line) {
		return new ObjectLineIntersectionsIteratorImpl(line);
	}

	private void addObject(Object3D object) {
		if (object.isBounded()) {
			Box3D bbox = getObjectBox(object);
			int x1 = mapToXbin(bbox.getX1());
			int x2 = mapToXbin(bbox.getX2());
			int y1 = mapToYbin(bbox.getY1());
			int y2 = mapToYbin(bbox.getY2());
			int z1 = mapToZbin(bbox.getZ1());
			int z2 = mapToZbin(bbox.getZ2());
			for (int xi = x1; xi <= x2; xi++) {
				for (int yi = y1; yi <= y2; yi++) {
					for (int zi = z1; zi <= z2; zi++) {
						indexObject(object, xi, yi, zi);
					}
				}
			}
		} else {
			// No info on bounds, so let's add the object to every bin
			for (int xi = 0; xi < getXbins(); xi++) {
				for (int yi = 0; yi < getYbins(); yi++) {
					for (int zi = 0; zi < getZbins(); zi++) {
						indexObject(object, xi, yi, zi);
					}
				}
			}
		}
	}

	private void indexObject(Object3D object, int xBin, int yBin, int zBin) {
		SpatialBin bin = SpatialBin.create(xBin, yBin, zBin);
		Collection<Object3D> collection = getIndex().get(bin);
		if (collection == null) {
			collection = new HashSet<Object3D>();
			getIndex().put(bin, collection);
		}
		collection.add(object);
	}

	private int mapToXbin(double x) {
		Box3D box = getFirstBinBoundingBox();
		int xi = (int) Math.floor((x - box.getX1()) / box.getWidth());
		return Math.max(Math.min(xi, getXbins() - 1), 0);
	}

	private int mapToYbin(double y) {
		Box3D box = getFirstBinBoundingBox();
		int yi = (int) Math.floor((y - box.getY1()) / box.getHeight());
		return Math.max(Math.min(yi, getYbins() - 1), 0);
	}

	private int mapToZbin(double z) {
		Box3D box = getFirstBinBoundingBox();
		int zi = (int) Math.floor((z - box.getZ1()) / box.getDepth());
		return Math.max(Math.min(zi, getZbins() - 1), 0);
	}

	private Box3D deriveFirstBinBoundingBox() {
		Box3D sceneBox = getSceneBox();
		double x = sceneBox.getX1();
		double y = sceneBox.getY1();
		double z = sceneBox.getZ1();
		double width = sceneBox.getWidth() / getXbins();
		double height = sceneBox.getHeight() / getYbins();
		double depth = sceneBox.getDepth() / getZbins();
		return new Box3D(x, x + width, y, y + height, z, z + depth);
	}

	private double getBinBoundaryX(int xBin, int xDir) {
		Box3D box = getFirstBinBoundingBox();
		return box.getX1() + box.getWidth() * (xDir < 0 ? xBin : xBin + 1);
	}

	private double getBinBoundaryY(int yBin, int yDir) {
		Box3D box = getFirstBinBoundingBox();
		return box.getY1() + box.getHeight() * (yDir < 0 ? yBin : yBin + 1);
	}

	private double getBinBoundaryZ(int zBin, int zDir) {
		Box3D box = getFirstBinBoundingBox();
		return box.getZ1() + box.getDepth() * (zDir < 0 ? zBin : zBin + 1);
	}

	private Collection<Object3D> getObjectsInBin(int xBin, int yBin, int zBin) {
		return getIndex().get(SpatialBin.create(xBin, yBin, zBin));
	}

	private int getXbins() {
		return xBins;
	}

	private int getYbins() {
		return yBins;
	}

	private int getZbins() {
		return zBins;
	}

	private Map<SpatialBin, Collection<Object3D>> getIndex() {
		return index;
	}

	private Box3D getFirstBinBoundingBox() {
		return firstBinBoundingBox;
	}

	private void setFirstBinBoundingBox(Box3D boundingBox) {
		this.firstBinBoundingBox = boundingBox;
	}

	private static class SpatialBin {

		private int x;

		private int y;

		private int z;

		private SpatialBin(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public static SpatialBin create(int x, int y, int z) {
			// Idea of caching bin objects proved no rendering time gain
			return new SpatialBin(x, y, z);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
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
			return x == other.x && y == other.y && z == other.z;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getZ() {
			return z;
		}

	}

	private class ObjectLineIntersectionsIteratorImpl extends ObjectLineIntersectionsIterator {

		private double x1, x2, xd, y1, y2, yd, z1, z2, zd;

		private int xdir, ydir, zdir;

		private int xi, yi, zi;

		private int xn, yn, zn;

		private boolean xin, yin, zin;

		private double tx, ty, tz;

		private Iterator<Object3D> currentObjects;

		private boolean proceed;

		public ObjectLineIntersectionsIteratorImpl(LineSegment3D line) {
			super(line);
			Point3D p1 = line.getP1();
			Point3D p2 = line.getP2();
			// init X
			x1 = p1.getX();
			x2 = p2.getX();
			xd = x2 - x1;
			xdir = (int) Math.signum(xd);
			xi = mapToXbin(x1);
			xn = getXbins() - 1;
			xin = xi >= 0 && xi <= xn;
			// init Y
			y1 = p1.getY();
			y2 = p2.getY();
			yd = y2 - y1;
			ydir = (int) Math.signum(yd);
			yi = mapToYbin(y1);
			yn = getYbins() - 1;
			yin = yi >= 0 && yi <= yn;
			// init Z
			z1 = p1.getZ();
			z2 = p2.getZ();
			zd = z2 - z1;
			zdir = (int) Math.signum(zd);
			zi = mapToZbin(z1);
			zn = getZbins() - 1;
			zin = zi >= 0 && zi <= zn;
			// init neighbouring bin boundary intersects
			tx = xd != 0 ? (getBinBoundaryX(xi, xdir) - x1) / xd : Double.MAX_VALUE;
			ty = yd != 0 ? (getBinBoundaryY(yi, ydir) - y1) / yd : Double.MAX_VALUE;
			tz = zd != 0 ? (getBinBoundaryZ(zi, zdir) - z1) / zd : Double.MAX_VALUE;
			// init traversal
			proceed = xin && yin && zin;
		}

		@Override
		protected void provisionIntersections() {
			// traverse bins along the line to add objects
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			Set<Object3D> objects = getObjects();
			while (proceed && intersections.isEmpty()) {
				if (currentObjects == null && xin && yin && zin) {
					Collection<Object3D> binObjects = getObjectsInBin(xi, yi, zi);
					if (binObjects != null)
						currentObjects = binObjects.iterator();
				}
				if (currentObjects != null && currentObjects.hasNext()) {
					Object3D object = currentObjects.next();
					if (objects.add(object) && object.isRaytraceable()) {
						object.asRaytraceableObject().intersectWithLightRay(getLine(), getScene(), intersections);
					}
				} else {
					currentObjects = null;
					if (tx <= ty && tx <= tz) {
						xi += xdir;
						tx = (getBinBoundaryX(xi, xdir) - x1) / xd;
						xin = xi >= 0 && xi <= xn;
						proceed = proceed && xin;
					} else if (ty <= tx && ty <= tz) {
						yi += ydir;
						ty = (getBinBoundaryY(yi, ydir) - y1) / yd;
						yin = yi >= 0 && yi <= yn;
						proceed = proceed && yin;
					} else {
						zi += zdir;
						tz = (getBinBoundaryZ(zi, zdir) - z1) / zd;
						zin = zi >= 0 && zi <= zn;
						proceed = proceed && zin;
					}
					proceed = proceed && (tx <= 1.0 || ty <= 1.0 || tz <= 1.0);
				}
			}
		}

	}

	public class UniformBinStatistics extends BinStatistics {

		public UniformBinStatistics() {
		}

		@Override
		public int getBinCount() {
			return getXbins() * getYbins() * getZbins();
		}

		@Override
		public int getEmptyBins() {
			int empty = 0;
			for (int zi = 0; zi < getZbins(); zi++) {
				for (int yi = 0; yi < getYbins(); yi++) {
					for (int xi = 0; xi < getXbins(); xi++) {
						Collection<Object3D> objects = getObjectsInBin(xi, yi, zi);
						if (objects == null || objects.isEmpty()) {
							empty++;
						}
					}
				}
			}
			return empty;
		}

		@Override
		public int getMaximumObjectsPerBin() {
			int max = 0;
			for (int zi = 0; zi < getZbins(); zi++) {
				for (int yi = 0; yi < getYbins(); yi++) {
					for (int xi = 0; xi < getXbins(); xi++) {
						Collection<Object3D> objects = getObjectsInBin(xi, yi, zi);
						if (objects != null) {
							max = Math.max(max, objects.size());
						}
					}
				}
			}
			return max;
		}

		@Override
		public double getAverageObjectsPerBin() {
			return computeAverageObjectsPerBin(true);
		}

		@Override
		public double getAverageObjectsPerNonEmptyBin() {
			return computeAverageObjectsPerBin(false);
		}

		private double computeAverageObjectsPerBin(boolean includeEmptyBins) {
			int sum = 0;
			int count = 0;
			for (int zi = 0; zi < getZbins(); zi++) {
				for (int yi = 0; yi < getYbins(); yi++) {
					for (int xi = 0; xi < getXbins(); xi++) {
						Collection<Object3D> objects = getObjectsInBin(xi, yi, zi);
						if (objects == null || objects.isEmpty()) {
							if (includeEmptyBins)
								count++;
						} else {
							sum += objects.size();
							count++;
						}
					}
				}
			}
			if (count == 0)
				return 0;
			return (double) sum / count;
		}

		@Override
		public double getAverageObjectsPerUnitSpace() {
			return getAverageObjectsPerBin();
		}

		@Override
		public ObjectsPerBinHistogram getObjectsPerBinHistogram(int classCount) {
			int classRangeSize = (int) Math.ceil(getMaximumObjectsPerBin() / (double) classCount);
			return new ObjectsPerBinHistogramImpl(classCount, classRangeSize);
		}

	}

	public class ObjectsPerBinHistogramImpl extends ObjectsPerBinHistogram {

		public ObjectsPerBinHistogramImpl(int classCount, int classRangeSize) {
			super(classCount, classRangeSize);
		}

		@Override
		public int[] getClassValues() {
			int n = getClassCount();
			int size = getClassRangeSize();
			int[] values = new int[n];
			for (int zi = 0; zi < getZbins(); zi++) {
				for (int yi = 0; yi < getYbins(); yi++) {
					for (int xi = 0; xi < getXbins(); xi++) {
						Collection<Object3D> objects = getObjectsInBin(xi, yi, zi);
						int count = objects != null ? objects.size() : 0;
						if (count > 0) {
							// Excluding empty bins
							int ci = Math.min((int) Math.floor(count / (double) size), n - 1);
							values[ci]++;
						}
					}
				}
			}
			return values;
		}

	}

}