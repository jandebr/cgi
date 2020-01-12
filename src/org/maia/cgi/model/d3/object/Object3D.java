package org.maia.cgi.model.d3.object;

import org.maia.cgi.model.d3.camera.CameraObserver;

/**
 * An object in 3D space
 * 
 * <p>
 * An instance of <code>Object3D</code> is multi-faceted, which means it can implement multiple behaviours each
 * specified by a separate sub-interface of <code>Object3D</code>. For example, an instance can allow to be transformed
 * and then would implement the <code>TransformableObject3D</code> interface. Or it could be composed of any number of
 * part objects and then would implement the <code>CompositeObject3D</code> interface. Any combination is possible but
 * there can be no a priori assumption about any particular interface being implemented. For that reason, the
 * <code>Object3D</code> interface is comprised of a number of convenience methods that check for interface
 * compatibility and their corresponding casting methods. This leads to the following code idiom:
 * 
 * <pre>
 * if (object.isTransformable()) {
 * 	object.asTransformableObject().scale(2.0); // make it twice as large
 * } else {
 * 	// The shape of the object is fixed
 * }
 * </pre>
 * 
 * </p>
 */
public interface Object3D extends CameraObserver {

	boolean isBounded();

	BoundedObject3D asBoundedObject();

	boolean isRaytraceable();

	RaytraceableObject3D asRaytraceableObject();

	boolean isTransformable();

	TransformableObject3D asTransformableObject();

	boolean isComposite();

	<T extends ComposableObject3D> CompositeObject3D<T> asCompositeObject();

	boolean isComposable();

	ComposableObject3D asComposableObject();

	boolean isMesh();

	MeshObject3D asMeshObject();

}