package org.maia.cgi.shading.d2;

import java.awt.image.BufferedImage;

import org.maia.cgi.compose.Compositing;

public class ImageTextureMapFileHandle extends TextureMapHandle {

	private String filePath;

	public ImageTextureMapFileHandle(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getFilePath().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageTextureMapFileHandle other = (ImageTextureMapFileHandle) obj;
		return getFilePath().equals(other.getFilePath());
	}

	@Override
	TextureMap resolve() {
		System.out.println("Loading texture map '" + getFilePath() + "'");
		return new ImageTextureMap(readImageFromFile());
	}

	@Override
	void dispose() {
		System.out.println("Disposing texture map '" + getFilePath() + "'");
	}

	protected BufferedImage readImageFromFile() {
		return Compositing.readImageFromFile(getFilePath());
	}

	public String getFilePath() {
		return filePath;
	}

}
