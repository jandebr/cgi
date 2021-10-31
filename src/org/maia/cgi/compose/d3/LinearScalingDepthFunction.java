package org.maia.cgi.compose.d3;

public class LinearScalingDepthFunction implements DepthFunction {

	private DepthFunction baseFunction;

	private double lift;

	private double scale;

	public LinearScalingDepthFunction(DepthFunction baseFunction, double scale) {
		this(baseFunction, scale, 0);
	}

	public LinearScalingDepthFunction(DepthFunction baseFunction, double scale, double lift) {
		this.baseFunction = baseFunction;
		this.scale = scale;
		this.lift = lift;
	}

	@Override
	public double eval(double depth) {
		return getLift() + getScale() * getBaseFunction().eval(depth);
	}

	private DepthFunction getBaseFunction() {
		return baseFunction;
	}

	public double getLift() {
		return lift;
	}

	public double getScale() {
		return scale;
	}

}