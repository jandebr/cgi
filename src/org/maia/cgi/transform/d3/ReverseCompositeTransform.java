package org.maia.cgi.transform.d3;

/**
 * Represents a sequence of (inverse) transformations, intended for "reverse" application
 * 
 * @see CompositeTransform
 */
public class ReverseCompositeTransform extends CompositeTransform {

	public ReverseCompositeTransform() {
	}

	@Override
	protected TransformMatrix extendCompositeMatrix(TransformMatrix composite, TransformMatrix matrix) {
		return matrix.postMultiply(composite);
	}

}
