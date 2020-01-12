package org.maia.cgi.gui.d3.renderer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.Scrollable;

import org.maia.cgi.compose.Compositing;
import org.maia.cgi.geometry.d2.Rectangle2D;
import org.maia.cgi.render.d3.view.GraphicsViewPortImpl;
import org.maia.cgi.render.d3.view.ViewPort;

@SuppressWarnings("serial")
public class RenderPane extends JLabel implements Scrollable {

	private ViewPort outputViewPort;

	private BufferedImage outputImage;

	public RenderPane() {
		this(RenderOptions.createDefaultOptions());
	}

	public RenderPane(RenderOptions renderOptions) {
		updateRenderOptions(renderOptions);
	}

	public void updateRenderOptions(RenderOptions renderOptions) {
		setBackground(renderOptions.getSceneBackgroundColor());
		changeRenderSize(renderOptions.getRenderWidth(), renderOptions.getRenderHeight());
	}

	public void changeRenderSize(int width, int height) {
		Dimension size = new Dimension(width, height);
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		setSize(size);
		invalidateOutput();
	}

	private void invalidateOutput() {
		this.outputViewPort = null;
		this.outputImage = null;
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 4;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 16;
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(getOutputImage(), 0, 0, null);
	}

	public ViewPort getOutputViewPort() {
		if (outputViewPort == null) {
			outputViewPort = createOutputImageViewPort();
		}
		return outputViewPort;
	}

	protected ViewPort createOutputImageViewPort() {
		Graphics2D graphics2D = getOutputImage().createGraphics();
		graphics2D.setBackground(getBackground());
		Rectangle2D windowBounds = new Rectangle2D(getWidth(), getHeight());
		return new GraphicsViewPortImpl(graphics2D, windowBounds);
	}

	public BufferedImage getOutputImage() {
		if (outputImage == null) {
			outputImage = createOutputImage();
		}
		return outputImage;
	}

	protected BufferedImage createOutputImage() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		Compositing.makeImageFullyTransparent(image);
		return image;
	}

}
