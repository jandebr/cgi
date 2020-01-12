package org.maia.cgi.model.d3.object;

public interface ComposableObject3D extends Object3D {

	<T extends ComposableObject3D> CompositeObject3D<T> getCompositeObject();

}
