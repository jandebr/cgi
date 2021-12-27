package org.maia.cgi.gui.d3.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.maia.cgi.CGISystem;
import org.maia.cgi.Metrics;
import org.maia.cgi.gui.GradientButton;
import org.maia.cgi.gui.d3.renderer.RenderOptionsPanel.RenderOptionsPanelObserver;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.camera.CameraObserver;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.model.d3.scene.SceneUtils;
import org.maia.cgi.render.d3.RenderOptions;
import org.maia.cgi.render.d3.RenderOptions.RenderMode;
import org.maia.cgi.render.d3.SceneRenderer;
import org.maia.cgi.render.d3.SceneRendererProgressTracker;
import org.maia.cgi.render.d3.view.ViewPort;

@SuppressWarnings("serial")
public class RenderFrame extends JFrame implements SceneRendererProgressTracker, CameraObserver,
		RenderOptionsPanelObserver {

	private String baseTitle;

	private Scene scene;

	private RenderOptions renderOptions;

	private RenderKit renderKit;

	private CameraControlsPanel cameraControlsPanel;

	private RenderOptionsPanel renderOptionsPanel;

	private RenderPane renderPane;

	private ScrollRenderPane scrollRenderPane;

	private RenderAction renderAction;

	private RenderButton renderButton;

	private ExportImageAction exportImageAction;

	private ExportImageButton exportImageButton;

	private ExitAction exitAction;

	private DisplayMetricsAction displayMetricsAction;

	private DisplayAboutAction displayAboutAction;

	private JMenu sceneSelectionMenu;

	private JProgressBar progressBar;

	private JLabel cpuUsageLabel;

	private JLabel memoryUsageLabel;

	private Thread renderThread;

	private long renderTimeMs;

	private static NumberFormat percentageFormat;

	static {
		percentageFormat = NumberFormat.getPercentInstance();
	}

	public RenderFrame(RenderKit renderKit, double majorTranslationDistance, double majorRotationAngleInRadians) {
		this(800, 600, renderKit, majorTranslationDistance, majorRotationAngleInRadians);
	}

	public RenderFrame(int viewWidth, int viewHeight, RenderKit renderKit, double majorTranslationDistance,
			double majorRotationAngleInRadians) {
		this(viewWidth, viewHeight, Color.DARK_GRAY, renderKit, majorTranslationDistance, majorRotationAngleInRadians);
	}

	public RenderFrame(int viewWidth, int viewHeight, Color viewBackgroundColor, RenderKit renderKit,
			double majorTranslationDistance, double majorRotationAngleInRadians) {
		super(RenderUIResources.frameTitle);
		this.baseTitle = getTitle();
		this.renderKit = renderKit;
		this.cameraControlsPanel = createCameraControlsPanel(majorTranslationDistance, majorRotationAngleInRadians);
		this.renderOptionsPanel = createRenderOptionsPanel();
		this.renderPane = createRenderPane(viewWidth, viewHeight);
		this.scrollRenderPane = createScrollRenderPane(viewWidth, viewHeight, viewBackgroundColor);
		this.renderAction = new RenderAction();
		this.renderButton = createRenderButton();
		this.exportImageAction = new ExportImageAction();
		this.exportImageButton = createExportImageButton();
		this.exitAction = new ExitAction();
		this.displayMetricsAction = new DisplayMetricsAction();
		this.displayAboutAction = new DisplayAboutAction();
		this.sceneSelectionMenu = buildSceneSelectionMenu();
		this.progressBar = createProgressBar();
		this.cpuUsageLabel = createCpuUsageLabel();
		this.memoryUsageLabel = createMemoryUsageLabel();
		buildUI();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		new Thread(new SystemUsageUpdater()).start();
	}

	protected CameraControlsPanel createCameraControlsPanel(double majorTranslationDistance,
			double majorRotationAngleInRadians) {
		return new CameraControlsPanel(majorTranslationDistance, majorRotationAngleInRadians);
	}

	protected RenderOptionsPanel createRenderOptionsPanel() {
		RenderOptionsPanel panel = new RenderOptionsPanel();
		panel.setAlignmentX(0);
		panel.addObserver(this);
		return panel;
	}

	protected RenderPane createRenderPane(int viewWidth, int viewHeight) {
		RenderPane pane = new RenderPane();
		pane.changeRenderSize(viewWidth, viewHeight);
		return pane;
	}

	protected ScrollRenderPane createScrollRenderPane(int viewWidth, int viewHeight, Color viewBackgroundColor) {
		return new ScrollRenderPane(viewWidth, viewHeight, viewBackgroundColor, getRenderPane());
	}

	protected RenderButton createRenderButton() {
		return new RenderButton(getRenderAction());
	}

	protected ExportImageButton createExportImageButton() {
		return new ExportImageButton(getExportImageAction());
	}

	protected JProgressBar createProgressBar() {
		JProgressBar bar = new JProgressBar(0, 100);
		bar.setValue(0);
		bar.setStringPainted(false);
		bar.setPreferredSize(new Dimension(100, 24));
		return bar;
	}

	protected JLabel createCpuUsageLabel() {
		JLabel label = new JLabel("CPU", RenderUIResources.usageCpuIcon, SwingConstants.LEFT);
		label.setFont(label.getFont().deriveFont(10f));
		label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		return label;
	}

	protected JLabel createMemoryUsageLabel() {
		JLabel label = new JLabel("RAM", RenderUIResources.usageMemoryIcon, SwingConstants.LEFT);
		label.setFont(label.getFont().deriveFont(10f));
		label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		return label;
	}

	protected void buildUI() {
		setJMenuBar(buildMenuBar());
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(buildCameraPanel(), BorderLayout.NORTH);
		panel.add(buildRenderPanel(), BorderLayout.EAST);
		panel.add(getScrollRenderPane(), BorderLayout.CENTER);
		panel.add(getProgressBar(), BorderLayout.SOUTH);
		getContentPane().add(panel);
		pack();
	}

	protected JComponent buildCameraPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(getCameraControlsPanel(), BorderLayout.WEST);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		return panel;
	}

	protected JComponent buildRenderPanel() {
		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(getRenderOptionsPanel());
		box.add(Box.createVerticalStrut(24));
		box.add(buildActionButtonsPanel());
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(box, BorderLayout.NORTH);
		panel.add(buildUsagePanel(), BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		return panel;
	}

	protected JComponent buildActionButtonsPanel() {
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(4, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;
		layout.setConstraints(getRenderButton(), c);
		c.insets = new Insets(16, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 1;
		layout.setConstraints(getExportImageButton(), c);
		JPanel panel = new JPanel(layout);
		panel.add(getRenderButton());
		panel.add(getExportImageButton());
		panel.setAlignmentX(0);
		return panel;
	}

	protected JComponent buildUsagePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(getCpuUsageLabel(), BorderLayout.NORTH);
		panel.add(getMemoryUsageLabel(), BorderLayout.SOUTH);
		return panel;
	}

	protected JMenuBar buildMenuBar() {
		JMenuBar bar = new JMenuBar();
		bar.add(buildFileMenu());
		bar.add(buildRenderMenu());
		bar.add(buildExtraMenu());
		return bar;
	}

	protected JMenu buildFileMenu() {
		JMenu menu = new JMenu(RenderUIResources.fileMenuTitle);
		menu.add(getSceneSelectionMenu());
		menu.add(new JMenuItem(getExportImageAction()));
		menu.add(new JMenuItem(getExitAction()));
		return menu;
	}

	protected JMenu buildRenderMenu() {
		JMenu menu = new JMenu(RenderUIResources.renderMenuTitle);
		menu.add(new JMenuItem(getRenderAction()));
		return menu;
	}

	protected JMenu buildExtraMenu() {
		JMenu menu = new JMenu(RenderUIResources.extraMenuTitle);
		menu.add(new JMenuItem(getDisplayMetricsAction()));
		menu.add(new JMenuItem(getDisplayAboutAction()));
		return menu;
	}

	protected JMenu buildSceneSelectionMenu() {
		return new JMenu(RenderUIResources.sceneSelectionMenuTitle);
	}

	public void maximize() {
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
	}

	public void showWithLoadedScene(final Scene scene, final RenderOptions options) {
		final RenderFrame frame = this;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					frame.setVisible(true);
					frame.load(scene, options);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isSceneLoaded() {
		return getScene() != null;
	}

	public void load(Scene scene, RenderOptions options) {
		if (scene != null) {
			if (isSceneLoaded()) {
				unloadScene();
			}
			setScene(scene);
			setRenderOptions(options);
			scene.getCamera().addObserver(this);
			getCameraControlsPanel().connect(scene.getCamera());
			getDisplayMetricsAction().setEnabled(true);
			updateTitle();
			updateRenderOptions();
			renderPrototype();
		}
	}

	private void unloadScene() {
		CGISystem.releaseMemory(getScene());
		getScene().getCamera().removeObserver(this);
		getCameraControlsPanel().disconnectCamera();
		getRenderOptionsPanel().restoreRenderOptionsSize();
		setScene(null);
		setRenderOptions(null);
		updateTitle();
		getDisplayMetricsAction().setEnabled(false);
	}

	public void addToSceneSelectionMenu(Scene scene, RenderOptions options) {
		getSceneSelectionMenu().add(new JMenuItem(new SelectSceneAction(scene, options)));
	}

	private void updateTitle() {
		String title = getBaseTitle();
		if (isSceneLoaded()) {
			if (getScene().getName() != null) {
				title += " - " + getScene().getName();
			}
			title += " (" + getRenderOptions().getRenderWidth() + " x " + getRenderOptions().getRenderHeight() + ")";
		}
		setTitle(title);
	}

	protected void renderPrototype() {
		render(RenderMode.PROTOTYPE);
	}

	protected void renderRealistically() {
		render(RenderMode.REALISTIC);
	}

	protected synchronized void render(RenderMode renderMode) {
		if (isSceneLoaded() && !isRendering()) {
			RenderOptions options = getRenderOptions();
			options.setRenderMode(renderMode);
			SceneRenderer renderer = getRenderKit().createRenderer(getScene(), options);
			renderer.addProgressTracker(this);
			renderer.addProgressTracker(new RenderChrono());
			ViewPort viewPort = getRenderPane().getOutputViewPort();
			Thread worker = new Thread(new RenderWorker(renderer, viewPort, options));
			setRenderThread(worker);
			worker.start();
		}
	}

	protected synchronized boolean isRendering() {
		return getRenderThread() != null;
	}

	protected boolean isInRealisticRenderMode() {
		return RenderMode.REALISTIC.equals(getRenderOptions().getRenderMode());
	}

	@Override
	public void renderingStarted(SceneRenderer renderer, Scene scene) {
		if (isInRealisticRenderMode()) {
			showZeroProgress();
		}
		if (isInRealisticRenderMode() || getCameraControlsPanel().isRepeatActivated()) {
			disableRenderPanel();
			getCameraControlsPanel().setEnabled(false);
			getSceneSelectionMenu().setEnabled(false);
		}
	}

	@Override
	public void renderingProgressUpdate(SceneRenderer renderer, Scene scene, int totalSteps, int stepIndex,
			double stepProgress, String stepLabel) {
		if (isInRealisticRenderMode()) {
			getRenderPane().repaint();
			showProgress(totalSteps, stepIndex, stepProgress, stepLabel);
		}
	}

	@Override
	public synchronized void renderingCompleted(SceneRenderer renderer, Scene scene) {
		setRenderThread(null);
		getRenderPane().repaint();
		if (isInRealisticRenderMode()) {
			clearProgress();
			CGISystem.releaseMemory();
		}
		if (isInRealisticRenderMode() || !getCameraControlsPanel().isRepeatActivated()) {
			enableRenderPanel();
			getCameraControlsPanel().setEnabled(true);
			getSceneSelectionMenu().setEnabled(true);
		}
		if (!isInRealisticRenderMode() && getCameraControlsPanel().isRepeatActivated()) {
			getCameraControlsPanel().repeatLastControlAction();
		}
	}

	private void showZeroProgress() {
		showProgress(0, -1, 0.0, "Start rendering...");
	}

	protected void showProgress(int totalSteps, int stepIndex, double stepProgress, String stepLabel) {
		JProgressBar bar = getProgressBar();
		bar.setValue((int) Math.floor(stepProgress * 100.0));
		bar.setString(makeProgressString(totalSteps, stepIndex, stepProgress, stepLabel));
		bar.setStringPainted(true);
	}

	protected String makeProgressString(int totalSteps, int stepIndex, double stepProgress, String stepLabel) {
		StringBuilder sb = new StringBuilder(32);
		if (totalSteps > 1) {
			sb.append('(');
			sb.append(stepIndex + 1);
			sb.append('/');
			sb.append(totalSteps);
			sb.append(')');
		}
		if (stepLabel != null) {
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(stepLabel);
		}
		if (stepIndex >= 0) {
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(percentageFormat.format(stepProgress));
		}
		return sb.toString();
	}

	protected void clearProgress() {
		JProgressBar bar = getProgressBar();
		bar.setValue(0);
		bar.setString(null);
		bar.setStringPainted(false);
	}

	protected void showSystemUsage() {
		double cpuLoad = CGISystem.getCpuLoad();
		long totalMemory = CGISystem.getTotalMemoryInBytes();
		long usedMemory = CGISystem.getUsedMemoryInBytes();
		long usedMemoryMB = usedMemory / (1024 * 1024);
		double usedMemoryRatio = usedMemory / (double) totalMemory;
		getCpuUsageLabel().setText("CPU " + percentageFormat.format(cpuLoad));
		getMemoryUsageLabel().setText("RAM " + percentageFormat.format(usedMemoryRatio) + " (" + usedMemoryMB + "MB)");
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		renderPrototype();
	}

	@Override
	public void renderOptionsChanged(RenderOptions renderOptions) {
		if (hasRenderSizeChanged(renderOptions)) {
			updateTitle(); // about size
			getRenderPane().updateRenderOptions(renderOptions);
			renderPrototype();
		}
	}

	private boolean hasRenderSizeChanged(RenderOptions renderOptions) {
		int paneWidth = getRenderPane().getWidth();
		int paneHeight = getRenderPane().getHeight();
		int optionsWidth = renderOptions.getRenderWidth();
		int optionsHeight = renderOptions.getRenderHeight();
		return paneWidth != optionsWidth || paneHeight != optionsHeight;
	}

	protected void updateRenderOptions() {
		getRenderOptionsPanel().updateRenderOptions(getRenderOptions());
		getRenderPane().updateRenderOptions(getRenderOptions());
	}

	protected void disableRenderPanel() {
		setRenderPanelEnabled(false);
	}

	protected void enableRenderPanel() {
		setRenderPanelEnabled(true);
	}

	private void setRenderPanelEnabled(boolean enabled) {
		getRenderOptionsPanel().setEnabled(enabled);
		getRenderAction().setEnabled(enabled);
		getExportImageAction().setEnabled(enabled);
	}

	protected String getBaseTitle() {
		return baseTitle;
	}

	protected Scene getScene() {
		return scene;
	}

	private void setScene(Scene scene) {
		this.scene = scene;
	}

	protected RenderOptions getRenderOptions() {
		return renderOptions;
	}

	private void setRenderOptions(RenderOptions renderOptions) {
		this.renderOptions = renderOptions;
	}

	protected RenderKit getRenderKit() {
		return renderKit;
	}

	protected CameraControlsPanel getCameraControlsPanel() {
		return cameraControlsPanel;
	}

	protected RenderOptionsPanel getRenderOptionsPanel() {
		return renderOptionsPanel;
	}

	protected RenderPane getRenderPane() {
		return renderPane;
	}

	private ScrollRenderPane getScrollRenderPane() {
		return scrollRenderPane;
	}

	private RenderAction getRenderAction() {
		return renderAction;
	}

	private RenderButton getRenderButton() {
		return renderButton;
	}

	private ExportImageAction getExportImageAction() {
		return exportImageAction;
	}

	private ExportImageButton getExportImageButton() {
		return exportImageButton;
	}

	private ExitAction getExitAction() {
		return exitAction;
	}

	private DisplayMetricsAction getDisplayMetricsAction() {
		return displayMetricsAction;
	}

	private DisplayAboutAction getDisplayAboutAction() {
		return displayAboutAction;
	}

	private JMenu getSceneSelectionMenu() {
		return sceneSelectionMenu;
	}

	private JProgressBar getProgressBar() {
		return progressBar;
	}

	private JLabel getCpuUsageLabel() {
		return cpuUsageLabel;
	}

	private JLabel getMemoryUsageLabel() {
		return memoryUsageLabel;
	}

	private Thread getRenderThread() {
		return renderThread;
	}

	private void setRenderThread(Thread renderThread) {
		this.renderThread = renderThread;
	}

	private long getRenderTimeMs() {
		return renderTimeMs;
	}

	private void setRenderTimeMs(long renderTimeMs) {
		this.renderTimeMs = renderTimeMs;
	}

	private class RenderWorker implements Runnable {

		private SceneRenderer renderer;

		private ViewPort viewPort;

		private RenderOptions options;

		public RenderWorker(SceneRenderer renderer, ViewPort viewPort, RenderOptions options) {
			this.renderer = renderer;
			this.viewPort = viewPort;
			this.options = options;
		}

		@Override
		public void run() {
			Metrics.getInstance().resetCounters();
			getRenderer().render(getScene(), getViewPort(), getOptions());
		}

		private SceneRenderer getRenderer() {
			return renderer;
		}

		private ViewPort getViewPort() {
			return viewPort;
		}

		private RenderOptions getOptions() {
			return options;
		}

	}

	private class SystemUsageUpdater implements Runnable {

		public SystemUsageUpdater() {
		}

		@Override
		public void run() {
			do {
				showSystemUsage();
				try {
					Thread.sleep(2000L);
				} catch (InterruptedException e) {
				}
			} while (true);
		}

	}

	private static class RenderButton extends GradientButton {

		public RenderButton(RenderAction action) {
			super(action, Color.WHITE, new Color(86, 227, 124));
			setPreferredSize(new Dimension(120, 64));
			setFont(getFont().deriveFont(16f));
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
					BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			setFocusPainted(false);
		}

	}

	private class RenderAction extends AbstractAction {

		public RenderAction() {
			super(RenderUIResources.renderLabel, RenderUIResources.renderIcon);
			putValue(Action.SHORT_DESCRIPTION, RenderUIResources.renderToolTipText);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			renderRealistically();
		}

	}

	private static class ExportImageButton extends JButton {

		public ExportImageButton(ExportImageAction action) {
			super(action);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
					BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			setFocusPainted(false);
		}

	}

	private class ExportImageAction extends AbstractAction {

		public ExportImageAction() {
			super(RenderUIResources.exportLabel, RenderUIResources.exportIcon);
			putValue(Action.SHORT_DESCRIPTION, RenderUIResources.exportToolTipText);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			File file = chooseFile();
			if (file != null) {
				exportImageToFile(getImage(), file, "png");
			}
		}

		protected File chooseFile() {
			File file = null;
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(RenderUIResources.exportFileChooserFilterName,
					"png");
			fileChooser.setFileFilter(filter);
			fileChooser.setDialogTitle(RenderUIResources.exportFileChooserDialogTitle);
			int returnValue = fileChooser.showSaveDialog(RenderFrame.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				file = fileChooser.getSelectedFile();
			}
			return file;
		}

		protected BufferedImage getImage() {
			return getRenderPane().getOutputImage();
		}

		protected void exportImageToFile(BufferedImage image, File file, String fileFormat) {
			try {
				ImageIO.write(image, fileFormat, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private class ExitAction extends AbstractAction {

		public ExitAction() {
			super(RenderUIResources.fileMenuItemExit);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			System.exit(0);
		}

	}

	private class SelectSceneAction extends AbstractAction {

		private Scene scene;

		private RenderOptions renderOptions;

		public SelectSceneAction(Scene scene, RenderOptions options) {
			super(scene.getName() != null ? scene.getName() : "Untitled");
			this.scene = scene;
			this.renderOptions = options;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			load(getScene(), getRenderOptions());
		}

		public Scene getScene() {
			return scene;
		}

		public RenderOptions getRenderOptions() {
			return renderOptions;
		}

	}

	private abstract class DialogAction extends AbstractAction {

		protected DialogAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			JDialog dialog = new JDialog(RenderFrame.this, getDialogTitle(), true);
			dialog.getContentPane().add(buildDialogContent());
			dialog.pack();
			dialog.setLocationRelativeTo(RenderFrame.this);
			dialog.setVisible(true);
		}

		protected abstract String getDialogTitle();

		protected abstract JComponent buildDialogContent();

	}

	private class DisplayMetricsAction extends DialogAction {

		public DisplayMetricsAction() {
			super(RenderUIResources.extraMenuItemShowMetrics);
			setEnabled(false);
		}

		@Override
		protected String getDialogTitle() {
			return RenderUIResources.metricsDialogTitle;
		}

		@Override
		protected JComponent buildDialogContent() {
			return new MetricsPanel(SceneUtils.getModelMetrics(getScene()), Metrics.getInstance(), getRenderTimeMs());
		}

	}

	private class DisplayAboutAction extends DialogAction {

		public DisplayAboutAction() {
			super(RenderUIResources.extraMenuItemAbout);
		}

		@Override
		protected String getDialogTitle() {
			return RenderUIResources.aboutDialogTitle;
		}

		@Override
		protected JComponent buildDialogContent() {
			return new AboutPanel();
		}

	}

	private static class ScrollRenderPane extends JScrollPane {

		public ScrollRenderPane(int width, int height, Color backgroundColor, RenderPane renderPane) {
			super(renderPane);
			Dimension size = new Dimension(width, height);
			getViewport().setPreferredSize(size);
			getViewport().setBackground(backgroundColor);
		}

	}

	private class RenderChrono implements SceneRendererProgressTracker {

		private long startTimeMs;

		@Override
		public void renderingStarted(SceneRenderer renderer, Scene scene) {
			startTimeMs = System.currentTimeMillis();
			updateRenderTimeMs();
		}

		@Override
		public void renderingProgressUpdate(SceneRenderer renderer, Scene scene, int totalSteps, int stepIndex,
				double stepProgress, String stepLabel) {
			updateRenderTimeMs();
		}

		@Override
		public void renderingCompleted(SceneRenderer renderer, Scene scene) {
			updateRenderTimeMs();
		}

		private void updateRenderTimeMs() {
			setRenderTimeMs(System.currentTimeMillis() - startTimeMs);
		}

	}

}