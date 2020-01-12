package org.maia.cgi.render.d3.view;

import java.awt.Color;
import java.awt.Graphics2D;

import org.maia.cgi.geometry.d2.Rectangle2D;

public class GraphicsViewPortImpl extends GraphicsViewPort {

	public GraphicsViewPortImpl(Graphics2D windowGraphics2D, Rectangle2D windowBounds) {
		super(windowGraphics2D, windowBounds);
	}

	public GraphicsViewPortImpl(Graphics2D windowGraphics2D, Rectangle2D viewBounds, Rectangle2D windowBounds) {
		super(windowGraphics2D, viewBounds, windowBounds);
	}

	@Override
	public void drawLineInViewCoordinates(double x1, double y1, double depth1, Color color1, double x2, double y2,
			double depth2, Color color2) {
		Graphics2D g = getViewGraphics2D();
		g.setColor(depth1 < depth2 ? color1 : color2);
		g.drawLine(toViewGraphicsCoordinate(x1), toViewGraphicsCoordinate(y1), toViewGraphicsCoordinate(x2),
				toViewGraphicsCoordinate(y2));
	}

	@Override
	public void paintPixelInWindowCoordinates(int xPixel, int yPixel, Color color) {
		Graphics2D g = getWindowGraphics2D();
		g.setColor(color);
		g.drawLine(xPixel, yPixel, xPixel, yPixel);
	}

}