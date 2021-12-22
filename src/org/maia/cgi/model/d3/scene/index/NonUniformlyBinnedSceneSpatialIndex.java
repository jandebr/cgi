package org.maia.cgi.model.d3.scene.index;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.scene.Scene;

/**
 * Spatial index of a Scene's objects in camera coordinates as a rectilinear grid of cuboids called "bins"
 * 
 * <p>
 * The binning strategy is designed to carve out empty space in a scene and to split bins that have a higher object
 * density into sub-bins with each a lower object density. This strategy results in a non-uniform (rectilinear) space
 * tessellation.
 * </p>
 * <p>
 * Compared to a uniform (Cartesion) tessellation as in <code>UniformlyBinnedSceneSpatialIndex</code>, the space is (in
 * general) more efficiently divided. It is not guaranteed but likely to result in a lower maximum number of contained
 * objects per bin and a lower average number of objects per unit space, given an equal amount of bins. If this is the
 * case, the advantage is that the same line traversal as in {@link #getObjectIntersections(LineSegment3D)} on average
 * meets less objects than with a uniform tessellation of the same scene. If object intersections are a costly
 * operation, this may improve performance over the <code>UniformlyBinnedSceneSpatialIndex</code>. However, the latter
 * benefits from a faster index creation, a more efficient bin traversal and less memory overhead. For a given scene,
 * advised is to experiment whichever method works out better. Note that the
 * {@link SceneSpatialIndexFactory#createIndex(Scene)} can be used to make this decision, however it is based on metrics
 * and cannot guarantee the best index for a use case.
 * </p>
 * <p>
 * The spatial index is constructed based on the current positions and orientations of the objects in the scene and the
 * camera. It is the responsability of the client code to create a new index to reflect an updated snapshot of that
 * scene.
 * </p>
 * 
 * @see UniformlyBinnedSceneSpatialIndex
 * @see SceneSpatialIndexFactory
 */
public class NonUniformlyBinnedSceneSpatialIndex extends BinnedSceneSpatialIndex {

	private SpatialBin rootBin;

	private int minimumBinObjectCount;

	private int minimumBinObjectReductionOnSplit;

	private int maximumBinTreeDepth;

	private int maximumLeafBins;

	private ThreadLocal<List<BinSide>> reusableDirectionsList;

	public NonUniformlyBinnedSceneSpatialIndex(Scene scene, int maximumLeafBins) {
		this(scene, 1, 1, 29, maximumLeafBins);
	}

	public NonUniformlyBinnedSceneSpatialIndex(Scene scene, int minimumBinObjectCount,
			int minimumBinObjectReductionOnSplit, int maximumBinTreeDepth, int maximumLeafBins) {
		super(scene);
		this.minimumBinObjectCount = minimumBinObjectCount;
		this.minimumBinObjectReductionOnSplit = minimumBinObjectReductionOnSplit;
		this.maximumBinTreeDepth = maximumBinTreeDepth;
		this.maximumLeafBins = maximumLeafBins;
		this.reusableDirectionsList = new ThreadLocal<List<BinSide>>();
		setRootBin(createRootBin());
		buildIndex();
	}

	public void printBins() {
		System.out.println(getRootBin());
	}

	@Override
	public void dispose() {
		setRootBin(null);
	}

	@Override
	public BinStatistics getBinStatistics() {
		return new NonUniformBinStatistics();
	}

	@Override
	public Iterator<ObjectSurfacePoint3D> getObjectIntersections(LineSegment3D line) {
		return new ObjectLineIntersectionsIteratorImpl(line);
	}

	private SpatialBin createRootBin() {
		return new SpatialBin(getSceneObjects(), getSceneBox());
	}

	private void buildIndex() {
		int leafs = 1; // root bin is a leaf initially
		int maxLeafs = getMaximumLeafBins();
		Deque<SpatialBin> queue = new LinkedList<SpatialBin>();
		queue.add(getRootBin());
		while (!queue.isEmpty() && leafs < maxLeafs) {
			SpatialBin bin = queue.pollFirst();
			if (bin.split()) {
				// Breadth-first traversal, to balance the bounded-size tree in depth
				queue.addLast(bin.getSplit().getFirstChildBin());
				queue.addLast(bin.getSplit().getSecondChildBin());
				leafs++; // bin no longer is a leaf, so -1 + 2
			}
		}
	}

