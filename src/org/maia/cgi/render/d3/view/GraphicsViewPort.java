package org.maia.cgi.render.d3.view;

import java.awt.Graphics2D;

import org.maia.cgi.geometry.d2.Rectangle2D;

public abstract class GraphicsViewPort implements ViewPort {

	private static final Rectangle2D CANONICAL_VIEW_BOUNDS = new Rectangle2D(-1.0, 1.0, -1.0, 1.0);

	/**
	 * Graphics2D draw operations work in integer coordinates, so we need to scale the view coordinates (typically
	 * between -1 and +1) by a large amount before drawing.
	 * <p>
	 * The transformation which is set on the canvas will take care of reverting back this integer scaling.
	 * </p>
	 */
	private static final double INTEGER_SCALE = 10000.0;

	private Rectangle2D viewBounds;

	private Rectangle2D windowBounds;

	private Graphics2D windowGraphics2D; // in window or device coordinates

	private Graphics2D viewGraphics2D; // in view coordinates

	protected GraphicsViewPort(Graphics2D windowGraphics2D, Rectangle2D windowBounds) {
		this(windowGraphics2D, CANONICAL_VIEW_BOUNDS, windowBounds);
	}

	protected GraphicsViewPort(Graphics2D windowGraphics2D, Rectangle2D viewBounds, Rectangle2D windowBounds) {
		this.windowGraphics2D = windowGraphics2D;
		this.viewBounds = viewBounds;
		this.windowBounds = windowBounds;
		this.viewGraphics2D = createViewGraphics2D();
	}

	private Graphics2D createViewGraphics2D() {
		Graphics2D g = (Graphics2D) getWindowGraphics2D().create();
		Rectangle2D view = getViewBounds();
		Rectangle2D window = getWindowBounds();
		double sx = window.getWidth() / view.getWidth();
		double sy = window.getHeight() / view.getHeight();
		// Transforms go in reverse order on Graphics2D
		g.translate(window.getLeft(), window.getTop());
		g.scale(sx, -sy);
		g.translate(-view.getLeft(), -view.getBottom());
		g.scale(1.0 / INTEGER_SCALE, 1.0 / INTEGER_SCALE);
		return g;
	}

	@Override
	public void startRendering() {
		// Nothing, but subclasses could override
	}

	@Override
	public void stopRendering() {
		// Nothing, but subclasses could override
	}

	@Override
	public void clear() {
		int width = (int) Math.floor(getWindowBounds().getWidth());
		int height = (int) Math.floor(getWindowBounds().getHeight());
		getWindowGraphics2D().clearRect(0, 0, width, height);
	}

	protected int toViewGraphicsCoordinate(double viewCoordinate) {
		return (int) Math.floor(INTEGER_SCALE * viewCoordinate);
	}

	protected Rectangle2D getViewBounds() {
		return viewBounds;
	}

	protected Rectangle2D getWindowBounds() {
		return windowBounds;
	}

	protected Graphics2D getWindowGraphics2D() {
		return windowGraphics2D;
	}

	protected Graphics2D getViewGraphics2D() {
		return viewGraphics2D;
	}

}