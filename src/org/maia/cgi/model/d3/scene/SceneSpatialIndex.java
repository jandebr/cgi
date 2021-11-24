package org.maia.cgi.model.d3.scene;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;

/**
 * Spatial index of a Scene's objects in 3D, in camera coordinates
 * 
 * <p>
 * The spatial index is constructed based on the current positions and orientations of the objects in the scene and the
 * camera. It is the responsability of the client code to create a new index if the index should reflect an updated
 * snapshot of that scene.
 * </p>
 */
public class SceneSpatialIndex {

	private Scene scene;

	private int xBins;

	private int yBins;

	private int zBins;

	private Map<SpatialBin, Collection<Object3D>> index;

	private Box3D firstBinBoundingBox;

	private ThreadLocal<List<ObjectSurfacePoint3D>> reusableIntersectionsList;

	private ThreadLocal<Set<Object3D>> reusableObjectsSet;

	private SceneSpatialIndex(Scene scene, int xBins, int yBins, int zBins) {
		this.scene = scene;
		this.xBins = xBins;
		this.yBins = yBins;
		this.zBins = zBins;
		this.index = new HashMap<SpatialBin, Collection<Object3D>>(xBins * yBins);
		this.reusableIntersectionsList = new ThreadLocal<List<ObjectSurfacePoint3D>>();
		this.reusableObjectsSet = new ThreadLocal<Set<Object3D>>();
	}

	public static SceneSpatialIndex createIndex(Scene scene) {
		SceneSpatialIndex index = new SceneSpatialIndex(scene, 50, 50, 50);
		index.addAllObjectsFromScene();
		return index;
	}

	public BinStatistics getBinStatistics() {
		return new BinStatistics();
	}

	public Iterator<ObjectSurfacePoint3D> getObjectIntersections(LineSegment3D line, boolean lineStartsWithinScene) {
		return new ObjectLineIntersectionsIterator(line, lineStartsWithinScene);
	}

	private void addAllObjectsFromScene() {
		setFirstBinBoundingBox(deriveFirstBinBoundingBox());
		for (Object3D object : SceneUtils.getAllIndividualObjectsInScene(getScene())) {
			addObject(object);
		}
	}