	private SpatialBin getRootBin() {
		return rootBin;
	}

	private void setRootBin(SpatialBin rootBin) {
		this.rootBin = rootBin;
	}

	private int getMinimumBinObjectCount() {
		return minimumBinObjectCount;
	}

	private int getMinimumBinObjectReductionOnSplit() {
		return minimumBinObjectReductionOnSplit;
	}

	private int getMaximumBinTreeDepth() {
		return maximumBinTreeDepth;
	}

	private int getMaximumLeafBins() {
		return maximumLeafBins;
	}

	private List<BinSide> getReusableDirectionsList() {
		List<BinSide> list = reusableDirectionsList.get();
		if (list == null) {
			list = new Vector<BinSide>(3);
			reusableDirectionsList.set(list);
		}
		return list;
	}

	private class SpatialBin extends Box3D {

		private Collection<Object3D> containedObjects; // leafs only, 'null' for ancestors

		private int depthInTree; // zero at root

		private SpatialBin parent; // 'null' at root

		private BinSplit split; // ancestors only, 'null' for leafs

		private BinNeighbors neighbors; // leafs only, 'null' for ancestors

		private static final double emptySpaceCarveoutThreshold = 0.25; // minimum proportion (between 0 and 1)

		public SpatialBin(Collection<Object3D> containedObjects, Box3D bounds) {
			this(containedObjects, bounds.getX1(), bounds.getX2(), bounds.getY1(), bounds.getY2(), bounds.getZ1(),
					bounds.getZ2());
		}

		public SpatialBin(Collection<Object3D> containedObjects, double x1, double x2, double y1, double y2, double z1,
				double z2) {
			super(x1, x2, y1, y2, z1, z2);
			setNeighbors(new BinNeighbors());
			setContainedObjects(containedObjects);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(1024);
			builder.append("Bin {\n");
			builder.append('\t').append("Depth: ").append(getDepthInTree()).append('\n');
			builder.append('\t').append("Bounds: ").append(super.toString()).append('\n');
			if (isLeaf()) {
				builder.append('\t').append(getNeighbors()).append('\n');
				builder.append('\t').append("Object count: ").append(getContainedObjectCount()).append('\n');
			} else {
				builder.append('\t').append(getSplit().getCut()).append('\n');
				builder.append('\t').append("First child ")
						.append(getSplit().getFirstChildBin().toString().replace("\n", "\n\t")).append('\n');
				builder.append('\t').append("Second child ")
						.append(getSplit().getSecondChildBin().toString().replace("\n", "\n\t")).append('\n');
			}
			builder.append("}");
			return builder.toString();
		}

		public boolean split() {
			boolean hasSplit = false;
			if (isLeaf()) {
				int n = getContainedObjectCount();
				if (n > getMinimumBinObjectCount() && getDepthInTree() < getMaximumBinTreeDepth()) {
					BinCut cut = computeOptimalCut();
					if (cut != null) {
						BinSplit split = cut(cut);
						int m = Math.min(split.getFirstChildBin().getContainedObjectCount(), split.getSecondChildBin()
								.getContainedObjectCount());
						if (n - m >= getMinimumBinObjectReductionOnSplit()) {
							reallocateNeighbors(split);
							setSplit(split);
							setContainedObjects(null); // spread over child bins
							hasSplit = true;
						}
					}
				}
			}
			return hasSplit;
		}

