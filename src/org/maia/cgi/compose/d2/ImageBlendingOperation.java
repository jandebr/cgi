package org.maia.cgi.compose.d2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.maia.cgi.compose.Compositing;

public class ImageBlendingOperation {

	private List<File> imageFiles;

	private float[] weights;

	public ImageBlendingOperation(List<File> imageFiles, float[] weights) {
		if (imageFiles.isEmpty() || imageFiles.size() != weights.length)
			throw new IllegalArgumentException();
		this.imageFiles = imageFiles;
		this.weights = weights;
	}

	public BufferedImage apply() throws IOException {
		int width = 0;
		int height = 0;
		float[][][] buffer = null;
		float[] rgbaComps = new float[4];
		float[] weights = getWeights();
		float weightSum = 0f;
		int i = 0;
		for (File file : getImageFiles()) {
			BufferedImage image = ImageIO.read(file);
			if (buffer == null) {
				width = image.getWidth();
				height = image.getHeight();
				buffer = new float[height][width][4];
			}
			// Weigh image data and add to the buffer
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					new Color(image.getRGB(x, y), true).getComponents(rgbaComps);
					for (int k = 0; k < 4; k++) {
						buffer[y][x][k] += weights[i] * rgbaComps[k];
					}
				}
			}
			weightSum += weights[i++];
		}
		// Produce resulting image from buffer
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Compositing.makeImageFullyTransparent(image);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int k = 0; k < 4; k++) {
					// normalize by sum of weights
					buffer[y][x][k] /= weightSum;
				}
				Color color = new Color(buffer[y][x][0], buffer[y][x][1], buffer[y][x][2], buffer[y][x][3]);
				image.setRGB(x, y, color.getRGB());
			}
		}
		return image;
	}

	private List<File> getImageFiles() {
		return imageFiles;
	}

	private float[] getWeights() {
		return weights;
	}

}