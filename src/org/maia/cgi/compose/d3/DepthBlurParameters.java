package org.maia.cgi.compose.d3;

public class DepthBlurParameters implements Cloneable {

	private double relativeInflectionDepth;

	private double smoothness;

	private double maxBlurPixelRadius;

	private double maxRelativeDepthSimilarity;

	public DepthBlurParameters(double relativeInflectionDepth, double smoothness) {
		this(relativeInflectionDepth, smoothness, 5.0);
	}

	public DepthBlurParameters(double relativeInflectionDepth, double smoothness, double maxBlurPixelRadius) {
		this(relativeInflectionDepth, smoothness, maxBlurPixelRadius, 0.01);
	}

	/**
	 * Creates a new set of parameters for blurring an image according to its depth and a blur function
	 * 
	 * @param relativeInflectionDepth
	 *            A value between 0 and 1 representing the relative distance between the minimal image depth (as 0) and
	 *            maximal image depth (as 1) where the blur function inflects. More accurately, where the second
	 *            derivative of the function is 0
	 * @param smoothness
	 *            A strictly positive number (&gt; 0) that controls the smoothness of the blur function. A larger value
	 *            gives a more smooth function. More accurately, the first derivative is reduced with a higher value for
	 *            smoothness
	 * @param maxBlurPixelRadius
	 *            The pixel radius used for blurring one image pixel at the maximum level of blurring
	 * @param maxRelativeDepthSimilarity
	 *            The maximum relative (i.e., between 0 and 1) difference in depth for nearby pixels to be considered
	 *            part of the same surface and hence blurrable. This threshold is an effective measure to retain
	 *            sharpness of edges on nearby objects which partly cover far, blurred objects from sight
	 */
	public DepthBlurParameters(double relativeInflectionDepth, double smoothness, double maxBlurPixelRadius,
			double maxRelativeDepthSimilarity) {
		this.relativeInflectionDepth = relativeInflectionDepth;
		this.smoothness = smoothness;
		this.maxBlurPixelRadius = maxBlurPixelRadius;
		this.maxRelativeDepthSimilarity = maxRelativeDepthSimilarity;
	}

	@Override
	public DepthBlurParameters clone() {
		return new DepthBlurParameters(getRelativeInflectionDepth(), getSmoothness(), getMaxBlurPixelRadius(),
				getMaxRelativeDepthSimilarity());
	}

	public double getRelativeInflectionDepth() {
		return relativeInflectionDepth;
	}

	public void setRelativeInflectionDepth(double relativeInflectionDepth) {
		this.relativeInflectionDepth = relativeInflectionDepth;
	}

	public double getSmoothness() {
		return smoothness;
	}

	public void setSmoothness(double smoothness) {
		this.smoothness = smoothness;
	}

	public double getMaxBlurPixelRadius() {
		return maxBlurPixelRadius;
	}

	public void setMaxBlurPixelRadius(double maxBlurPixelRadius) {
		this.maxBlurPixelRadius = maxBlurPixelRadius;
	}

	public double getMaxRelativeDepthSimilarity() {
		return maxRelativeDepthSimilarity;
	}

	public void setMaxRelativeDepthSimilarity(double maxRelativeDepthSimilarity) {
		this.maxRelativeDepthSimilarity = maxRelativeDepthSimilarity;
	}

}