		protected BinCut computeOptimalCut() {
			BinCut cut = null;
			Box3D bbox = getContainedObjectsClippedBoundingBox();
			if (bbox != null && !bbox.isCollapsed()) {
				// By preference, carve out large portions of empty space
				double st = emptySpaceCarveoutThreshold;
				double sLeft = bbox.getX1() - getX1();
				double sRight = getX2() - bbox.getX2();
				double sBottom = bbox.getY1() - getY1();
				double sTop = getY2() - bbox.getY2();
				double sBack = bbox.getZ1() - getZ1();
				double sFront = getZ2() - bbox.getZ2();
				double sx = Math.max(sLeft, sRight);
				double sy = Math.max(sBottom, sTop);
				double sz = Math.max(sBack, sFront);
				if (sx >= st && sx >= sy && sx >= sz) {
					cut = new BinCut(Dimension.X, sLeft >= sRight ? bbox.getX1() : bbox.getX2());
				} else if (sy >= st && sy >= sx && sy >= sz) {
					cut = new BinCut(Dimension.Y, sBottom >= sTop ? bbox.getY1() : bbox.getY2());
				} else if (sz >= st) {
					cut = new BinCut(Dimension.Z, sBack >= sFront ? bbox.getZ1() : bbox.getZ2());
				} else {
					// Otherwise divide the objects evenly along the longest dimension
					double w = bbox.getWidth();
					double h = bbox.getHeight();
					double d = bbox.getDepth();
					if (w >= h && w >= d) {
						cut = new BinCut(Dimension.X, (bbox.getX1() + bbox.getX2()) / 2);
					} else if (h >= w && h >= d) {
						cut = new BinCut(Dimension.Y, (bbox.getY1() + bbox.getY2()) / 2);
					} else {
						cut = new BinCut(Dimension.Z, (bbox.getZ1() + bbox.getZ2()) / 2);
					}
				}
			}
			return cut;
		}

		private BinSplit cut(BinCut cut) {
			BinSplit split = new BinSplit(cut);
			Dimension dim = cut.getDimension();
			double c = cut.getCoordinate();
			for (int i = 0; i < 2; i++) {
				double x1 = Dimension.X.equals(dim) && i == 1 ? c : getX1();
				double x2 = Dimension.X.equals(dim) && i == 0 ? c : getX2();
				double y1 = Dimension.Y.equals(dim) && i == 1 ? c : getY1();
				double y2 = Dimension.Y.equals(dim) && i == 0 ? c : getY2();
				double z1 = Dimension.Z.equals(dim) && i == 1 ? c : getZ1();
				double z2 = Dimension.Z.equals(dim) && i == 0 ? c : getZ2();
				Box3D bounds = new Box3D(x1, x2, y1, y2, z1, z2);
				Collection<Object3D> objects = getContainedObjectsOverlapping(bounds);
				SpatialBin bin = new SpatialBin(objects, bounds);
				bin.setDepthInTree(getDepthInTree() + 1);
				bin.setParent(this);
				if (i == 0) {
					split.setFirstChildBin(bin);
				} else {
					split.setSecondChildBin(bin);
				}
			}
			return split;
		}

		private void reallocateNeighbors(BinSplit split) {
			reallocateNeighbors(split, BinSide.LEFT);
			reallocateNeighbors(split, BinSide.RIGHT);
			reallocateNeighbors(split, BinSide.BOTTOM);
			reallocateNeighbors(split, BinSide.TOP);
			reallocateNeighbors(split, BinSide.BACK);
			reallocateNeighbors(split, BinSide.FRONT);
			setNeighbors(null); // spread over child bins
		}

		private void reallocateNeighbors(BinSplit split, BinSide side) {
			SpatialBin c1 = split.getFirstChildBin();
			SpatialBin c2 = split.getSecondChildBin();
			BinNeighbors n1 = c1.getNeighbors();
			BinNeighbors n2 = c2.getNeighbors();
			n1.markNeighborsStart(side);
			n2.markNeighborsStart(side);
			Iterator<SpatialBin> it = this.getNeighbors().iterator(side);
			Dimension cutDim = split.getCut().getDimension();
			if (side.getDimension().equals(cutDim)) {
				if (side.isFirstInDimension()) {
					while (it.hasNext()) {
						SpatialBin neighbor = it.next();
						n1.addNeighborInOrder(neighbor);
						neighbor.getNeighbors().replaceNeighbor(this, c1); // symmetric relation
					}
					n2.addNeighborInOrder(c1);
				} else {
					while (it.hasNext()) {
						SpatialBin neighbor = it.next();
						n2.addNeighborInOrder(neighbor);
						neighbor.getNeighbors().replaceNeighbor(this, c2); // symmetric relation
					}
					n1.addNeighborInOrder(c2);
				}
			} else {
				double co = split.getCut().getCoordinate();
				while (it.hasNext()) {
					SpatialBin neighbor = it.next();
					if (neighbor.getLargerCoordinate(cutDim) <= co) {
						n1.addNeighborInOrder(neighbor);
						neighbor.getNeighbors().replaceNeighbor(this, c1); // symmetric relation
					} else if (neighbor.getSmallerCoordinate(cutDim) >= co) {
						n2.addNeighborInOrder(neighbor);
						neighbor.getNeighbors().replaceNeighbor(this, c2); // symmetric relation
					} else {
						n1.addNeighborInOrder(neighbor);
						n2.addNeighborInOrder(neighbor);
						neighbor.getNeighbors().replaceNeighborWithPair(this, c1, c2); // symmetric relation
					}
				}
			}
		}

