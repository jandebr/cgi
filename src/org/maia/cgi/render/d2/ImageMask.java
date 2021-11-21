package org.maia.cgi.render.d2;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageMask extends ImageTextureMap implements Mask {

	private Color maskColor;

	public ImageMask(BufferedImage image, Color maskColor) {
		super(image);
		this.maskColor = maskColor;
	}

	@Override
	public boolean isMasked(double x, double y) {
		Color c = sampleColor(x, y);
		if (c == null) {
			return true;
		} else {
			return c.equals(getMaskColor());
		}
	}

	public Color getMaskColor() {
		return maskColor;
	}

}
