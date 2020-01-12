package org.maia.cgi.gui.d3.renderer;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class AboutPanel extends Box {

	public AboutPanel() {
		super(BoxLayout.Y_AXIS);
		buildUI();
	}

	protected void buildUI() {
		setBorder(BorderFactory.createEmptyBorder(24, 64, 64, 64));
		add(buildLogo());
		add(Box.createVerticalStrut(24));
		add(buildPropertyLabel("Developed by"));
		add(buildValueLabel("Jan De Beer"));
		add(Box.createVerticalStrut(24));
		add(buildPropertyLabel("In the year"));
		add(buildValueLabel("2019"));
		add(Box.createVerticalStrut(24));
		add(buildPropertyLabel("Licensed under"));
		add(buildValueLabel("Creative Commons BY-NC"));
	}

	protected JLabel buildLogo() {
		JLabel label = new JLabel(RenderUIResources.aboutLogoIcon);
		label.setAlignmentX(0.5f);
		return label;
	}

	protected JLabel buildPropertyLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
		label.setAlignmentX(0.5f);
		return label;
	}

	protected JLabel buildValueLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(14f));
		label.setForeground(new Color(194, 60, 8));
		label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		label.setAlignmentX(0.5f);
		return label;
	}

}