		private Box3D getContainedObjectsClippedBoundingBox() {
			Box3D bbox = null;
			for (Object3D object : getContainedObjects()) {
				if (object.isBounded()) {
					Box3D objectBox = getObjectBox(object);
					Box3D clippedBox = objectBox.getIntersection(this);
					if (bbox == null) {
						bbox = clippedBox;
					} else {
						bbox.expandToContain(clippedBox);
					}
				}
			}
			return bbox;
		}

		private Collection<Object3D> getContainedObjectsOverlapping(Box3D box) {
			Collection<Object3D> overlappingObjects = new Vector<Object3D>(1 + getContainedObjectCount() / 8);
			for (Object3D object : getContainedObjects()) {
				boolean overlaps = true;
				if (object.isBounded()) {
					Box3D objectBox = getObjectBox(object);
					overlaps = objectBox.overlaps(box);
				}
				if (overlaps) {
					overlappingObjects.add(object);
				}
			}
			return overlappingObjects;
		}

		private double getSmallerCoordinate(Dimension dim) {
			if (Dimension.X.equals(dim)) {
				return getX1();
			} else if (Dimension.Y.equals(dim)) {
				return getY1();
			} else {
				return getZ1();
			}
		}

		private double getLargerCoordinate(Dimension dim) {
			if (Dimension.X.equals(dim)) {
				return getX2();
			} else if (Dimension.Y.equals(dim)) {
				return getY2();
			} else {
				return getZ2();
			}
		}

		public SpatialBin findLeafBinContaining(Point3D point, BinSide xAffinity, BinSide yAffinity, BinSide zAffinity) {
			if (!contains(point)) {
				if (isRoot()) {
					return null;
				} else {
					return getParent().findLeafBinContaining(point, xAffinity, yAffinity, zAffinity);
				}
			} else {
				return findDescendantLeafBinContaining(point, xAffinity, yAffinity, zAffinity);
			}
		}

		private SpatialBin findDescendantLeafBinContaining(Point3D point, BinSide xAffinity, BinSide yAffinity,
				BinSide zAffinity) {
			if (isLeaf()) {
				return this;
			} else {
				BinSplit split = getSplit();
				SpatialBin c1 = split.getFirstChildBin();
				SpatialBin c2 = split.getSecondChildBin();
				double c = split.getCut().getCoordinate();
				Dimension dim = split.getCut().getDimension();
				if (Dimension.X.equals(dim)) {
					if (point.getX() < c || (point.getX() == c && BinSide.RIGHT.equals(xAffinity))) {
						return c1.findDescendantLeafBinContaining(point, xAffinity, yAffinity, zAffinity);
					} else {
						return c2.findDescendantLeafBinContaining(point, xAffinity, yAffinity, zAffinity);
					}
				} else if (Dimension.Y.equals(dim)) {
					if (point.getY() < c || (point.getY() == c && BinSide.TOP.equals(yAffinity))) {
						return c1.findDescendantLeafBinContaining(point, xAffinity, yAffinity, zAffinity);
					} else {
						return c2.findDescendantLeafBinContaining(point, xAffinity, yAffinity, zAffinity);
					}
				} else {
					if (point.getZ() < c || (point.getZ() == c && BinSide.FRONT.equals(zAffinity))) {
						return c1.findDescendantLeafBinContaining(point, xAffinity, yAffinity, zAffinity);
					} else {
						return c2.findDescendantLeafBinContaining(point, xAffinity, yAffinity, zAffinity);
					}
				}
			}
		}

		public boolean isEmpty() {
			return getContainedObjects().isEmpty();
		}

