package org.maia.cgi.compose.d2;

public class ConvolutionMatrixBuilder {

	private int rows;

	private int columns;

	private double[][] values;

	public ConvolutionMatrixBuilder(int dim) {
		this(dim, dim);
	}

	public ConvolutionMatrixBuilder(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		this.values = new double[rows][columns];
	}

	public ConvolutionMatrixBuilder(ConvolutionMatrix matrix) {
		this.rows = matrix.getRows();
		this.columns = matrix.getColumns();
		this.values = matrix.clone().getValues();
	}

	public ConvolutionMatrix build() {
		return new ConvolutionMatrix(getValues());
	}

	public ConvolutionMatrixBuilder normalize() {
		double sum = getSumOfValues();
		if (sum != 0) {
			for (int i = 0; i < getRows(); i++) {
				for (int j = 0; j < getColumns(); j++) {
					setValue(i, j, getValue(i, j) / sum);
				}
			}
		}
		return this;
	}

	public double getSumOfValues() {
		double sum = 0;
		for (int i = 0; i < getRows(); i++) {
			for (int j = 0; j < getColumns(); j++) {
				sum += getValue(i, j);
			}
		}
		return sum;
	}

	public double getMinValue() {
		double min = getValue(0, 0);
		for (int i = 0; i < getRows(); i++) {
			for (int j = 0; j < getColumns(); j++) {
				min = Math.min(min, getValue(i, j));
			}
		}
		return min;
	}

	public double getMaxValue() {
		double max = getValue(0, 0);
		for (int i = 0; i < getRows(); i++) {
			for (int j = 0; j < getColumns(); j++) {
				max = Math.max(max, getValue(i, j));
			}
		}
		return max;
	}

	public double getValue(int row, int col) {
		return getValues()[row][col];
	}

	public ConvolutionMatrixBuilder setValue(int row, int col, double value) {
		getValues()[row][col] = value;
		return this;
	}

	private double[][] getValues() {
		return values;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

}
