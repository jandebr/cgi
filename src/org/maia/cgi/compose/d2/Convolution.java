package org.maia.cgi.compose.d2;

public class Convolution {

	public static ConvolutionMatrix getGaussianBlurMatrix(int dim) {
		return getGaussianBlurMatrix(dim, dim);
	}

	public static ConvolutionMatrix getGaussianBlurMatrix(int rows, int columns) {
		ConvolutionMatrixBuilder builder = new ConvolutionMatrixBuilder(rows, columns);
		double extent = 2.0;
		double dy = (2 * extent) / (rows - 1);
		double dx = (2 * extent) / (columns - 1);
		double gs = 1.0 / Math.sqrt(2 * Math.PI);
		for (int i = 0; i < rows; i++) {
			double gxi = i * dy - extent;
			double gyi = gs * Math.exp(-0.5 * gxi * gxi);
			for (int j = 0; j < columns; j++) {
				double gxj = j * dx - extent;
				double gyj = gs * Math.exp(-0.5 * gxj * gxj);
				builder.setValue(i, j, gyi * gyj);
			}
		}
		return builder.normalize().build();
	}

	public static ConvolutionMatrix getScaledGaussianBlurMatrix(int dim, double maxToMinRatio) {
		return getScaledGaussianBlurMatrix(dim, dim, maxToMinRatio);
	}

	public static ConvolutionMatrix getScaledGaussianBlurMatrix(int rows, int columns, double maxToMinRatio) {
		ConvolutionMatrixBuilder builder = new ConvolutionMatrixBuilder(getGaussianBlurMatrix(rows, columns));
		double min = builder.getMinValue();
		double max = builder.getMaxValue();
		if (max > min) {
			double min2 = max / maxToMinRatio;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {
					double v = builder.getValue(i, j);
					double v2 = min2 + (v - min) / (max - min) * (max - min2);
					builder.setValue(i, j, v2);
				}
			}
		}
		return builder.normalize().build();
	}

}