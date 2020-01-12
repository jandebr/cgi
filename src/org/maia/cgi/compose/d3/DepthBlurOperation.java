package org.maia.cgi.compose.d3;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.maia.cgi.compose.Compositing;
import org.maia.cgi.compose.d2.Convolution;
import org.maia.cgi.compose.d2.ConvolutionMask;
import org.maia.cgi.compose.d2.ConvolutionMatrix;
import org.maia.cgi.render.d3.view.ColorDepthBuffer;

public class DepthBlurOperation {

	private ColorDepthBuffer buffer;

	private DepthFunction blurFunction;

	private int maxBlurRadius;

	private double maxRelativeDepthSimilarity;

	private int x, y, blurRadius;

	private double depth;

	private Map<Integer, ConvolutionMatrix> blurMatrices; // key = blur radius (1,2,3,...)

	/**
	 * Creates a new operation that blurs an image according to its depth layer
	 * 
	 * @param buffer
	 *            The image buffer holding a depth layer
	 * @param blurFunction
	 *            A function that evaluates for any given depth value the relative (i.e., between 0 and 1) level of
	 *            blurring that is to be applied
	 * @param maxBlurRadius
	 *            The pixel radius used for blurring one image pixel at the maximum level of blurring
	 * @param maxRelativeDepthSimilarity
	 *            The maximum relative (i.e., between 0 and 1) difference in depth for nearby pixels to be considered
	 *            part of the same surface and hence blurrable. This threshold is an effective measure to retain
	 *            sharpness of edges on nearby objects which are partly covering far objects from sight
	 */
	public DepthBlurOperation(ColorDepthBuffer buffer, DepthFunction blurFunction, int maxBlurRadius,
			double maxRelativeDepthSimilarity) {
		this.buffer = buffer;
		this.blurFunction = blurFunction;
		this.maxBlurRadius = maxBlurRadius;
		this.maxRelativeDepthSimilarity = maxRelativeDepthSimilarity;
		this.blurMatrices = new HashMap<Integer, ConvolutionMatrix>();
	}

	public BufferedImage apply() {
		return apply(null);
	}

	public BufferedImage apply(DepthBlurOperationProgressTracker tracker) {
		DepthFunction ft = getBlurFunction();
		ColorDepthBuffer buffer = getBuffer();
		int width = buffer.getWidth();
		int height = buffer.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Compositing.makeImageFullyTransparent(image);
		ConvolutionMask cmask = new NeighbouringDepthSelector(getMaxRelativeDepthSimilarity()
				* (buffer.getMaximiumDepth() - buffer.getMinimumDepth()));
		if (tracker != null)
			tracker.operationStarted();
		for (y = 0; y < height; y++) {
			for (x = 0; x < width; x++) {
				depth = buffer.getDepth(x, y);
				if (depth > 0) {
					double blur = ft.eval(depth);
					blurRadius = (int) Math.floor(blur * maxBlurRadius);
					if (blurRadius <= 0) {
						image.setRGB(x, y, buffer.getRGB(x, y));
					} else {
						Color blurredColor = buffer.convoluteColor(x - blurRadius, y - blurRadius,
								getBlurMatrix(blurRadius), cmask);
						image.setRGB(x, y, blurredColor.getRGB());
					}
				}
			}
			if (tracker != null)
				tracker.operationUpdate((y + 1) / (double) height);
		}
		if (tracker != null)
			tracker.operationCompleted();
		return image;
	}

	private ConvolutionMatrix getBlurMatrix(int blurRadius) {
		ConvolutionMatrix matrix = blurMatrices.get(blurRadius);
		if (matrix == null) {
			int dim = 1 + 2 * blurRadius;
			matrix = Convolution.getScaledGaussianBlurMatrix(dim, 4.0);
			blurMatrices.put(blurRadius, matrix); // cache for reuse
		}
		return matrix;
	}

	private ColorDepthBuffer getBuffer() {
		return buffer;
	}

	private DepthFunction getBlurFunction() {
		return blurFunction;
	}

	private int getMaxBlurRadius() {
		return maxBlurRadius;
	}

	private double getMaxRelativeDepthSimilarity() {
		return maxRelativeDepthSimilarity;
	}

	private class NeighbouringDepthSelector implements ConvolutionMask {

		private double maxDepthSimilarity;

		public NeighbouringDepthSelector(double maxDepthSimilarity) {
			this.maxDepthSimilarity = maxDepthSimilarity;
		}

		@Override
		public boolean isMasked(int row, int col) {
			DepthBlurOperation op = DepthBlurOperation.this;
			int yi = op.y - op.blurRadius + row;
			int xi = op.x - op.blurRadius + col;
			double di = op.buffer.getDepth(xi, yi);
			if (di == Double.MAX_VALUE || di <= 0) {
				return false; // blur with the ambient background
			} else {
				return Math.abs(op.depth - di) > maxDepthSimilarity;
			}
		}

	}

	public static interface DepthBlurOperationProgressTracker {

		void operationStarted();

		void operationUpdate(double progress);

		void operationCompleted();

	}

}