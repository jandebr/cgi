package org.maia.cgi.transform.d3;

import java.util.List;
import java.util.Vector;

import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.metrics.Metrics;

/**
 * A 3D transformation matrix.
 *
 * <p>
 * As it works with homogeneous coordinates, the matrix is 4x4.
 * </p>
 * 
 * <p>
 * Instances of this class are immutable. This allows composite transformation matrices to be computed and kept in
 * memory for efficient reuse. To create new or adjusted <code>TransformMatrix</code> instances, it is advised to use
 * <code>TransformMatrixBuilder</code>
 * </p>
 * 
 * @see TransformMatrixBuilder
 */
public class TransformMatrix {

	private double[] values;

	private TransformMatrix inverseMatrix;

	public TransformMatrix(double[] values) {
		if (values == null || values.length != 16)
			throw new IllegalArgumentException("The matrix should have 16 values as its dimensions are 4 by 4");
		this.values = values;
	}

	/**
	 * Multiplies this matrix with another matrix
	 * 
	 * @param matrix
	 *            The second operand
	 * @return The result of this matrix multiplied by the given <code>matrix</code>. In symbols, <code>result</code> =
	 *         <code>T</code> * <code>M</code> , where <code>T</code> denotes <code>this</code> and <code>M</code>
	 *         denotes <code>matrix</code>
	 */
	public TransformMatrix preMultiply(TransformMatrix matrix) {
		Metrics.getInstance().incrementMatrixMultiplications();
		double[] T = getValues();
		double[] M = matrix.getValues();
		// result R = T * M
		double[] R = new double[16];
		// First row
		R[0] = T[0] * M[0] + T[1] * M[4] + T[2] * M[8] + T[3] * M[12];
		R[1] = T[0] * M[1] + T[1] * M[5] + T[2] * M[9] + T[3] * M[13];
		R[2] = T[0] * M[2] + T[1] * M[6] + T[2] * M[10] + T[3] * M[14];
		R[3] = T[0] * M[3] + T[1] * M[7] + T[2] * M[11] + T[3] * M[15];
		// Second row
		R[4] = T[4] * M[0] + T[5] * M[4] + T[6] * M[8] + T[7] * M[12];
		R[5] = T[4] * M[1] + T[5] * M[5] + T[6] * M[9] + T[7] * M[13];
		R[6] = T[4] * M[2] + T[5] * M[6] + T[6] * M[10] + T[7] * M[14];
		R[7] = T[4] * M[3] + T[5] * M[7] + T[6] * M[11] + T[7] * M[15];
		// Third row
		R[8] = T[8] * M[0] + T[9] * M[4] + T[10] * M[8] + T[11] * M[12];
		R[9] = T[8] * M[1] + T[9] * M[5] + T[10] * M[9] + T[11] * M[13];
		R[10] = T[8] * M[2] + T[9] * M[6] + T[10] * M[10] + T[11] * M[14];
		R[11] = T[8] * M[3] + T[9] * M[7] + T[10] * M[11] + T[11] * M[15];
		// Fourth row
		R[12] = T[12] * M[0] + T[13] * M[4] + T[14] * M[8] + T[15] * M[12];
		R[13] = T[12] * M[1] + T[13] * M[5] + T[14] * M[9] + T[15] * M[13];
		R[14] = T[12] * M[2] + T[13] * M[6] + T[14] * M[10] + T[15] * M[14];
		R[15] = T[12] * M[3] + T[13] * M[7] + T[14] * M[11] + T[15] * M[15];
		return new TransformMatrix(R);
	}

	/**
	 * Multiplies another matrix with this matrix
	 * 
	 * @param matrix
	 *            The first operand
	 * @return The result of the given <code>matrix</code> multiplied by this matrix. In symbols, <code>result</code> =
	 *         <code>M</code> * <code>T</code> , where <code>T</code> denotes <code>this</code> and <code>M</code>
	 *         denotes <code>matrix</code>
	 */
	public TransformMatrix postMultiply(TransformMatrix matrix) {
		return matrix.preMultiply(this);
	}

	/**
	 * Transforms the given point
	 * 
	 * @param point
	 *            A point in 3D
	 * @return The transformation of the <code>point</code> under this matrix. In symbols, <code>result</code> =
	 *         <code>T</code> * <code>p</code> , where <code>T</code> denotes <code>this</code> and <code>p</code>
	 *         denotes <code>point</code>
	 */
	public Point3D transform(Point3D point) {
		Metrics.getInstance().incrementPointTransformations();
		double[] T = this.getValues();
		double px = point.getX();
		double py = point.getY();
		double pz = point.getZ();
		double pw = point.getW();
		double tx = T[0] * px + T[1] * py + T[2] * pz + T[3] * pw;
		double ty = T[4] * px + T[5] * py + T[6] * pz + T[7] * pw;
		double tz = T[8] * px + T[9] * py + T[10] * pz + T[11] * pw;
		double tw = T[12] * px + T[13] * py + T[14] * pz + T[15] * pw;
		return new Point3D(tx, ty, tz, tw);
	}

	public List<Point3D> transform(List<Point3D> points) {
		List<Point3D> tPoints = new Vector<Point3D>(points.size());
		for (Point3D point : points) {
			tPoints.add(transform(point));
		}
		return tPoints;
	}

	public boolean isAffine() {
		double[] T = getValues();
		return T[12] == 0 && T[13] == 0 && T[14] == 0 && T[15] == 1.0;
	}

	@Override
	public String toString() {
		String open = "┌││└";
		String close = "┐││┘";
		StringBuilder sb = new StringBuilder(256);
		for (int i = 0; i < 4; i++) {
			sb.append(open.charAt(i));
			sb.append(' ');
			for (int j = 0; j < 4; j++) {
				double v = getValue(i, j);
				sb.append(String.format("%10.3f", v));
			}
			sb.append(' ');
			sb.append(close.charAt(i));
			sb.append('\n');
		}
		return sb.toString();
	}

	protected double getValue(int row, int col) {
		return getValues()[row * 4 + col];
	}

	protected double[] getValues() {
		return values;
	}

	TransformMatrix getInverseMatrix() {
		return inverseMatrix;
	}

	void setInverseMatrix(TransformMatrix inverseMatrix) {
		this.inverseMatrix = inverseMatrix;
	}

}