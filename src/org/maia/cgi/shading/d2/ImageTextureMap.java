package org.maia.cgi.shading.d2;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageTextureMap implements TextureMap {

	private BufferedImage image;

	private float[] hsbComps = new float[3];

	public ImageTextureMap(BufferedImage image) {
		this.image = image;
	}

	@Override
	public double sampleDouble(double x, double y) {
		double value = -1.0;
		Color color = sampleColor(x, y);
		if (color != null) {
			Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComps);
			value = hsbComps[2]; // brightness, between 0 and 1
		}
		return value;
	}

	@Override
	public int sampleInt(double x, double y) {
		int value = -1;
		if (isInsideImage(x, y)) {
			int px = (int) Math.floor(x);
			int py = (int) Math.floor(y);
			value = getImage().getRGB(px, py);
		}
		return value;
	}

	@Override
	public Color sampleColor(double x, double y) {
		Color color = null;
		if (isInsideImage(x, y)) {
			color = new Color(sampleInt(x, y), true);
		}
		return color;
	}

	protected boolean isInsideImage(double x, double y) {
		int px = (int) Math.floor(x);
		int py = (int) Math.floor(y);
		return px >= 0 && py >= 0 && px < getImage().getWidth() && py < getImage().getHeight();
	}

	public BufferedImage getImage() {
		return image;
	}

}