	private void addObject(Object3D object) {
		if (object.isBounded()) {
			Box3D bbox = object.asBoundedObject().getBoundingBox(CoordinateFrame.CAMERA, getCamera());
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
		Box3D sceneBox = getScene().getBoundingBox(CoordinateFrame.CAMERA);
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

	public Scene getScene() {
		return scene;
	}

	private Camera getCamera() {
		return getScene().getCamera();
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

	private List<ObjectSurfacePoint3D> getReusableIntersectionsList() {
		List<ObjectSurfacePoint3D> list = reusableIntersectionsList.get();
		if (list == null) {
			list = new Vector<ObjectSurfacePoint3D>();
			reusableIntersectionsList.set(list);
		}
		return list;
	}

	private Set<Object3D> getReusableObjectsSet() {
		Set<Object3D> set = reusableObjectsSet.get();
		if (set == null) {
			set = new HashSet<Object3D>(300);
			reusableObjectsSet.set(set);
		}
		return set;
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

	private class ObjectLineIntersectionsIterator implements Iterator<ObjectSurfacePoint3D> {

		private LineSegment3D line;

		private boolean lineStartsWithinScene;

		private double x1, x2, xd, y1, y2, yd, z1, z2, zd;

		private int xdir, ydir, zdir;

		private int xi, yi, zi;

		private int xn, yn, zn;

		private boolean xin, yin, zin;

		private double tx, ty, tz;

		private boolean proceed;

		public ObjectLineIntersectionsIterator(LineSegment3D line, boolean lineStartsWithinScene) {
			this.line = line;
			this.lineStartsWithinScene = lineStartsWithinScene;
			// init X
			x1 = line.getP1().getX();
			x2 = line.getP2().getX();
			xd = x2 - x1;
			xdir = (int) Math.signum(xd);
			xi = mapToXbin(x1);
			xn = getXbins() - 1;
			xin = xi >= 0 && xi <= xn;
			// init Y
			y1 = line.getP1().getY();
			y2 = line.getP2().getY();
			yd = y2 - y1;
			ydir = (int) Math.signum(yd);
			yi = mapToYbin(y1);
			yn = getYbins() - 1;
			yin = yi >= 0 && yi <= yn;
			// init Z
			z1 = line.getP1().getZ();
			z2 = line.getP2().getZ();
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
			proceed = !lineStartsWithinScene || (xin && yin && zin);
			getIntersections().clear();
			getObjects().clear();
		}

		@Override
		public boolean hasNext() {
			if (getIntersections().isEmpty()) {
				provisionIntersections();
				return !getIntersections().isEmpty();
			} else {
				return true;
			}
		}

		@Override
		public ObjectSurfacePoint3D next() {
			if (hasNext()) {
				return getIntersections().remove(getIntersections().size() - 1);
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private void provisionIntersections() {
			// traverse bins along the line to add objects
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			Set<Object3D> objects = getObjects();
			while (proceed && intersections.isEmpty()) {
				if (xin && yin && zin) {
					Collection<Object3D> binObjects = getObjectsInBin(xi, yi, zi);
					if (binObjects != null) {
						for (Object3D object : binObjects) {
							if (objects.add(object) && object.isRaytraceable()) {
								object.asRaytraceableObject()
										.intersectWithLightRay(line, getScene(), intersections);
							}
						}
					}
				}
				if (tx <= ty && tx <= tz) {
					xi += xdir;
					tx = (getBinBoundaryX(xi, xdir) - x1) / xd;
					xin = xi >= 0 && xi <= xn;
					proceed = proceed && (!lineStartsWithinScene || xin);
				} else if (ty <= tx && ty <= tz) {
					yi += ydir;
					ty = (getBinBoundaryY(yi, ydir) - y1) / yd;
					yin = yi >= 0 && yi <= yn;
					proceed = proceed && (!lineStartsWithinScene || yin);
				} else {
					zi += zdir;
					tz = (getBinBoundaryZ(zi, zdir) - z1) / zd;
					zin = zi >= 0 && zi <= zn;
					proceed = proceed && (!lineStartsWithinScene || zin);
				}
				proceed = proceed && (tx <= 1.0 || ty <= 1.0 || tz <= 1.0);
			}
		}

		private List<ObjectSurfacePoint3D> getIntersections() {
			return getReusableIntersectionsList();
		}

		private Set<Object3D> getObjects() {
			return getReusableObjectsSet();
		}

	}

	public class BinStatistics {

		public BinStatistics() {
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Scene Spatial Index statistics {\n");
			sb.append("\tBins: ").append(getXbins() + " x " + getYbins() + " x " + getZbins()).append("\n");
			sb.append("\tEmpty bins: ").append(getEmptyBins()).append("\n");
			sb.append("\tMaximum objects per spatial bin: ").append(getMaximumObjectsPerBin()).append("\n");
			sb.append("\tAverage objects per spatial bin: ").append(Math.floor(getAverageObjectsPerBin() * 10) / 10)
					.append("\n");
			sb.append("\tHistogram non-empty bins ")
					.append(getObjectsPerBinHistogram(20).toCsvString().replace("\n", "\n\t")).append("---\n");
			sb.append("}");
			return sb.toString();
		}

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

		public double getAverageObjectsPerBin() {
			int sum = 0;
			for (int zi = 0; zi < getZbins(); zi++) {
				for (int yi = 0; yi < getYbins(); yi++) {
					for (int xi = 0; xi < getXbins(); xi++) {
						Collection<Object3D> objects = getObjectsInBin(xi, yi, zi);
						if (objects != null) {
							sum += objects.size();
						}
					}
				}
			}
			return (double) sum / (getYbins() * getXbins() * getZbins());
		}

		public ObjectsPerBinHistogram getObjectsPerBinHistogram(int classCount) {
			int classRangeSize = (int) Math.ceil(getMaximumObjectsPerBin() / (double) classCount);
			return new ObjectsPerBinHistogram(classCount, classRangeSize);
		}

	}

	public class ObjectsPerBinHistogram {

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
				lowerBounds[i] = Math.max(i * size, 1); // Excluding empty bins
			}
			return lowerBounds;
		}

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

		public int getClassCount() {
			return classCount;
		}

		public int getClassRangeSize() {
			return classRangeSize;
		}

	}

}