		public boolean isLeaf() {
			return split == null;
		}

		public boolean isRoot() {
			return parent == null;
		}

		public int getContainedObjectCount() {
			return getContainedObjects().size();
		}

		public Collection<Object3D> getContainedObjects() {
			return containedObjects;
		}

		private void setContainedObjects(Collection<Object3D> objects) {
			this.containedObjects = objects;
		}

		public int getDepthInTree() {
			return depthInTree;
		}

		private void setDepthInTree(int depthInTree) {
			this.depthInTree = depthInTree;
		}

		private SpatialBin getParent() {
			return parent;
		}

		private void setParent(SpatialBin parent) {
			this.parent = parent;
		}

		public BinSplit getSplit() {
			return split;
		}

		private void setSplit(BinSplit split) {
			this.split = split;
		}

		private BinNeighbors getNeighbors() {
			return neighbors;
		}

		private void setNeighbors(BinNeighbors neighbors) {
			this.neighbors = neighbors;
		}

	}

	private static class BinSplit {

		private BinCut cut;

		private SpatialBin firstChildBin;

		private SpatialBin secondChildBin;

		public BinSplit(BinCut cut) {
			this.cut = cut;
		}

		public SpatialBin getFirstChildBin() {
			return firstChildBin;
		}

		public void setFirstChildBin(SpatialBin firstChildBin) {
			this.firstChildBin = firstChildBin;
		}

		public SpatialBin getSecondChildBin() {
			return secondChildBin;
		}

		public void setSecondChildBin(SpatialBin secondChildBin) {
			this.secondChildBin = secondChildBin;
		}

		public BinCut getCut() {
			return cut;
		}

	}

	private static class BinCut {

		private Dimension dimension;

		private double coordinate;

		public BinCut(Dimension dimension, double coordinate) {
			this.dimension = dimension;
			this.coordinate = coordinate;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("BinCut [");
			builder.append(dimension);
			builder.append(" = ");
			builder.append(coordinate);
			builder.append("]");
			return builder.toString();
		}

		public Dimension getDimension() {
			return dimension;
		}

		public double getCoordinate() {
			return coordinate;
		}

	}

	private static enum Dimension {

		X,

		Y,

		Z;

	}

	private static enum BinSide {

		LEFT(Dimension.X, true),

		RIGHT(Dimension.X, false),

		BOTTOM(Dimension.Y, true),

		TOP(Dimension.Y, false),

		BACK(Dimension.Z, true),

		FRONT(Dimension.Z, false);

		private Dimension dimension;

		private boolean firstInDimension;

		private BinSide(Dimension dimension, boolean firstInDimension) {
			this.dimension = dimension;
			this.firstInDimension = firstInDimension;
		}

		public Dimension getDimension() {
			return dimension;
		}

		public boolean isFirstInDimension() {
			return firstInDimension;
		}

	}

	private static class BinNeighbors implements Iterable<SpatialBin> {

		private List<SpatialBin> neighbors; // in order : left, right, bottom, top, back, front

		private int neighborsLeftStartIndex;

		private int neighborsRightStartIndex;

		private int neighborsBottomStartIndex;

		private int neighborsTopStartIndex;

		private int neighborsBackStartIndex;

		private int neighborsFrontStartIndex;

		private static ThreadLocal<BinNeighborsIterator> reusableIterator;

		static {
			reusableIterator = new ThreadLocal<BinNeighborsIterator>();
		}

		public BinNeighbors() {
			this.neighbors = new Vector<SpatialBin>();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("BinNeighbors [");
			builder.append(getNeighborsCount(BinSide.LEFT) + " left");
			builder.append(", ");
			builder.append(getNeighborsCount(BinSide.RIGHT) + " right");
			builder.append(", ");
			builder.append(getNeighborsCount(BinSide.BOTTOM) + " bottom");
			builder.append(", ");
			builder.append(getNeighborsCount(BinSide.TOP) + " top");
			builder.append(", ");
			builder.append(getNeighborsCount(BinSide.BACK) + " back");
			builder.append(", ");
			builder.append(getNeighborsCount(BinSide.FRONT) + " front");
			builder.append("]");
			return builder.toString();
		}

