package org.maia.cgi.compose.d2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.maia.cgi.compose.Compositing;

public class ConvolutionMatrix {

	private int rows;

	private int columns;

	private double[][] values;

	public ConvolutionMatrix(double[][] values) {
		this.values = values;
		this.rows = values.length;
		this.columns = values[0].length;
	}

	@Override
	public String toString() {
		String open = "┌│└";
		String close = "�?│┘";
		StringBuilder sb = new StringBuilder(256);
		for (int i = 0; i < getRows(); i++) {
			sb.append(open.charAt(i == 0 ? 0 : (i == getRows() - 1 ? 2 : 1)));
			sb.append(' ');
			for (int j = 0; j < getColumns(); j++) {
				double v = getValue(i, j);
				sb.append(String.format("%10.3f", v));
			}
			sb.append(' ');
			sb.append(close.charAt(i == 0 ? 0 : (i == getRows() - 1 ? 2 : 1)));
			sb.append('\n');
		}
		return sb.toString();
	}

	@Override
	public ConvolutionMatrix clone() {
		double[][] values = new double[getRows()][];
		for (int i = 0; i < getRows(); i++) {
			values[i] = Arrays.copyOf(getValues()[i], getColumns());
		}
		return new ConvolutionMatrix(values);
	}

	public Color convoluteImageAtPixel(BufferedImage image, int x0, int y0) {
		return convoluteImageAtPixel(image, x0, y0, ConvolutionMask.ALL_INCLUSIVE);
	}

	public Color convoluteImageAtPixel(BufferedImage image, int x0, int y0, ConvolutionMask mask) {
		Color color = null;
		float[] rgbaCompsAvg = new float[4]; // weighted average
		float[] rgbaComps = new float[4];
		double valuesSum = 0;
		int width = image.getWidth();
		int height = image.getHeight();
		for (int i = 0; i < getRows(); i++) {
			int y = y0 + i;
			if (y >= 0 && y < height) {
				for (int j = 0; j < getColumns(); j++) {
					int x = x0 + j;
					if (x >= 0 && x < width) {
						if (!mask.isMasked(i, j)) {
							double value = getValue(i, j);
							Color icolor = new Color(image.getRGB(x, y), true);
							icolor.getRGBComponents(rgbaComps);
							for (int k = 0; k < rgbaComps.length; k++) {
								rgbaCompsAvg[k] += value * rgbaComps[k];
							}
							valuesSum += value;
						}
					}
				}
			}
		}
		if (valuesSum != 0) {
			for (int k = 0; k < rgbaCompsAvg.length; k++) {
				rgbaCompsAvg[k] /= valuesSum;
			}
			color = new Color(rgbaCompsAvg[0], rgbaCompsAvg[1], rgbaCompsAvg[2], rgbaCompsAvg[3]);
		}
		return color;
	}

	public BufferedImage convoluteImage(BufferedImage image) {
		return convoluteImage(image, ConvolutionMask.ALL_INCLUSIVE);
	}

	public BufferedImage convoluteImage(BufferedImage image, ConvolutionMask mask) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage cimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Compositing.makeImageFullyTransparent(cimg);
		int yOffset = -(getRows() - 1) / 2;
		int xOffset = -(getColumns() - 1) / 2;
		for (int y = 0; y < height; y++) {
			int y0 = y + yOffset;
			for (int x = 0; x < width; x++) {
				int x0 = x + xOffset;
				cimg.setRGB(x, y, convoluteImageAtPixel(image, x0, y0, mask).getRGB());
			}
		}
		return cimg;
	}

	protected double getValue(int row, int col) {
		return getValues()[row][col];
	}

	protected double[][] getValues() {
		return values;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

}