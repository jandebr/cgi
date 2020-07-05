package org.maia.cgi.geometry.d3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.maia.cgi.transform.d3.TransformMatrix;

/**
 * Standard Bézier curve defined by a sequence of two or more control points
 * 
 * <p>
 * This implementation supports <em>affine transformations</em> (unlike perspective projections) through the
 * <code>{@link #transform(TransformMatrix) transform}</code> method
 * </p>
 * 
 * <h4>Note on computational efficiency</h4>
 * <p>
 * The lower the number of control points, the more computationally efficient the sampling process is. This
 * implementation is however optimized as long as the number of control points does not exceed
 * {@link BezierCurve3D#MAXIMUM_EFFICIENT_CONTROL_POINTS}. A higher number of control points will exponentially increase
 * time complexity. For that reason, the number of control points cannot be higher than
 * {@link BezierCurve3D#MAXIMUM_CONTROL_POINTS}.
 * </p>
 */
public class BezierCurve3D implements Curve3D {

	private List<Point3D> controlPoints;

	public static final int MINIMUM_CONTROL_POINTS = 2;

	public static final int MAXIMUM_EFFICIENT_CONTROL_POINTS = 30;

	public static final int MAXIMUM_CONTROL_POINTS = 40;

	private static Map<Integer, Long> cachedBinomialCoefficients = new HashMap<Integer, Long>(100);

	public BezierCurve3D(List<Point3D> controlPoints) {
		checkParameters(controlPoints);
		this.controlPoints = controlPoints;
	}

	@Override
	public Point3D sample(double t) {
		t = Math.min(Math.max(t, 0.0), 1.0);
		double x = 0, y = 0, z = 0;
		int L = getControlPoints().size() - 1;
		for (int k = 0; k <= L; k++) {
			Point3D cp = getControlPoints().get(k);
			double w = evaluateBernsteinPolynomial(k, L, t);
			x += w * cp.getX();
			y += w * cp.getY();
			z += w * cp.getZ();
		}
		return new Point3D(x, y, z);
	}

	@Override
	public Curve3D transform(TransformMatrix matrix) {
		if (!matrix.isAffine())
			throw new UnsupportedOperationException("This curve only supports affine transformations");
		return new BezierCurve3D(matrix.transform(getControlPoints()));
	}

	private static void checkParameters(List<Point3D> controlPoints) {
		int n = controlPoints.size();
		if (n < MINIMUM_CONTROL_POINTS)
			throw new IllegalArgumentException("Too few control points (" + n + "), should be at least "
					+ MINIMUM_CONTROL_POINTS);
		if (n > MAXIMUM_CONTROL_POINTS)
			throw new IllegalArgumentException("Too many control points (" + n + "), should be at most "
					+ MAXIMUM_CONTROL_POINTS);
	}

	private static double evaluateBernsteinPolynomial(int k, int L, double t) {
		if (k < 0 || k > L) {
			return 0.0;
		} else {
			Long cachedCoefficient = getCachedBinomialCoefficient(k, L);
			if (cachedCoefficient != null) {
				return cachedCoefficient.longValue() * Math.pow(1.0 - t, L - k) * Math.pow(t, k);
			} else if (L < MAXIMUM_EFFICIENT_CONTROL_POINTS) {
				long coefficient = computeBinomialCoefficient(k, L);
				cacheBinomialCoefficient(k, L, coefficient);
				return coefficient * Math.pow(1.0 - t, L - k) * Math.pow(t, k);
			} else {
				return (1.0 - t) * evaluateBernsteinPolynomial(k, L - 1, t) + t
						* evaluateBernsteinPolynomial(k - 1, L - 1, t);
			}
		}
	}

	private static long computeBinomialCoefficient(int k, int L) {
		int max = Math.max(k, L - k);
		int min = Math.min(k, L - k);
		long n = 1L;
		for (int i = max + 1; i <= L; i++) {
			n *= i;
		}
		long d = 1L;
		for (int i = 2; i <= min; i++) {
			d *= i;
		}
		return n / d;
	}

	private static void cacheBinomialCoefficient(int k, int L, long coefficient) {
		getCachedBinomialCoefficients().put(getBinomialCoefficientCacheKey(k, L), coefficient);
	}

	private static Long getCachedBinomialCoefficient(int k, int L) {
		return getCachedBinomialCoefficients().get(getBinomialCoefficientCacheKey(k, L));
	}

	private static Integer getBinomialCoefficientCacheKey(int k, int L) {
		return (k << 16) | L;
	}

	private List<Point3D> getControlPoints() {
		return controlPoints;
	}

	private static Map<Integer, Long> getCachedBinomialCoefficients() {
		return cachedBinomialCoefficients;
	}

}