		public void markNeighborsStart(BinSide side) {
			int index = getNeighbors().size();
			if (BinSide.LEFT.equals(side)) {
				neighborsLeftStartIndex = index;
			} else if (BinSide.RIGHT.equals(side)) {
				neighborsRightStartIndex = index;
			} else if (BinSide.BOTTOM.equals(side)) {
				neighborsBottomStartIndex = index;
			} else if (BinSide.TOP.equals(side)) {
				neighborsTopStartIndex = index;
			} else if (BinSide.BACK.equals(side)) {
				neighborsBackStartIndex = index;
			} else {
				neighborsFrontStartIndex = index;
			}
		}

		public void addNeighborInOrder(SpatialBin neighbor) {
			getNeighbors().add(neighbor);
		}

		public void replaceNeighbor(SpatialBin neighbor, SpatialBin replacement) {
			int i = getNeighbors().indexOf(neighbor);
			getNeighbors().set(i, replacement);
		}

		public void replaceNeighborWithPair(SpatialBin neighbor, SpatialBin replacement1, SpatialBin replacement2) {
			int i = getNeighbors().indexOf(neighbor);
			getNeighbors().set(i, replacement1);
			getNeighbors().add(i + 1, replacement2);
			if (neighborsRightStartIndex > i) {
				neighborsRightStartIndex++;
				neighborsBottomStartIndex++;
				neighborsTopStartIndex++;
				neighborsBackStartIndex++;
				neighborsFrontStartIndex++;
			} else if (neighborsBottomStartIndex > i) {
				neighborsBottomStartIndex++;
				neighborsTopStartIndex++;
				neighborsBackStartIndex++;
				neighborsFrontStartIndex++;
			} else if (neighborsTopStartIndex > i) {
				neighborsTopStartIndex++;
				neighborsBackStartIndex++;
				neighborsFrontStartIndex++;
			} else if (neighborsBackStartIndex > i) {
				neighborsBackStartIndex++;
				neighborsFrontStartIndex++;
			} else if (neighborsFrontStartIndex > i) {
				neighborsFrontStartIndex++;
			}
		}

		@Override
		public Iterator<SpatialBin> iterator() {
			return getReusableIterator().init(this, 0, getNeighbors().size());
		}

		public Iterator<SpatialBin> iterator(BinSide side) {
			return getReusableIterator().init(this, getNeighborsStartIndex(side), getNeighborsEndIndexExclusive(side));
		}

		private int getNeighborsCount(BinSide side) {
			return getNeighborsEndIndexExclusive(side) - getNeighborsStartIndex(side);
		}

		private int getNeighborsStartIndex(BinSide side) {
			if (BinSide.LEFT.equals(side)) {
				return neighborsLeftStartIndex;
			} else if (BinSide.RIGHT.equals(side)) {
				return neighborsRightStartIndex;
			} else if (BinSide.BOTTOM.equals(side)) {
				return neighborsBottomStartIndex;
			} else if (BinSide.TOP.equals(side)) {
				return neighborsTopStartIndex;
			} else if (BinSide.BACK.equals(side)) {
				return neighborsBackStartIndex;
			} else {
				return neighborsFrontStartIndex;
			}
		}

		private int getNeighborsEndIndexExclusive(BinSide side) {
			if (BinSide.LEFT.equals(side)) {
				return neighborsRightStartIndex;
			} else if (BinSide.RIGHT.equals(side)) {
				return neighborsBottomStartIndex;
			} else if (BinSide.BOTTOM.equals(side)) {
				return neighborsTopStartIndex;
			} else if (BinSide.TOP.equals(side)) {
				return neighborsBackStartIndex;
			} else if (BinSide.BACK.equals(side)) {
				return neighborsFrontStartIndex;
			} else {
				return getNeighbors().size();
			}
		}

		private List<SpatialBin> getNeighbors() {
			return neighbors;
		}

		private static BinNeighborsIterator getReusableIterator() {
			BinNeighborsIterator it = reusableIterator.get();
			if (it == null) {
				it = new BinNeighborsIterator();
				reusableIterator.set(it);
			}
			return it;
		}

	}

	private static class BinNeighborsIterator implements Iterator<SpatialBin> {

		private BinNeighbors neighbors;

