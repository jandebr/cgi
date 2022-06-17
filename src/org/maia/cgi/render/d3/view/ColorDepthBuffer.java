package org.maia.cgi.render.d3.view;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.maia.cgi.compose.Compositing;
import org.maia.cgi.compose.d2.ConvolutionMask;
import org.maia.cgi.compose.d2.ConvolutionMatrix;

public class ColorDepthBuffer {

	private BufferedImage image;

	private DepthBuffer depthBuffer;

	public ColorDepthBuffer(BufferedImage image) {
		setImage(image);
		setDepthBuffer(new DepthBuffer(image.getWidth(), image.getHeight()));
	}

	public ColorDepthBuffer(BufferedImage image, double depth) {
		this(image);
		getDepthBuffer().clearDepth(depth);
	}

	public ColorDepthBuffer(int width, int height, Color ambientColor) {
		this(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
		if (ambientColor != null) {
			Compositing.fillImageWithSolidColor(getImage(), ambientColor);
		} else {
			Compositing.makeImageFullyTransparent(getImage());
		}
	}

	public void addLayerWithTransparency(BufferedImage image, double depth) {
		addLayerWithTransparency(image, 0, 0, depth);
	}

	public void addLayerWithTransparency(BufferedImage layerImage, int x0, int y0, double depth) {
		int width = layerImage.getWidth();
		int height = layerImage.getHeight();
		for (int i = 0; i < height; i++) {
			int y = y0 + i;
			if (y >= 0 && y < getHeight()) {
				for (int j = 0; j < width; j++) {
					int x = x0 + j;
					if (x >= 0 && x < getWidth()) {
						int rgb = layerImage.getRGB(j, i);
						int alpha = (rgb & 0xff000000) >> 24;
						if (alpha != 0) { // not fully transparent
							double depth0 = getDepth(x, y);
							Color color0 = getColor(x, y);
							if (alpha == 255 && depth <= depth0) {
								setRGB(x, y, rgb);
								setDepth(x, y, depth);
							} else if (depth <= depth0 || !Compositing.isFullyOpaque(color0)) {
								Color color = new Color(rgb, true);
								Color frontColor = depth <= depth0 ? color : color0;
								Color backColor = depth <= depth0 ? color0 : color;
								rgb = Compositing.combineColorsByTransparency(frontColor, backColor).getRGB();
								setRGB(x, y, rgb);
								if (depth <= depth0)
									setDepth(x, y, depth);
							}
						}
					}
				}
			}
		}
	}

	public void setRGB(int x, int y, int rgb) {
		getImage().setRGB(x, y, rgb);
	}

	public void setColor(int x, int y, Color color) {
		setRGB(x, y, color.getRGB());
	}

	public void setDepth(int x, int y, double depth) {
		getDepthBuffer().setDepth(x, y, depth);
	}

	public void setColorAndDepth(int x, int y, Color color, double depth) {
		setColor(x, y, color);
		setDepth(x, y, depth);
	}

	public int getRGB(int x, int y) {
		return getImage().getRGB(x, y);
	}

	public Color getColor(int x, int y) {
		return new Color(getRGB(x, y), true);
	}

	public double getDepth(int x, int y) {
		return getDepthBuffer().getDepth(x, y);
	}

	public double getMinimumDepth() {
		return getDepthBuffer().getMinimumDepth();
	}

	public double getMaximiumDepth() {
		return getDepthBuffer().getMaximumDepth();
	}

	public int getWidth() {
		return getDepthBuffer().getWidth();
	}

	public int getHeight() {
		return getDepthBuffer().getHeight();
	}

	public Color convoluteColor(int x, int y, ConvolutionMatrix matrix) {
		return matrix.convoluteImageAtPixel(getImage(), x, y);
	}

	public Color convoluteColor(int x, int y, ConvolutionMatrix matrix, ConvolutionMask mask) {
		return matrix.convoluteImageAtPixel(getImage(), x, y, mask);
	}

	public void replaceImage(BufferedImage newImage) {
		setImage(newImage);
	}

	public BufferedImage getImage() {
		return image;
	}

	private void setImage(BufferedImage image) {
		this.image = image;
	}

	private DepthBuffer getDepthBuffer() {
		return depthBuffer;
	}

	private void setDepthBuffer(DepthBuffer depthBuffer) {
		this.depthBuffer = depthBuffer;
	}

	private static class DepthBuffer {

		private int width;

		private int height;

		private double[][] depths;

		private double minimumDepth;

		private double maximumDepth;

		private boolean empty;

		public DepthBuffer(int width, int height) {
			this.width = width;
			this.height = height;
			this.depths = new double[height][width];
			this.empty = true;
		}

		public void clearDepth(double depth) {
			for (int y = 0; y < getHeight(); y++) {
				Arrays.fill(getDepths()[y], depth);
			}
			setEmpty(false);
			setMinimumDepth(depth);
			setMaximumDepth(depth);
		}

		public void setDepth(int x, int y, double depth) {
			getDepths()[y][x] = depth;
			if (isEmpty()) {
				setEmpty(false);
				setMinimumDepth(depth);
				setMaximumDepth(depth);
			} else {
				setMinimumDepth(Math.min(getMinimumDepth(), depth));
				setMaximumDepth(Math.max(getMaximumDepth(), depth));
			}
		}

		public double getDepth(int x, int y) {
			return getDepths()[y][x];
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		private double[][] getDepths() {
			return depths;
		}

		public double getMinimumDepth() {
			return minimumDepth;
		}

		private void setMinimumDepth(double minimumDepth) {
			this.minimumDepth = minimumDepth;
		}

		public double getMaximumDepth() {
			return maximumDepth;
		}

		private void setMaximumDepth(double maximumDepth) {
			this.maximumDepth = maximumDepth;
		}

		private boolean isEmpty() {
			return empty;
		}

		private void setEmpty(boolean empty) {
			this.empty = empty;
		}

	}

}
