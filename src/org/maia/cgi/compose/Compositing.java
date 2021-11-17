package org.maia.cgi.compose;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.maia.cgi.compose.d2.ImageBlendingOperation;
import org.maia.cgi.compose.d3.DepthBlurOperation;
import org.maia.cgi.compose.d3.DepthBlurOperation.DepthBlurOperationProgressTracker;
import org.maia.cgi.compose.d3.DepthBlurParameters;
import org.maia.cgi.compose.d3.DepthFunction;
import org.maia.cgi.compose.d3.SigmoidDepthFunction;
import org.maia.cgi.render.d3.view.ColorDepthBuffer;

public class Compositing {

	private static float[] rgbaComps = new float[4];

	private static float[] hsbComps = new float[3];

	public static Color adjustBrightness(Color color, double factor) {
		if (factor == 0)
			return color;
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComps);
		double brightness = hsbComps[2];
		double darkness = 1.0 - brightness;
		if (factor >= 0) {
			// increase brightness
			brightness = 1.0 - darkness * (1.0 - factor);
		} else {
			// increase darkness
			darkness = 1.0 - brightness * (1.0 + factor);
			brightness = 1.0 - darkness;
		}
		int rgba = (color.getAlpha() << 24)
				| (Color.HSBtoRGB(hsbComps[0], hsbComps[1] * (float) (Math.min(1.0, 1.0 - factor)), (float) brightness) & 0x00ffffff);
		return new Color(rgba, true);
	}

	public static Color adjustSaturation(Color color, double factor) {
		if (factor == 0)
			return color;
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComps);
		double saturation = hsbComps[1];
		double grayness = 1.0 - saturation;
		if (factor >= 0) {
			// increase saturation
			saturation = 1.0 - grayness * (1.0 - factor);
		} else {
			// increase grayness
			grayness = 1.0 - saturation * (1.0 + factor);
			saturation = 1.0 - grayness;
		}
		int rgba = (color.getAlpha() << 24)
				| (Color.HSBtoRGB(hsbComps[0], (float) saturation, hsbComps[2]) & 0x00ffffff);
		return new Color(rgba, true);
	}

	public static double getTransparency(Color color) {
		return 1.0 - color.getAlpha() / 255.0;
	}

	public static Color setTransparency(Color color, double transparency) {
		color.getRGBColorComponents(rgbaComps);
		rgbaComps[3] = (float) (1.0 - transparency);
		return new Color(rgbaComps[0], rgbaComps[1], rgbaComps[2], rgbaComps[3]);
	}

	public static Color combineColorsByTransparency(List<Color> colors) {
		Color color = null;
		if (colors.size() == 1) {
			color = colors.get(0);
		} else if (colors.size() > 1) {
			Color c0 = colors.get(0);
			Color c1 = combineColorsByTransparency(colors.subList(1, colors.size()));
			color = combineColorsByTransparency(c0, c1);
		}
		return color;
	}

	public static Color combineColorsByTransparency(Color frontColor, Color backColor) {
		if (frontColor.getAlpha() == 255)
			return frontColor;
		double alpha = frontColor.getAlpha() / 255.0;
		double beta = 1.0 - alpha;
		double gamma = backColor.getAlpha() / 255.0;
		int red = (int) Math.floor(alpha * frontColor.getRed() + beta * backColor.getRed());
		int green = (int) Math.floor(alpha * frontColor.getGreen() + beta * backColor.getGreen());
		int blue = (int) Math.floor(alpha * frontColor.getBlue() + beta * backColor.getBlue());
		int al = (int) Math.floor(255.0 * (alpha + beta * gamma));
		return new Color(red, green, blue, al);
	}

	public static Color interpolateColors(Color from, Color to, double ratio) {
		double rev = 1.0 - ratio;
		int alpha = (int) Math.floor(rev * from.getAlpha() + ratio * to.getAlpha());
		int red = (int) Math.floor(rev * from.getRed() + ratio * to.getRed());
		int green = (int) Math.floor(rev * from.getGreen() + ratio * to.getGreen());
		int blue = (int) Math.floor(rev * from.getBlue() + ratio * to.getBlue());
		return new Color(red, green, blue, alpha);
	}

	public static BufferedImage readImageFromFile(String filePath) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(filePath));
		} catch (IOException e) {
			System.err.println("Failed to read image from path '" + filePath + "'");
			e.printStackTrace();
		}
		return image;
	}

	public static BufferedImage readImageFromStream(InputStream stream) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(stream);
			stream.close();
		} catch (IOException e) {
			System.err.println("Failed to read image from stream");
			e.printStackTrace();
		}
		return image;
	}

	public static void writeImageToFile(BufferedImage image, String filePath) {
		String format = "png";
		int i = filePath.lastIndexOf('.');
		if (i > 0) {
			format = filePath.substring(i + 1).toLowerCase();
		}
		try {
			ImageIO.write(image, format, new File(filePath));
		} catch (IOException e) {
			System.err.println("Failed to write image to path '" + filePath + "'");
			e.printStackTrace();
		}
	}

	public static BufferedImage scaleImage(BufferedImage image, double scale) {
		return scaleImage(image, scale, scale);
	}

	public static BufferedImage scaleImage(BufferedImage image, double sx, double sy) {
		int sw = (int) Math.floor(image.getWidth() * sx);
		int sh = (int) Math.floor(image.getHeight() * sy);
		BufferedImage scaledImage = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = scaledImage.createGraphics();
		AffineTransform at = AffineTransform.getScaleInstance(sx, sy);
		g2.drawRenderedImage(image, at);
		return scaledImage;
	}

	public static void fillImageWithSolidColor(BufferedImage image, Color color) {
		Graphics2D graphics2D = image.createGraphics();
		graphics2D.setBackground(color);
		graphics2D.clearRect(0, 0, image.getWidth(), image.getHeight());
		graphics2D.dispose();
	}

	public static void makeImageFullyTransparent(BufferedImage image) {
		fillImageWithSolidColor(image, new Color(0x00000000, true));
	}

	/**
	 * Blurs an image according to its depth layer and a set of blur parameters
	 * 
	 * @param buffer
	 *            The image buffer holding a depth layer
	 * @param params
	 *            A set of parameters that control the blurring operation
	 * @param tracker
	 *            A progress tracker for this operation, or <code>null</code>
	 * @return A new image that is a blurred derivative from the image in <code>buffer</code>
	 */
	public static BufferedImage blurImageByDepth(ColorDepthBuffer buffer, DepthBlurParameters params,
			DepthBlurOperationProgressTracker tracker) {
		DepthFunction ft = SigmoidDepthFunction.createFilter(buffer.getMinimumDepth(), buffer.getMaximiumDepth(),
				params.getRelativeInflectionDepth(), params.getSmoothness());
		return new DepthBlurOperation(buffer, ft, (int) Math.round(params.getMaxBlurPixelRadius()),
				params.getMaxRelativeDepthSimilarity()).apply(tracker);
	}

	public static BufferedImage blendImagesInDecay(List<File> imageFiles, float decay) {
		int n = imageFiles.size();
		float[] weights = new float[n];
		for (int i = 0; i < n; i++) {
			weights[i] = n - decay * i;
		}
		return blendImages(imageFiles, weights);
	}

	public static BufferedImage blendImages(List<File> imageFiles, float[] weights) {
		BufferedImage image = null;
		try {
			image = new ImageBlendingOperation(imageFiles, weights).apply();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

}