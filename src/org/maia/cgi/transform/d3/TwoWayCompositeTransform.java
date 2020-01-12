package org.maia.cgi.transform.d3;

import java.util.List;

import org.maia.cgi.geometry.d3.Point3D;

/**
 * Represents a sequence of transformations, supporting both "forward" and "reverse" application
 * 
 * <p>
 * Essentially brings together one <code>CompositeTransform</code> (for forward application) and one
 * <code>ReverseCompositeTransform</code> (for reverse application), referencing a sequence of individual
 * transformations and their inverses, respectively.
 * </p>
 * 
 * @see CompositeTransform
 * @see ReverseCompositeTransform
 */
public class TwoWayCompositeTransform {

	private CompositeTransform forwardCompositeTransform;

	private CompositeTransform reverseCompositeTransform;

	public TwoWayCompositeTransform() {
		this.forwardCompositeTransform = new CompositeTransform();
		this.reverseCompositeTransform = new ReverseCompositeTransform();
	}

	public TwoWayCompositeTransform then(TransformMatrix matrix) {
		getForwardCompositeTransform().then(matrix);
		getReverseCompositeTransform().then(Transformation.getInverseMatrix(matrix));
		// Set the inverses of the both current composite matrices
		getForwardCompositeMatrix().setInverseMatrix(getReverseCompositeMatrix());
		getReverseCompositeMatrix().setInverseMatrix(getForwardCompositeMatrix());
		return this;
	}

	public TwoWayCompositeTransform undo() {
		getForwardCompositeTransform().undo();
		getReverseCompositeTransform().undo();
		return this;
	}

	public TwoWayCompositeTransform undoFrom(int stepIndex) {
		getForwardCompositeTransform().undoFrom(stepIndex);
		getReverseCompositeTransform().undoFrom(stepIndex);
		return this;
	}

	public TwoWayCompositeTransform replace(int stepIndex, TransformMatrix matrix) {
		getForwardCompositeTransform().replace(stepIndex, matrix);
		getReverseCompositeTransform().replace(stepIndex, Transformation.getInverseMatrix(matrix));
		// Set the inverses of the both current composite matrices
		getForwardCompositeMatrix().setInverseMatrix(getReverseCompositeMatrix());
		getReverseCompositeMatrix().setInverseMatrix(getForwardCompositeMatrix());
		return this;
	}

	public TwoWayCompositeTransform reset() {
		getForwardCompositeTransform().reset();
		getReverseCompositeTransform().reset();
		return this;
	}

	public int getIndexOfCurrentStep() {
		return getForwardCompositeTransform().getIndexOfCurrentStep();
	}

	public Point3D forwardTransform(Point3D point) {
		return getForwardCompositeMatrix().transform(point);
	}

	public List<Point3D> forwardTransform(List<Point3D> points) {
		return getForwardCompositeMatrix().transform(points);
	}

	public Point3D reverseTransform(Point3D point) {
		return getReverseCompositeMatrix().transform(point);
	}

	public List<Point3D> reverseTransform(List<Point3D> points) {
		return getReverseCompositeMatrix().transform(points);
	}

	public TransformMatrix getForwardCompositeMatrix() {
		return getForwardCompositeTransform().getCompositeMatrix();
	}

	public TransformMatrix getReverseCompositeMatrix() {
		return getReverseCompositeTransform().getCompositeMatrix();
	}

	private CompositeTransform getForwardCompositeTransform() {
		return forwardCompositeTransform;
	}

	private CompositeTransform getReverseCompositeTransform() {
		return reverseCompositeTransform;
	}

}
