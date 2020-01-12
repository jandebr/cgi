package org.maia.cgi.transform.d3;

import java.util.Arrays;

public class TransformMatrixBuilder {

	private double[] values;

	public TransformMatrixBuilder() {
		this.values = new double[16];
	}

	public TransformMatrixBuilder(double[] values) {
		this.values = Arrays.copyOf(values, values.length);
	}

	public TransformMatrixBuilder(TransformMatrix matrix) {
		this(matrix.getValues());
	}

	public TransformMatrix build() {
		return new TransformMatrix(getValues());
	}

	public double getValue(int row, int col) {
		return getValues()[row * 4 + col];
	}

	public TransformMatrixBuilder setValue(int row, int col, double value) {
		getValues()[row * 4 + col] = value;
		return this;
	}

	private double[] getValues() {
		return values;
	}

}
