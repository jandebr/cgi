package org.maia.cgi.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.Action;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class GradientButton extends JButton {

	private Color colorFrom;

	private Color colorTo;

	public GradientButton(Action action, Color colorFrom, Color colorTo) {
		super(action);
		setContentAreaFilled(false);
		this.colorFrom = colorFrom;
		this.colorTo = colorTo;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setPaint(new GradientPaint(new Point(0, 0), getColorFrom(), new Point(0, getHeight()), getColorTo()));
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.dispose();
		super.paintComponent(g);
	}

	public Color getColorFrom() {
		return colorFrom;
	}

	public Color getColorTo() {
		return colorTo;
	}

}