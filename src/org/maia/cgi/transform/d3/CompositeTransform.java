package org.maia.cgi.transform.d3;

import java.util.List;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Point3D;

/**
 * Represents a sequence of transformations, intended for "forward" application
 * 
 * @see ReverseCompositeTransform
 */
public class CompositeTransform {

	/**
	 * The individual transformation matrices in the sequence. The order corresponds to the order in which the
	 * transformations are to be applied.
	 */
	private List<TransformMatrix> matrices = new Vector<TransformMatrix>();

	/**
	 * The combined result of applying all the transformation matrices in the subsequence.
	 * 
	 * In symbols,
	 * <code>C[i] = M[i] * C[i-1] = M[i] * M[i-1] * C[i-2] = ... = M[i] * M[i-1] * M[i-2] * ... * M[0]</code> , where
	 * <code>C</code> denotes <code>compositeMatrices</code> and <code>M</code> denotes <code>matrices</code>
	 */
	private List<TransformMatrix> compositeMatrices = new Vector<TransformMatrix>();

	public CompositeTransform() {
		reset(); // initialize with the Identity transform
	}

	public CompositeTransform then(TransformMatrix matrix) {
		getMatrices().add(matrix);
		getCompositeMatrices().add(extendCompositeMatrix(getCompositeMatrix(), matrix));
		return this;
	}

	public CompositeTransform undo() {
		int n = getMatrices().size();
		if (n > 1) {
			getMatrices().remove(n - 1);
			getCompositeMatrices().remove(n - 1);
		} else {
			throw new IllegalStateException(
					"Attempting to invoke CompositeTransform.previous() when in the initial state");
		}
		return this;
	}

	public CompositeTransform undoFrom(int stepIndex) {
		validateStepIndex(stepIndex);
		int n = getMatrices().size() - stepIndex;
		for (int i = 0; i < n; i++) {
			undo();
		}
		return this;
	}

	public CompositeTransform replace(int stepIndex, TransformMatrix matrix) {
		validateStepIndex(stepIndex);
		if (stepIndex == getIndexOfCurrentStep()) {
			return undo().then(matrix);
		} else {
			List<TransformMatrix> redo = new Vector<TransformMatrix>(getMatrices().subList(stepIndex + 1,
					getMatrices().size()));
			undoFrom(stepIndex).then(matrix);
			for (TransformMatrix R : redo) {
				then(R);
			}
			return this;
		}
	}

	public CompositeTransform reset() {
		getMatrices().clear();
		getCompositeMatrices().clear();
		return then(Transformation.getIdentityMatrix());
	}

	public int getIndexOfCurrentStep() {
		return getMatrices().size() - 1;
	}

	private void validateStepIndex(int stepIndex) {
		if (stepIndex <= 0 || stepIndex > getIndexOfCurrentStep())
			throw new IndexOutOfBoundsException("Step index out of bounds [1," + getIndexOfCurrentStep() + "]: "
					+ stepIndex);
	}

	public Point3D transform(Point3D point) {
		return getCompositeMatrix().transform(point);
	}

	public TransformMatrix getCompositeMatrix() {
		int n = getCompositeMatrices().size();
		if (n == 0) {
			return Transformation.getIdentityMatrix();
		} else {
			return getCompositeMatrices().get(n - 1);
		}
	}

	protected TransformMatrix extendCompositeMatrix(TransformMatrix composite, TransformMatrix matrix) {
		return matrix.preMultiply(composite);
	}

	public List<TransformMatrix> getMatrices() {
		return matrices;
	}

	private List<TransformMatrix> getCompositeMatrices() {
		return compositeMatrices;
	}

}
