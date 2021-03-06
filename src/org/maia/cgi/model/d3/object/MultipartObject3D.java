package org.maia.cgi.model.d3.object;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Box3D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.metrics.Metrics;
import org.maia.cgi.model.d3.CoordinateFrame;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.scene.Scene;

public class MultipartObject3D<T extends ComposableObject3D> extends BaseObject3D implements CompositeObject3D<T> {

	private Collection<T> parts;

	public MultipartObject3D() {
		this.parts = new Vector<T>();
	}

	public MultipartObject3D(Collection<T> parts) {
		this();
		addParts(parts);
	}

	public void addParts(Collection<T> parts) {
		for (T part : parts) {
			addPart(part);
		}
	}

	@SuppressWarnings("unchecked")
	public void addPart(T part) {
		if (part instanceof BaseObject3D) {
			((BaseObject3D) part).setCompositeObject((CompositeObject3D<BaseObject3D>) this);
		}
		getParts().add(part);
	}

	@Override
	public final boolean isComposite() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final CompositeObject3D<T> asCompositeObject() {
		return this;
	}

	@Override
	protected Box3D deriveBoundingBox(CoordinateFrame cframe, Camera camera) {
		Metrics.getInstance().incrementBoundingBoxComputations();
		Box3D bbox = null;
		for (Iterator<T> it = getParts().iterator(); it.hasNext();) {
			Object3D part = it.next();
			if (part.isBounded()) {
				Box3D partBox = part.asBoundedObject().getBoundingBox(cframe, camera);
				if (partBox != null) {
					if (bbox == null) {
						bbox = partBox.clone();
					} else {
						bbox.expandToContain(partBox);
					}
				}
			}
		}
		return bbox;
	}

	@Override
	public void intersectWithRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			boolean applyShading) {
		for (Iterator<T> it = getParts().iterator(); it.hasNext();) {
			Object3D part = it.next();
			if (part.isRaytraceable()) {
				part.asRaytraceableObject().intersectWithRay(ray, scene, intersections, applyShading);
			}
		}
	}

	@Override
	public void notifySelfHasTransformed() {
		super.notifySelfHasTransformed();
		fireAncestorHasTransformedOnParts();
	}

	@Override
	public void notifyAncestorHasTransformed() {
		super.notifyAncestorHasTransformed();
		fireAncestorHasTransformedOnParts();
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		super.cameraHasChanged(camera);
		fireCameraHasChangedOnParts(camera);
	}

	private void fireAncestorHasTransformedOnParts() {
		for (Iterator<T> it = getParts().iterator(); it.hasNext();) {
			Object3D part = it.next();
			if (part.isTransformable()) {
				part.asTransformableObject().notifyAncestorHasTransformed();
			}
		}
	}

	private void fireCameraHasChangedOnParts(Camera camera) {
		for (Iterator<T> it = getParts().iterator(); it.hasNext();) {
			Object3D part = it.next();
			part.cameraHasChanged(camera);
		}
	}

	@Override
	public Collection<T> getParts() {
		return parts;
	}

}
