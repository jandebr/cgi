package org.maia.cgi.compose.d2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.maia.cgi.compose.Compositing;

public class ImageTransparencyOperation {

	private BufferedImage sourceImage;

	private double transparencyMultiplier;

	public ImageTransparencyOperation(BufferedImage sourceImage, double transparencyMultiplier) {
		this.sourceImage = sourceImage;
		this.transparencyMultiplier = transparencyMultiplier;
	}

	public BufferedImage apply() throws IOException {
		BufferedImage source = getSourceImage();
		int width = source.getWidth();
		int height = source.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color color = new Color(source.getRGB(x, y), true);
				double transparency = Compositing.getTransparency(color);
				transparency = 1.0 - (1.0 - transparency) * (1.0 - getTransparencyMultiplier());
				Color newColor = Compositing.setTransparency(color, transparency);
				image.setRGB(x, y, newColor.getRGB());
			}
		}
		return image;
	}

	private BufferedImage getSourceImage() {
		return sourceImage;
	}

	private double getTransparencyMultiplier() {
		return transparencyMultiplier;
	}

}