package org.maia.cgi.model.d3.object;

import org.maia.cgi.transform.d3.TransformMatrix;

public interface TransformableObject3D extends Object3D {

	TransformableObject3D translateX(double distance);

	TransformableObject3D translateY(double distance);

	TransformableObject3D translateZ(double distance);

	TransformableObject3D translate(double dx, double dy, double dz);

	TransformableObject3D scaleX(double scale);

	TransformableObject3D scaleY(double scale);

	TransformableObject3D scaleZ(double scale);

	TransformableObject3D scale(double scale);

	TransformableObject3D scale(double sx, double sy, double sz);

	TransformableObject3D rotateX(double angleInRadians);

	TransformableObject3D rotateY(double angleInRadians);

	TransformableObject3D rotateZ(double angleInRadians);

	TransformableObject3D transform(TransformMatrix matrix);

	TransformableObject3D undoLastTransform();

	TransformableObject3D undoTransformsFrom(int stepIndex);

	TransformableObject3D replaceTransformAt(int stepIndex, TransformMatrix matrix);

	TransformableObject3D resetTransforms();

	int getIndexOfCurrentTransformStep();

	void notifySelfHasTransformed();

	void notifyAncestorHasTransformed();

}