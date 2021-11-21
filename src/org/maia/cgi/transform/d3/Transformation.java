package org.maia.cgi.transform.d3;

import org.maia.cgi.Metrics;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.model.d3.OrthographicProjection;

public class Transformation {

	private static TransformMatrix IDENTITY_MATRIX;

	private Transformation() {
	}

	public static TransformMatrix getIdentityMatrix() {
		if (IDENTITY_MATRIX == null) {
			TransformMatrix I = createIdentityMatrix();
			I.setInverseMatrix(I);
			IDENTITY_MATRIX = I;
		}
		return IDENTITY_MATRIX;
	}

	private static TransformMatrix createIdentityMatrix() {
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, 1.0);
		builder.setValue(1, 1, 1.0);
		builder.setValue(2, 2, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getTranslationMatrix(double dx, double dy, double dz) {
		TransformMatrix M = createTranslationMatrix(dx, dy, dz);
		TransformMatrix I = createTranslationMatrix(-dx, -dy, -dz);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix createTranslationMatrix(double dx, double dy, double dz) {
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, 1.0);
		builder.setValue(0, 3, dx);
		builder.setValue(1, 1, 1.0);
		builder.setValue(1, 3, dy);
		builder.setValue(2, 2, 1.0);
		builder.setValue(2, 3, dz);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getScalingMatrix(double sx, double sy, double sz) {
		TransformMatrix M = createScalingMatrix(sx, sy, sz);
		TransformMatrix I = createScalingMatrix(1.0 / sx, 1.0 / sy, 1.0 / sz);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix createScalingMatrix(double sx, double sy, double sz) {
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, sx);
		builder.setValue(1, 1, sy);
		builder.setValue(2, 2, sz);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getRotationXrollMatrix(double angleInRadians) {
		TransformMatrix M = createRotationXrollMatrix(angleInRadians);
		TransformMatrix I = createRotationXrollMatrix(-angleInRadians);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix createRotationXrollMatrix(double angleInRadians) {
		double c = Math.cos(angleInRadians);
		double s = Math.sin(angleInRadians);
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, 1.0);
		builder.setValue(1, 1, c);
		builder.setValue(1, 2, -s);
		builder.setValue(2, 1, s);
		builder.setValue(2, 2, c);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getRotationYrollMatrix(double angleInRadians) {
		TransformMatrix M = createRotationYrollMatrix(angleInRadians);
		TransformMatrix I = createRotationYrollMatrix(-angleInRadians);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix createRotationYrollMatrix(double angleInRadians) {
		double c = Math.cos(angleInRadians);
		double s = Math.sin(angleInRadians);
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, c);
		builder.setValue(0, 2, s);
		builder.setValue(1, 1, 1.0);
		builder.setValue(2, 0, -s);
		builder.setValue(2, 2, c);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getRotationZrollMatrix(double angleInRadians) {
		TransformMatrix M = createRotationZrollMatrix(angleInRadians);
		TransformMatrix I = createRotationZrollMatrix(-angleInRadians);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix createRotationZrollMatrix(double angleInRadians) {
		double c = Math.cos(angleInRadians);
		double s = Math.sin(angleInRadians);
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, c);
		builder.setValue(0, 1, -s);
		builder.setValue(1, 0, s);
		builder.setValue(1, 1, c);
		builder.setValue(2, 2, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getOrthographicProjectionMatrix(OrthographicProjection projection) {
		if (OrthographicProjection.ONTO_XY_PLANE.equals(projection)) {
			return getOrthographicProjectionOntoXYplaneMatrix();
		} else if (OrthographicProjection.ONTO_XZ_PLANE.equals(projection)) {
			return getOrthographicProjectionOntoXZplaneMatrix();
		} else if (OrthographicProjection.ONTO_YZ_PLANE.equals(projection)) {
			return getOrthographicProjectionOntoYZplaneMatrix();
		}
		return null;
	}

	public static TransformMatrix getOrthographicProjectionOntoXYplaneMatrix() {
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, 1.0);
		builder.setValue(1, 1, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getOrthographicProjectionOntoXZplaneMatrix() {
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, 1.0);
		builder.setValue(2, 2, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getOrthographicProjectionOntoYZplaneMatrix() {
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(1, 1, 1.0);
		builder.setValue(2, 2, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getCameraViewingMatrix(Point3D eye, Vector3D uUnit, Vector3D vUnit, Vector3D nUnit) {
		Vector3D e = eye.minus(Point3D.origin());
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, uUnit.getX());
		builder.setValue(0, 1, uUnit.getY());
		builder.setValue(0, 2, uUnit.getZ());
		builder.setValue(0, 3, -e.dotProduct(uUnit));
		builder.setValue(1, 0, vUnit.getX());
		builder.setValue(1, 1, vUnit.getY());
		builder.setValue(1, 2, vUnit.getZ());
		builder.setValue(1, 3, -e.dotProduct(vUnit));
		builder.setValue(2, 0, nUnit.getX());
		builder.setValue(2, 1, nUnit.getY());
		builder.setValue(2, 2, nUnit.getZ());
		builder.setValue(2, 3, -e.dotProduct(nUnit));
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix getPerspectiveProjectionMatrix(double viewAngleInRadians, double aspectRatio,
			double N, double F) {
		double top = N * Math.tan(viewAngleInRadians / 2);
		double bottom = -top;
		double right = top * aspectRatio;
		double left = -right;
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		builder.setValue(0, 0, 2 * N / (right - left));
		builder.setValue(0, 2, (right + left) / (right - left));
		builder.setValue(1, 1, 2 * N / (top - bottom));
		builder.setValue(1, 2, (top + bottom) / (top - bottom));
		builder.setValue(2, 2, -(F + N) / (F - N));
		builder.setValue(2, 3, -2 * F * N / (F - N));
		builder.setValue(3, 2, -1.0);
		return builder.build();
	}

	public static TransformMatrix getInverseMatrix(TransformMatrix matrix) throws MatrixInversionException {
		TransformMatrix inverse = matrix.getInverseMatrix();
		if (inverse == null) {
			inverse = createInverseMatrix(matrix);
			inverse.setInverseMatrix(matrix); // wiring for future reuse
			matrix.setInverseMatrix(inverse); // wiring for future reuse
		}
		return inverse;
	}

	private static TransformMatrix createInverseMatrix(TransformMatrix matrix) throws MatrixInversionException {
		Metrics.getInstance().incrementMatrixInversions();
		double det = computeDeterminant(matrix);
		if (det == 0)
			throw new MatrixInversionException();
		double[] T = matrix.getValues();
		TransformMatrixBuilder builder = new TransformMatrixBuilder();
		if (matrix.isAffine()) {
			builder.setValue(0, 0, (T[5] * T[10] - T[6] * T[9]) / det);
			builder.setValue(0, 1, (T[2] * T[9] - T[1] * T[10]) / det);
			builder.setValue(0, 2, (T[1] * T[6] - T[2] * T[5]) / det);
			builder.setValue(0, 3, (T[3] * T[6] * T[9] - T[2] * T[7] * T[9] - T[3] * T[5] * T[10] + T[1] * T[7] * T[10]
					+ T[2] * T[5] * T[11] - T[1] * T[6] * T[11])
					/ det);
			builder.setValue(1, 0, (T[6] * T[8] - T[4] * T[10]) / det);
			builder.setValue(1, 1, (T[0] * T[10] - T[2] * T[8]) / det);
			builder.setValue(1, 2, (T[2] * T[4] - T[0] * T[6]) / det);
			builder.setValue(1, 3, (T[2] * T[7] * T[8] - T[3] * T[6] * T[8] + T[3] * T[4] * T[10] - T[0] * T[7] * T[10]
					- T[2] * T[4] * T[11] + T[0] * T[6] * T[11])
					/ det);
			builder.setValue(2, 0, (T[4] * T[9] - T[5] * T[8]) / det);
			builder.setValue(2, 1, (T[1] * T[8] - T[0] * T[9]) / det);
			builder.setValue(2, 2, (T[0] * T[5] - T[1] * T[4]) / det);
			builder.setValue(2, 3, (T[3] * T[5] * T[8] - T[1] * T[7] * T[8] - T[3] * T[4] * T[9] + T[0] * T[7] * T[9]
					+ T[1] * T[4] * T[11] - T[0] * T[5] * T[11])
					/ det);
			builder.setValue(3, 3, (T[1] * T[6] * T[8] - T[2] * T[5] * T[8] + T[2] * T[4] * T[9] - T[0] * T[6] * T[9]
					- T[1] * T[4] * T[10] + T[0] * T[5] * T[10])
					/ det);
		} else {
			builder.setValue(0, 0, (T[6] * T[11] * T[13] - T[7] * T[10] * T[13] + T[7] * T[9] * T[14] - T[5] * T[11]
					* T[14] - T[6] * T[9] * T[15] + T[5] * T[10] * T[15])
					/ det);
			builder.setValue(0, 1, (T[3] * T[10] * T[13] - T[2] * T[11] * T[13] - T[3] * T[9] * T[14] + T[1] * T[11]
					* T[14] + T[2] * T[9] * T[15] - T[1] * T[10] * T[15])
					/ det);
			builder.setValue(0, 2, (T[2] * T[7] * T[13] - T[3] * T[6] * T[13] + T[3] * T[5] * T[14] - T[1] * T[7]
					* T[14] - T[2] * T[5] * T[15] + T[1] * T[6] * T[15])
					/ det);
			builder.setValue(0, 3, (T[3] * T[6] * T[9] - T[2] * T[7] * T[9] - T[3] * T[5] * T[10] + T[1] * T[7] * T[10]
					+ T[2] * T[5] * T[11] - T[1] * T[6] * T[11])
					/ det);
			builder.setValue(1, 0, (T[7] * T[10] * T[12] - T[6] * T[11] * T[12] - T[7] * T[8] * T[14] + T[4] * T[11]
					* T[14] + T[6] * T[8] * T[15] - T[4] * T[10] * T[15])
					/ det);
			builder.setValue(1, 1, (T[2] * T[11] * T[12] - T[3] * T[10] * T[12] + T[3] * T[8] * T[14] - T[0] * T[11]
					* T[14] - T[2] * T[8] * T[15] + T[0] * T[10] * T[15])
					/ det);
			builder.setValue(1, 2, (T[3] * T[6] * T[12] - T[2] * T[7] * T[12] - T[3] * T[4] * T[14] + T[0] * T[7]
					* T[14] + T[2] * T[4] * T[15] - T[0] * T[6] * T[15])
					/ det);
			builder.setValue(1, 3, (T[2] * T[7] * T[8] - T[3] * T[6] * T[8] + T[3] * T[4] * T[10] - T[0] * T[7] * T[10]
					- T[2] * T[4] * T[11] + T[0] * T[6] * T[11])
					/ det);
			builder.setValue(2, 0, (T[5] * T[11] * T[12] - T[7] * T[9] * T[12] + T[7] * T[8] * T[13] - T[4] * T[11]
					* T[13] - T[5] * T[8] * T[15] + T[4] * T[9] * T[15])
					/ det);
			builder.setValue(2, 1, (T[3] * T[9] * T[12] - T[1] * T[11] * T[12] - T[3] * T[8] * T[13] + T[0] * T[11]
					* T[13] + T[1] * T[8] * T[15] - T[0] * T[9] * T[15])
					/ det);
			builder.setValue(2, 2, (T[1] * T[7] * T[12] - T[3] * T[5] * T[12] + T[3] * T[4] * T[13] - T[0] * T[7]
					* T[13] - T[1] * T[4] * T[15] + T[0] * T[5] * T[15])
					/ det);
			builder.setValue(2, 3, (T[3] * T[5] * T[8] - T[1] * T[7] * T[8] - T[3] * T[4] * T[9] + T[0] * T[7] * T[9]
					+ T[1] * T[4] * T[11] - T[0] * T[5] * T[11])
					/ det);
			builder.setValue(3, 0, (T[6] * T[9] * T[12] - T[5] * T[10] * T[12] - T[6] * T[8] * T[13] + T[4] * T[10]
					* T[13] + T[5] * T[8] * T[14] - T[4] * T[9] * T[14])
					/ det);
			builder.setValue(3, 1, (T[1] * T[10] * T[12] - T[2] * T[9] * T[12] + T[2] * T[8] * T[13] - T[0] * T[10]
					* T[13] - T[1] * T[8] * T[14] + T[0] * T[9] * T[14])
					/ det);
			builder.setValue(3, 2, (T[2] * T[5] * T[12] - T[1] * T[6] * T[12] - T[2] * T[4] * T[13] + T[0] * T[6]
					* T[13] + T[1] * T[4] * T[14] - T[0] * T[5] * T[14])
					/ det);
			builder.setValue(3, 3, (T[1] * T[6] * T[8] - T[2] * T[5] * T[8] + T[2] * T[4] * T[9] - T[0] * T[6] * T[9]
					- T[1] * T[4] * T[10] + T[0] * T[5] * T[10])
					/ det);
		}
		return builder.build();
	}

	private static double computeDeterminant(TransformMatrix matrix) {
		double[] T = matrix.getValues();
		if (matrix.isAffine()) {
			return -T[2] * T[5] * T[8] + T[1] * T[6] * T[8] + T[2] * T[4] * T[9] - T[0] * T[6] * T[9] - T[1] * T[4]
					* T[10] + T[0] * T[5] * T[10];
		} else {
			return T[3] * T[6] * T[9] * T[12] - T[2] * T[7] * T[9] * T[12] - T[3] * T[5] * T[10] * T[12] + T[1] * T[7]
					* T[10] * T[12] + T[2] * T[5] * T[11] * T[12] - T[1] * T[6] * T[11] * T[12] - T[3] * T[6] * T[8]
					* T[13] + T[2] * T[7] * T[8] * T[13] + T[3] * T[4] * T[10] * T[13] - T[0] * T[7] * T[10] * T[13]
					- T[2] * T[4] * T[11] * T[13] + T[0] * T[6] * T[11] * T[13] + T[3] * T[5] * T[8] * T[14] - T[1]
					* T[7] * T[8] * T[14] - T[3] * T[4] * T[9] * T[14] + T[0] * T[7] * T[9] * T[14] + T[1] * T[4]
					* T[11] * T[14] - T[0] * T[5] * T[11] * T[14] - T[2] * T[5] * T[8] * T[15] + T[1] * T[6] * T[8]
					* T[15] + T[2] * T[4] * T[9] * T[15] - T[0] * T[6] * T[9] * T[15] - T[1] * T[4] * T[10] * T[15]
					+ T[0] * T[5] * T[10] * T[15];
		}
	}

	@SuppressWarnings("serial")
	public static class MatrixInversionException extends RuntimeException {

		public MatrixInversionException() {
			this("Cannot invert a singular matrix");
		}

		public MatrixInversionException(String message) {
			super(message);
		}

	}

}