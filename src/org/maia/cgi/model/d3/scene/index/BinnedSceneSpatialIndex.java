package org.maia.cgi.model.d3.scene.index;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.scene.Scene;

public abstract class BinnedSceneSpatialIndex extends SceneSpatialIndex {

	private ThreadLocal<List<ObjectSurfacePoint3D>> reusableIntersectionsList;

	private ThreadLocal<Set<Object3D>> reusableObjectsSet;

	protected BinnedSceneSpatialIndex(Scene scene) {
		super(scene);
		init();
	}

	protected void init() {
		this.reusableIntersectionsList = new ThreadLocal<List<ObjectSurfacePoint3D>>();
		this.reusableObjectsSet = new ThreadLocal<Set<Object3D>>();
	}

	@Override
	public String toString() {
		return getBinStatistics().toString();
	}

	@Override
	public void releaseMemory() {
		init();
	}

	public abstract BinStatistics getBinStatistics();

	protected List<ObjectSurfacePoint3D> getReusableIntersectionsList() {
		List<ObjectSurfacePoint3D> list = reusableIntersectionsList.get();
		if (list == null) {
			list = new Vector<ObjectSurfacePoint3D>();
			reusableIntersectionsList.set(list);
		}
		return list;
	}

	protected Set<Object3D> getReusableObjectsSet() {
		Set<Object3D> set = reusableObjectsSet.get();
		if (set == null) {
			set = new HashSet<Object3D>(300);
			reusableObjectsSet.set(set);
		}
		return set;
	}

	protected abstract class ObjectLineIntersectionsIterator implements Iterator<ObjectSurfacePoint3D> {

		private LineSegment3D line;

		protected ObjectLineIntersectionsIterator(LineSegment3D line) {
			this.line = line;
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

		protected abstract void provisionIntersections();

		protected LineSegment3D getLine() {
			return line;
		}

		protected List<ObjectSurfacePoint3D> getIntersections() {
			return getReusableIntersectionsList();
		}

		protected Set<Object3D> getObjects() {
			return getReusableObjectsSet();
		}

	}

	public abstract class BinStatistics {

		protected BinStatistics() {
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(BinnedSceneSpatialIndex.this.getClass().getSimpleName() + " statistics {\n");
			int n = getBinCount();
			int m = getEmptyBins();
			sb.append("\tBins: ").append(n).append("\n");
			sb.append("\tEmpty bins: ").append(m).append("\n");
			sb.append("\tNon-empty bins: ").append(n - m).append("\n");
			sb.append("\tMaximum objects per bin: ").append(getMaximumObjectsPerBin()).append("\n");
			sb.append("\tAverage objects per bin: ").append(Math.floor(getAverageObjectsPerBin() * 100) / 100)
					.append("\n");
			sb.append("\tAverage objects per non-empty bin: ")
					.append(Math.floor(getAverageObjectsPerNonEmptyBin() * 100) / 100).append("\n");
			sb.append("\tAverage objects per unit space: ")
					.append(Math.floor(getAverageObjectsPerUnitSpace() * 100) / 100).append("\n");
			sb.append("\tHistogram non-empty bins: ")
					.append(getObjectsPerBinHistogram(20).toCsvString().replace("\n", "\n\t")).append("---\n");
			sb.append("}");
			return sb.toString();
		}

		public abstract int getBinCount();

		public abstract int getEmptyBins();

		public abstract int getMaximumObjectsPerBin();

		public abstract double getAverageObjectsPerBin();

		public abstract double getAverageObjectsPerNonEmptyBin();

		public abstract double getAverageObjectsPerUnitSpace();

		public abstract ObjectsPerBinHistogram getObjectsPerBinHistogram(int classCount);

	}

	public abstract class ObjectsPerBinHistogram {

		private int classCount;

		private int classRangeSize;

		protected ObjectsPerBinHistogram(int classCount, int classRangeSize) {
			this.classCount = classCount;
			this.classRangeSize = classRangeSize;
		}

		public String toCsvString() {
			StringBuilder sb = new StringBuilder(getClassCount() * 8);
			sb.append("objects,count\n");
			int[] lowerBounds = getClassLowerBounds();
			int[] values = getClassValues();
			for (int i = 0; i < lowerBounds.length; i++) {
				if (i > 0 || getClassRangeSize() > 1) {
					sb.append(lowerBounds[i] + "+");
					sb.append(',');
					sb.append(values[i]);
					sb.append('\n');
				}
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

		public abstract int[] getClassValues();

		public int getClassCount() {
			return classCount;
		}

		public int getClassRangeSize() {
			return classRangeSize;
		}

	}

}