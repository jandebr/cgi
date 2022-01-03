package org.maia.cgi.model.d3.scene.index;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.maia.cgi.geometry.d2.Rectangle2D;
import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.object.BoundedObject3D;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.ReusableObjectPack;

/**
 * 2D index of a <code>Scene</code>'s objects projected to the scene's view plane, represented as a rectilinear grid
 * <p>
 * In this type of index, the grid is constructed by dividing on the <code>XY</code> plane, whereas the <code>Z</code>
 * axis is left untouched.
 * </p>
 * <p>
 * As an implementation note, the grid is defined on the canonical view volume rather than the view plane itself. The
 * advantage is that {@link BoundedObject3D#getBoundingBoxInViewVolumeCoordinates()} can be used as the projection of an
 * object onto the grid.
 * </p>
 * <p>
 * The index is constructed based on the current positions and orientations of the objects in the scene and the camera.
 * It is the responsability of the client code to create a new index to reflect an updated snapshot of that scene.
 * </p>
 */
public class NonUniformlyBinnedSceneViewPlaneIndex extends NonUniformlyBinnedSceneSpatialIndex implements
		SceneViewPlaneIndex {

	public NonUniformlyBinnedSceneViewPlaneIndex(Scene scene, int maximumLeafBins) {
		super(scene, maximumLeafBins);
	}

	@Override
	public void buildIndex() {
		super.buildIndex();
		sortBinnedObjectsByIncreasingDepth();
	}

	private void sortBinnedObjectsByIncreasingDepth() {
		Comparator<Object3D> comparator = new ObjectSorterByIncreasingDepth();
		for (Iterator<SpatialBin> it = getDepthFirstLeafBinIterator(); it.hasNext();) {
			Collections.sort(it.next().getContainedObjects(), comparator);
		}
	}

	@Override
	public Iterator<Object3D> getViewPlaneObjects(Point3D pointOnViewPlane, ReusableObjectPack reusableObjects) {
		Point3D pointInViewVolume = projectToViewVolume(pointOnViewPlane, reusableObjects);
		SpatialBin leafBin = findLeafBinContaining(pointInViewVolume, reusableObjects);
		if (leafBin != null) {
			return new ViewPlaneObjectsIterator(leafBin, pointInViewVolume);
		} else {
			return EmptyViewPlaneObjectsIterator.instance;
		}
	}

	private Point3D projectToViewVolume(Point3D pointOnViewPlane, ReusableObjectPack reusableObjects) {
		Point3D pointInViewVolume = reusableObjects.getPointInViewVolume();
		Rectangle2D vpr = getCamera().getViewVolume().getViewPlaneRectangle();
		pointInViewVolume.setX((pointOnViewPlane.getX() - vpr.getX1()) / vpr.getWidth() * 2.0 - 1.0);
		pointInViewVolume.setY((pointOnViewPlane.getY() - vpr.getY1()) / vpr.getHeight() * 2.0 - 1.0);
		pointInViewVolume.setZ(-1.0); // view plane = near plane
		return pointInViewVolume;
	}

	private SpatialBin findLeafBinContaining(Point3D pointInViewVolume, ReusableObjectPack reusableObjects) {
		SpatialBin leafBin = null;
		SpatialBin lastBin = reusableObjects.getLastVisitedLeafBin().getBin();
		if (lastBin != null) {
			leafBin = lastBin.findLeafBinContaining(pointInViewVolume);
		} else {
			leafBin = super.findLeafBinContaining(pointInViewVolume);
		}
		reusableObjects.getLastVisitedLeafBin().setBin(leafBin);
		return leafBin;
	}

	@Override
	protected final boolean splitBinsExclusivelyInXY() {
		return true;
	}

	@Override
	protected final boolean keepTrackOfBinNeighbors() {
		return false;
	}

	@Override
	protected Box3D getSceneBox() {
		return Box3D.canonical(); // entire canonical view volume
	}

	@Override
	protected Box3D getObjectBox(Object3D object) {
		Box3D box = null;
		if (object.isBounded()) {
			box = object.asBoundedObject().getBoundingBoxInViewVolumeCoordinates(getCamera());
		}
		return box;
	}

	private class ObjectSorterByIncreasingDepth implements Comparator<Object3D> {

		public ObjectSorterByIncreasingDepth() {
		}

		@Override
		public int compare(Object3D o1, Object3D o2) {
			double nearDepth1 = -o1.asBoundedObject().getBoundingBoxInCameraCoordinates(getCamera()).getZ2();
			double nearDepth2 = -o2.asBoundedObject().getBoundingBoxInCameraCoordinates(getCamera()).getZ2();
			if (nearDepth1 < nearDepth2) {
				return -1;
			} else if (nearDepth1 > nearDepth2) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	private class ViewPlaneObjectsIterator implements Iterator<Object3D> {

		private List<Object3D> leafBinObjects;

		private int currentIndex;

		private Point3D pointInViewVolume;

		public ViewPlaneObjectsIterator(SpatialBin leafBin, Point3D pointInViewVolume) {
			this.leafBinObjects = leafBin.getContainedObjects();
			this.pointInViewVolume = pointInViewVolume;
		}

		@Override
		public boolean hasNext() {
			provisionNextObject();
			return currentIndex < leafBinObjects.size();
		}

		@Override
		public Object3D next() {
			if (hasNext()) {
				return leafBinObjects.get(currentIndex++);
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private void provisionNextObject() {
			while (currentIndex < leafBinObjects.size() && !accept(leafBinObjects.get(currentIndex))) {
				currentIndex++;
			}
		}

		private boolean accept(Object3D object) {
			return containsInXY(getObjectBox(object), pointInViewVolume);
		}

		private boolean containsInXY(Box3D box, Point3D point) {
			if (point.getX() < box.getX1() || point.getX() > box.getX2())
				return false;
			if (point.getY() < box.getY1() || point.getY() > box.getY2())
				return false;
			return true;
		}

	}

	private static class EmptyViewPlaneObjectsIterator implements Iterator<Object3D> {

		public static EmptyViewPlaneObjectsIterator instance = new EmptyViewPlaneObjectsIterator();

		private EmptyViewPlaneObjectsIterator() {
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Object3D next() {
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public static class ReusableLastVisitedLeafBin {

		private SpatialBin bin;

		public ReusableLastVisitedLeafBin() {
		}

		private SpatialBin getBin() {
			return bin;
		}

		private void setBin(SpatialBin bin) {
			this.bin = bin;
		}

	}

}