		private int currentIndex;

		private int endIndexExclusive;

		public BinNeighborsIterator() {
		}

		public BinNeighborsIterator init(BinNeighbors neighbors, int startIndex, int endIndexExclusive) {
			this.neighbors = neighbors;
			this.currentIndex = startIndex;
			this.endIndexExclusive = endIndexExclusive;
			return this;
		}

		@Override
		public boolean hasNext() {
			return currentIndex < endIndexExclusive;
		}

		@Override
		public SpatialBin next() {
			return neighbors.getNeighbors().get(currentIndex++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private class ObjectLineIntersectionsIteratorImpl extends ObjectLineIntersectionsIterator {

		private SpatialBin currentBin;

		private Point3D currentPosition;

		private Iterator<Object3D> currentObjects;

		private double dx, dy, dz, sx, sy, sz;

		private boolean proceed;

		public ObjectLineIntersectionsIteratorImpl(LineSegment3D line) {
			super(line);
			Point3D p1 = line.getP1();
			Point3D p2 = line.getP2();
			dx = p2.getX() - p1.getX();
			dy = p2.getY() - p1.getY();
			dz = p2.getZ() - p1.getZ();
			sx = Math.signum(dx);
			sy = Math.signum(dy);
			sz = Math.signum(dz);
			BinSide xAffinity = dx >= 0 ? BinSide.LEFT : BinSide.RIGHT;
			BinSide yAffinity = dy >= 0 ? BinSide.BOTTOM : BinSide.TOP;
			BinSide zAffinity = dz >= 0 ? BinSide.BACK : BinSide.FRONT;
			currentBin = getRootBin().findLeafBinContaining(p1, xAffinity, yAffinity, zAffinity);
			currentPosition = p1.clone();
			proceed = currentBin != null;
		}

		@Override
		protected void provisionIntersections() {
			// traverse bins along the line to add objects
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			Set<Object3D> objects = getObjects();
			while (proceed && intersections.isEmpty()) {
				if (currentObjects == null) {
					currentObjects = currentBin.getContainedObjects().iterator();
				}
				if (currentObjects.hasNext()) {
					Object3D object = currentObjects.next();
					if (objects.add(object) && object.isRaytraceable()) {
						object.asRaytraceableObject().intersectWithLightRay(getLine(), getScene(), intersections);
					}
				} else {
					advancePositionToNextBin();
					currentObjects = null;
					proceed = currentBin != null;
				}
			}
		}

		private void advancePositionToNextBin() {
			// X-plane hit
			double px = currentPosition.getX();
			double rx = dx > 0 ? (currentBin.getX2() - px) / dx : (dx < 0 ? (currentBin.getX1() - px) / dx
					: Double.POSITIVE_INFINITY);
			// Y-plane hit
			double py = currentPosition.getY();
			double ry = dy > 0 ? (currentBin.getY2() - py) / dy : (dy < 0 ? (currentBin.getY1() - py) / dy
					: Double.POSITIVE_INFINITY);
			// Z-plane hit
			double pz = currentPosition.getZ();
			double rz = dz > 0 ? (currentBin.getZ2() - pz) / dz : (dz < 0 ? (currentBin.getZ1() - pz) / dz
					: Double.POSITIVE_INFINITY);
			// Closest side(s) hit
			List<BinSide> directions = getDirections();
			directions.clear();
			double qx, qy, qz;
			double r = Math.min(Math.min(rx, ry), rz);
			if (rx == r) {
				directions.add(dx > 0 ? BinSide.RIGHT : BinSide.LEFT);
				qx = dx > 0 ? currentBin.getX2() : currentBin.getX1();
			} else {
				qx = px + r * dx;
			}
			if (ry == r) {
				directions.add(dy > 0 ? BinSide.TOP : BinSide.BOTTOM);
				qy = dy > 0 ? currentBin.getY2() : currentBin.getY1();
			} else {
				qy = py + r * dy;
			}
			if (rz == r) {
				directions.add(dz > 0 ? BinSide.FRONT : BinSide.BACK);
				qz = dz > 0 ? currentBin.getZ2() : currentBin.getZ1();
			} else {
				qz = pz + r * dz;
			}
			// End of the line check
			Point3D p2 = getLine().getP2();
			if (qx * sx > p2.getX() * sx && qy * sy > p2.getY() * sy && qz * sz > p2.getZ() * sz) {
				currentBin = null;
			} else {
				// Advance position
				currentPosition.setX(qx);
				currentPosition.setY(qy);
				currentPosition.setZ(qz);
				// Advance bin
				for (BinSide dir : directions) {
					currentBin = findAdjacentBinContainingPoint(currentPosition, currentBin, dir);
				}
			}
		}

		private SpatialBin findAdjacentBinContainingPoint(Point3D point, SpatialBin homeBin, BinSide side) {
			if (homeBin != null) {
				Iterator<SpatialBin> neighbors = homeBin.getNeighbors().iterator(side);
				while (neighbors.hasNext()) {
					SpatialBin neighbor = neighbors.next();
					if (neighbor.contains(point))
						return neighbor;
				}
			}
			return null;
		}

		private List<BinSide> getDirections() {
			return getReusableDirectionsList();
		}

	}

	public class NonUniformBinStatistics extends BinStatistics {

		public NonUniformBinStatistics() {
		}

		@Override
		public int getBinCount() {
			int count = 0;
			for (DepthFirstLeafBinIterator it = new DepthFirstLeafBinIterator(getRootBin()); it.hasNext(); it.next()) {
				count++;
			}
			return count;
		}

		@Override
		public int getEmptyBins() {
			int empty = 0;
			for (DepthFirstLeafBinIterator it = new DepthFirstLeafBinIterator(getRootBin()); it.hasNext();) {
				if (it.next().isEmpty())
					empty++;
			}
			return empty;
		}

		@Override
		public int getMaximumObjectsPerBin() {
			int max = 0;
			for (DepthFirstLeafBinIterator it = new DepthFirstLeafBinIterator(getRootBin()); it.hasNext();) {
				max = Math.max(max, it.next().getContainedObjectCount());
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
			for (DepthFirstLeafBinIterator it = new DepthFirstLeafBinIterator(getRootBin()); it.hasNext();) {
				int n = it.next().getContainedObjectCount();
				if (n == 0) {
					if (includeEmptyBins)
						count++;
				} else {
					sum += n;
					count++;
				}
			}
			if (count == 0)
				return 0;
			return (double) sum / count;
		}

		@Override
		public double getAverageObjectsPerUnitSpace() {
			double weightedSum = 0;
			double totalVolume = 0;
			for (DepthFirstLeafBinIterator it = new DepthFirstLeafBinIterator(getRootBin()); it.hasNext();) {
				SpatialBin bin = it.next();
				double binVolume = bin.getWidth() * bin.getHeight() * bin.getDepth();
				weightedSum += binVolume * bin.getContainedObjectCount();
				totalVolume += binVolume;
			}
			if (totalVolume == 0)
				return 0;
			return weightedSum / totalVolume;
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
			for (DepthFirstLeafBinIterator it = new DepthFirstLeafBinIterator(getRootBin()); it.hasNext();) {
				int count = it.next().getContainedObjectCount();
				if (count > 0) {
					// Excluding empty bins
					int ci = Math.min((int) Math.floor(count / (double) size), n - 1);
					values[ci]++;
				}
			}
			return values;
		}

	}

	private static class DepthFirstLeafBinIterator implements Iterator<SpatialBin> {

		private Stack<SpatialBin> binStack;

		public DepthFirstLeafBinIterator(SpatialBin rootBin) {
			this.binStack = new Stack<SpatialBin>();
			this.binStack.push(rootBin);
			provisionNextLeaf();
		}

		@Override
		public boolean hasNext() {
			return !getBinStack().isEmpty();
		}

		@Override
		public SpatialBin next() {
			SpatialBin leaf = getBinStack().pop();
			provisionNextLeaf();
			return leaf;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private void provisionNextLeaf() {
			Stack<SpatialBin> stack = getBinStack();
			while (!stack.isEmpty() && !stack.peek().isLeaf()) {
				BinSplit split = stack.pop().getSplit();
				stack.push(split.getFirstChildBin());
				stack.push(split.getSecondChildBin());
			}
		}

		private Stack<SpatialBin> getBinStack() {
			return binStack;
		}

	}

}