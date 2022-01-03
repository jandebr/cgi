package org.maia.cgi.model.d3.scene.index;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.maia.cgi.geometry.d2.Point2D;
import org.maia.cgi.geometry.d2.Rectangle2D;
import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Plane3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.camera.ViewVolume;
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.render.d3.ReusableObjectPack;

/**
 * 2D index of a <code>Scene</code>'s objects projected to the scene's view plane, represented as a rectilinear grid
 * 
 * <p>
 * The index is constructed based on the current positions and orientations of the objects in the scene and the camera.
 * It is the responsability of the client code to create a new index to reflect an updated snapshot of that scene.
 * </p>
 */
public class NonUniformlyBinnedSceneViewPlaneIndex extends NonUniformlyBinnedSceneSpatialIndex implements
		SceneViewPlaneIndex {

	private double backZ;

	private double frontZ;

	private Map<Object3D, Box3D> objectBoxes;

	public NonUniformlyBinnedSceneViewPlaneIndex(Scene scene, int maximumLeafBins) {
		super(scene, maximumLeafBins);
		this.backZ = getViewVolume().getFarPlaneZ();
		this.frontZ = getViewVolume().getViewPlaneZ();
		this.objectBoxes = new HashMap<Object3D, Box3D>(1000);
	}

	@Override
	public void dispose() {
		super.dispose();
		getObjectBoxes().clear();
	}

	@Override
	public void buildIndex() {
		super.buildIndex();
		sortBinnedObjectsByIncreasingDepth();
	}

	@Override
	public Iterator<Object3D> getViewPlaneObjects(Point3D pointOnViewPlane, ReusableObjectPack reusableObjects) {
		SpatialBin leafBin = findLeafBinContaining(pointOnViewPlane, reusableObjects);
		if (leafBin != null) {
			return new ViewPlaneObjectsIterator(leafBin, pointOnViewPlane);
		} else {
			return EmptyViewPlaneObjectsIterator.instance;
		}
	}

	private SpatialBin findLeafBinContaining(Point3D pointOnViewPlane, ReusableObjectPack reusableObjects) {
		SpatialBin leafBin = null;
		SpatialBin lastBin = reusableObjects.getLastVisitedLeafBin().getBin();
		if (lastBin != null) {
			leafBin = lastBin.findLeafBinContaining(pointOnViewPlane);
		} else {
			leafBin = super.findLeafBinContaining(pointOnViewPlane);
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
		Rectangle2D vpr = getViewVolume().getViewPlaneRectangle();
		return new Box3D(vpr.getX1(), vpr.getX2(), vpr.getY1(), vpr.getY2(), getBackZ(), getFrontZ());
	}

	@Override
	protected Box3D getObjectBox(Object3D object) {
		Box3D box = null;
		if (getObjectBoxes().containsKey(object)) {
			box = getObjectBoxes().get(object);
		} else {
			Rectangle2D rect = getObjectBoundsClippedOnViewPlane(object);
			if (rect != null) {
				box = new Box3D(rect.getX1(), rect.getX2(), rect.getY1(), rect.getY2(), getBackZ(), getFrontZ());
			}
			getObjectBoxes().put(object, box);
		}
		return box;
	}

	private Rectangle2D getObjectBoundsClippedOnViewPlane(Object3D object) {
		if (object.isBounded()) {
			Box3D box = object.asBoundedObject().getBoundingBoxInCameraCoordinates(getCamera());
			return projectAndClipOntoViewPlane(box);
		} else {
			return getViewVolume().getViewPlaneRectangle(); // suppose it covers the entire view plane
		}
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

	private void sortBinnedObjectsByIncreasingDepth() {
		Comparator<Object3D> comparator = new ObjectSorterByIncreasingDepth();
		for (Iterator<SpatialBin> it = getDepthFirstLeafBinIterator(); it.hasNext();) {
			Collections.sort(it.next().getContainedObjects(), comparator);
		}
	}

	private Camera getCamera() {
		return getScene().getCamera();
	}

	private ViewVolume getViewVolume() {
		return getCamera().getViewVolume();
	}

	private double getBackZ() {
		return backZ;
	}

	private double getFrontZ() {
		return frontZ;
	}

	private Map<Object3D, Box3D> getObjectBoxes() {
		return objectBoxes;
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

		private Point3D pointOnViewPlane;

		public ViewPlaneObjectsIterator(SpatialBin leafBin, Point3D pointOnViewPlane) {
			this.leafBinObjects = leafBin.getContainedObjects();
			this.pointOnViewPlane = pointOnViewPlane;
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
			return getObjectBox(object).contains(pointOnViewPlane);
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