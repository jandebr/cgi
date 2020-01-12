package org.maia.cgi.model.d3.object;

import java.util.Collection;

public interface CompositeObject3D<T extends ComposableObject3D> extends Object3D {

	Collection<T> getParts();

}
