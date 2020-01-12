package org.maia.cgi.render.d3.view;

import java.awt.Color;

public interface ViewPort {

	void startRendering();

	void stopRendering();

	void clear();
	
	void drawLineInViewCoordinates(double x1, double y1, double depth1, Color color1, double x2, double y2, double depth2, Color color2);
	
	void paintPixelInWindowCoordinates(int xPixel, int yPixel, Color color);

}