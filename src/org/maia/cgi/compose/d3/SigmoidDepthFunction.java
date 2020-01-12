package org.maia.cgi.compose.d3;

public class SigmoidDepthFunction implements DepthFunction {

	private SigmoidFunction function;

	private SigmoidDepthFunction(SigmoidFunction function) {
		this.function = function;
	}

	/**
	 * Creates a new Sigmoid-shaped function
	 * 
	 * @param nearDepth
	 *            The depth value for which the projected value is 0 : <code>function(nearDepth) = 0</code>
	 * @param farDepth
	 *            The depth value for which the projected value is 1 : <code>function(farDepth) = 1</code>
	 * @param relativeInflectionDepth
	 *            A value between 0 and 1 representing the relative distance between <code>nearDepth</code> (as 0) and
	 *            <code>farDepth</code> (as 1) where the function inflects. More accurately, where the first derivative
	 *            of the continuously increasing Sigmoid function is 0
	 * @param smoothness
	 *            A strictly positive number (&gt; 0) that controls the smoothness of the Sigmoid function. A larger
	 *            value gives a more smooth function. More accurately, the first derivative is reduced with a higher
	 *            value for smoothness
	 * @return A new instance of Sigmoid-shaped function
	 */
	public static SigmoidDepthFunction createFilter(double nearDepth, double farDepth, double relativeInflectionDepth,
			double smoothness) {
		double dd = farDepth - nearDepth;
		double b = nearDepth + relativeInflectionDepth * dd;
		double a = 12.0 / dd * (1.0 / smoothness);
		SigmoidFunction function = new SigmoidFunction(a, b);
		double y0 = function.eval(nearDepth);
		double y1 = function.eval(farDepth);
		double yd = y1 - y0;
		double s = 1.0 / yd;
		function.scale(s).translateY(-y0 * s); // such that: ft(nearDepth) = 0 and ft(farDepth) = 1
		return new SigmoidDepthFunction(function);
	}

	@Override
	public double eval(double depth) {
		return getFunction().eval(depth);
	}

	private SigmoidFunction getFunction() {
		return function;
	}

	private static class SigmoidFunction {

		private double a, b, c, d;

		public SigmoidFunction() {
			this(1.0, 0);
		}

		public SigmoidFunction(double a, double b) {
			this(a, b, 0, 1.0);
		}

		public SigmoidFunction(double a, double b, double c, double d) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}

		public double eval(double x) {
			return c + d / (1.0 + Math.exp(-a * (x - b)));
		}

		public SigmoidFunction scale(double scale) {
			this.d = scale;
			return this;
		}

		public SigmoidFunction translateY(double dy) {
			this.c = dy;
			return this;
		}

	}

}