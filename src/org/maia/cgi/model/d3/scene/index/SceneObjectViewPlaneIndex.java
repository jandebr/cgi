package org.maia.cgi.model.d3.scene.index;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.maia.cgi.model.d3.object.Object3D;
import org.maia.cgi.model.d3.scene.Scene;

public class SceneObjectViewPlaneIndex extends NonUniformlyBinnedSceneSpatialIndex {

	private double backZ;

	private double frontZ;

	private Map<Object3D, Box3D> objectBoxes;

	private ThreadLocal<SpatialBin> lastVisitedLeafBin;

	public SceneObjectViewPlaneIndex(Scene scene, int maximumLeafBins) {
		super(scene, maximumLeafBins);
		this.backZ = getViewVolume().getFarPlaneZ();
		this.frontZ = getViewVolume().getViewPlaneZ();
		this.objectBoxes = new HashMap<Object3D, Box3D>(1000);
		this.lastVisitedLeafBin = new ThreadLocal<SpatialBin>();
	}

	@Override
	public void buildIndex() {
		super.buildIndex();
		sortBinnedObjectsByIncreasingDepth();
	}

	public List<Object3D> getObjects(Point3D pointOnViewPlane) {
		SpatialBin leafBin = findLeafBinContaining(pointOnViewPlane);
		if (leafBin != null) {
			return leafBin.getContainedObjects();
		} else {
			return null;
		}
	}

	@Override
	protected SpatialBin findLeafBinContaining(Point3D point) {
		SpatialBin leafBin = null;
		SpatialBin lastBin = getLastVisitedLeafBin().get();
		if (lastBin != null) {
			leafBin = lastBin.findLeafBinContaining(point);
		} else {
			leafBin = super.findLeafBinContaining(point);
		}
		getLastVisitedLeafBin().set(leafBin);
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
			Box3D box = object.asBoundedObject().getBoundingBox(CoordinateFrame.CAMERA, getCamera());
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

	private ThreadLocal<SpatialBin> getLastVisitedLeafBin() {
		return lastVisitedLeafBin;
	}

	private class ObjectSorterByIncreasingDepth implements Comparator<Object3D> {

		@Override
		public int compare(Object3D o1, Object3D o2) {
			double nearDepth1 = -o1.asBoundedObject().getBoundingBox(CoordinateFrame.CAMERA, getCamera()).getZ2();
			double nearDepth2 = -o2.asBoundedObject().getBoundingBox(CoordinateFrame.CAMERA, getCamera()).getZ2();
			if (nearDepth1 < nearDepth2) {
				return -1;
			} else if (nearDepth1 > nearDepth2) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}