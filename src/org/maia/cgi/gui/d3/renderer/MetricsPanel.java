package org.maia.cgi.gui.d3.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.maia.cgi.Metrics;
import org.maia.cgi.model.d3.scene.SceneUtils.ModelMetrics;

@SuppressWarnings("serial")
public class MetricsPanel extends JPanel {

	private static NumberFormat numberFormat;

	static {
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setGroupingUsed(true);
	}

	public MetricsPanel(ModelMetrics modelMetrics, Metrics computeMetrics, long renderTimeMs) {
		buildUI(modelMetrics, computeMetrics, renderTimeMs);
	}

	protected void buildUI(ModelMetrics modelMetrics, Metrics computeMetrics, long renderTimeMs) {
		JTabbedPane tpane = new JTabbedPane();
		tpane.addTab(RenderUIResources.metricsModelTabTitle, buildModelMetricsPanel(modelMetrics));
		tpane.addTab(RenderUIResources.metricsComputeTabTitle, buildComputeMetricsPanel(computeMetrics, renderTimeMs));
		add(tpane);
	}

	protected JComponent buildModelMetricsPanel(ModelMetrics modelMetrics) {
		JPanel panel = new JPanel(new GridLayout(0, 2, 16, 2));
		panel.add(buildMetricNameLabel("Vertices"));
		panel.add(buildMetricValueLabel(modelMetrics.getVertices()));
		panel.add(buildMetricNameLabel("Vertices (unique)"));
		panel.add(buildMetricValueLabel(modelMetrics.getUniqueVertices()));
		panel.add(buildMetricNameLabel("Edges"));
		panel.add(buildMetricValueLabel(modelMetrics.getEdges()));
		panel.add(buildMetricNameLabel("Edges (unique)"));
		panel.add(buildMetricValueLabel(modelMetrics.getUniqueEdges()));
		panel.add(buildMetricNameLabel("Faces"));
		panel.add(buildMetricValueLabel(modelMetrics.getFaces()));
		JPanel parent = new JPanel(new BorderLayout());
		parent.add(panel, BorderLayout.NORTH);
		parent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		return parent;
	}

	protected JComponent buildComputeMetricsPanel(Metrics computeMetrics, long renderTimeMs) {
		JPanel panel = new JPanel(new GridLayout(0, 2, 16, 2));
		panel.add(buildMetricNameLabel("Render time"));
		panel.add(buildMetricValueLabel(formatRenderTime(renderTimeMs)));
		addSpacer(panel);
		panel.add(buildMetricNameLabel("Point transformations"));
		panel.add(buildMetricValueLabel(computeMetrics.getPointTransformations()));
		panel.add(buildMetricNameLabel("Point normalizations"));
		panel.add(buildMetricValueLabel(computeMetrics.getPointNormalizations()));
		panel.add(buildMetricNameLabel("Vector dot products"));
		panel.add(buildMetricValueLabel(computeMetrics.getVectorDotProducts()));
		panel.add(buildMetricNameLabel("Vector cross products"));
		panel.add(buildMetricValueLabel(computeMetrics.getVectorCrossProducts()));
		panel.add(buildMetricNameLabel("Vector normalizations"));
		panel.add(buildMetricValueLabel(computeMetrics.getVectorNormalizations()));
		panel.add(buildMetricNameLabel("Vector angles"));
		panel.add(buildMetricValueLabel(computeMetrics.getVectorAnglesInBetween()));
		panel.add(buildMetricNameLabel("Matrix multiplications"));
		panel.add(buildMetricValueLabel(computeMetrics.getMatrixMultiplications()));
		panel.add(buildMetricNameLabel("Matrix inversions"));
		panel.add(buildMetricValueLabel(computeMetrics.getMatrixInversions()));
		addSpacer(panel);
		panel.add(buildMetricNameLabel("Line with line intersections"));
		panel.add(buildMetricValueLabel(computeMetrics.getLineWithLineIntersections()));
		panel.add(buildMetricNameLabel("Line with plane intersections"));
		panel.add(buildMetricValueLabel(computeMetrics.getLineWithPlaneIntersections()));
		panel.add(buildMetricNameLabel("Line with object intersections"));
		panel.add(buildMetricValueLabel(computeMetrics.getLineWithObjectIntersections()));
		panel.add(buildMetricNameLabel("Line with object hits"));
		panel.add(buildMetricValueLabel(computeMetrics.getLineWithObjectHits()));
		addSpacer(panel);
		panel.add(buildMetricNameLabel("Bounding box computations"));
		panel.add(buildMetricValueLabel(computeMetrics.getBoundingBoxComputations()));
		panel.add(buildMetricNameLabel("Point inside simple face checks"));
		panel.add(buildMetricValueLabel(computeMetrics.getPointInsideSimpleFaceChecks()));
		panel.add(buildMetricNameLabel("Point to light source traversals"));
		panel.add(buildMetricValueLabel(computeMetrics.getSurfacePositionToLightSourceTraversals()));
		panel.add(buildMetricNameLabel("Point to light source object encounters"));
		panel.add(buildMetricValueLabel(computeMetrics.getSurfacePositionToLightSourceObjectEncounters()));
		JPanel parent = new JPanel(new BorderLayout());
		parent.add(buildComputeMetricsDescription(), BorderLayout.NORTH);
		parent.add(panel, BorderLayout.CENTER);
		parent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		return parent;
	}

	protected void addSpacer(JPanel panel) {
		panel.add(Box.createGlue());
		panel.add(Box.createGlue());
	}

	protected JLabel buildComputeMetricsDescription() {
		JLabel label = new JLabel(RenderUIResources.metricsComputeDescription);
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		return label;
	}

	protected JLabel buildMetricNameLabel(String name) {
		JLabel label = new JLabel(name);
		return label;
	}

	protected JLabel buildMetricValueLabel(long value) {
		JLabel label = buildMetricValueLabel(numberFormat.format(value));
		if (value == 0) {
			label.setForeground(Color.GRAY);
		}
		return label;
	}

	protected JLabel buildMetricValueLabel(String value) {
		JLabel label = new JLabel(value);
		label.setForeground(new Color(194, 60, 8));
		return label;
	}

	private String formatRenderTime(long renderTimeMs) {
		long renderTimeSecs = renderTimeMs / 1000L;
		int hours = (int) (renderTimeSecs / 3600L);
		renderTimeSecs -= hours * 3600L;
		int minutes = (int) (renderTimeSecs / 60L);
		renderTimeSecs -= minutes * 60L;
		int seconds = (int) renderTimeSecs;
		int milliSeconds = (int) (renderTimeMs % 1000L);
		return formatTime(hours, minutes, seconds, milliSeconds);
	}

	private String formatTime(int hours, int minutes, int seconds, int milliSeconds) {
		StringBuilder sb = new StringBuilder();
		sb.append(formatWithMinimumDigits(hours, 2)).append(":").append(formatWithMinimumDigits(minutes, 2))
				.append(":").append(formatWithMinimumDigits(seconds, 2)).append(".")
				.append(formatWithMinimumDigits(milliSeconds, 3));
		return sb.toString();
	}

	private String formatWithMinimumDigits(int n, int minDigits) {
		String str = String.valueOf(n);
		while (str.length() < minDigits) {
			str = "0" + str;
		}
		return str;
	}

}