package org.maia.cgi.render.d2;

import java.awt.Color;

public class ImageMaskFileHandle extends ImageTextureMapFileHandle {

	private Color maskColor;

	public ImageMaskFileHandle(String filePath, Color maskColor) {
		super(filePath);
		this.maskColor = maskColor;
	}

	@Override
	ImageMask resolve() {
		System.out.println("Loading mask '" + getFilePath() + "'");
		return new ImageMask(readImageFromFile(), getMaskColor());
	}

	@Override
	void dispose() {
		System.out.println("Disposing mask '" + getFilePath() + "'");
	}

	public Color getMaskColor() {
		return maskColor;
	}

}
