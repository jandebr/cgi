package org.maia.cgi.gui.d3.renderer;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.camera.MovableCamera;
import org.maia.cgi.model.d3.camera.RevolvingCamera;

@SuppressWarnings("serial")
public class CameraControlsPanel extends Box {

	private Camera camera;

	private CameraControlAction lastControlAction;

	private long lastControlActionTime;

	private long minimumTimeBetweenRepeatActions;

	private List<CameraControlButton> controlButtons;

	private JToggleButton repeatButton;

	private JSlider incrementSlider;

	private double majorTranslationDistance;

	private double majorRotationAngleInRadians;

	public CameraControlsPanel(double majorTranslationDistance, double majorRotationAngleInRadians) {
		this(majorTranslationDistance, majorRotationAngleInRadians, 40L); // cap at 25fps
	}

	public CameraControlsPanel(double majorTranslationDistance, double majorRotationAngleInRadians,
			long minimumTimeBetweenRepeatActions) {
		super(BoxLayout.X_AXIS);
		this.majorTranslationDistance = majorTranslationDistance;
		this.majorRotationAngleInRadians = majorRotationAngleInRadians;
		this.minimumTimeBetweenRepeatActions = minimumTimeBetweenRepeatActions;
		this.controlButtons = createControlButtons();
		this.repeatButton = createRepeatButton();
		this.incrementSlider = createIncrementSlider();
		buildUI();
	}

	protected List<CameraControlButton> createControlButtons() {
		List<CameraControlButton> buttons = new Vector<CameraControlButton>();
		String group = CameraControlButton.REVOLVING_GROUP;
		buttons.add(new CameraControlButton(new RevolveLongitudinalAction(), group));
		buttons.add(new CameraControlButton(new RevolveLongitudinalReverseAction(), group));
		buttons.add(new CameraControlButton(new RevolveLatitudinalAction(), group));
		buttons.add(new CameraControlButton(new RevolveLatitudinalReverseAction(), group));
		buttons.add(new CameraControlButton(new RevolveFartherAction(), group));
		buttons.add(new CameraControlButton(new RevolveCloserAction(), group));
		group = CameraControlButton.SLIDING_GROUP;
		buttons.add(new CameraControlButton(new SlideRightAction(), group));
		buttons.add(new CameraControlButton(new SlideLeftAction(), group));
		buttons.add(new CameraControlButton(new SlideUpAction(), group));
		buttons.add(new CameraControlButton(new SlideDownAction(), group));
		buttons.add(new CameraControlButton(new SlideBackwardAction(), group));
		buttons.add(new CameraControlButton(new SlideForwardAction(), group));
		group = CameraControlButton.ROTATING_GROUP;
		buttons.add(new CameraControlButton(new RotatePitchAction(), group));
		buttons.add(new CameraControlButton(new RotatePitchReverseAction(), group));
		buttons.add(new CameraControlButton(new RotateYawAction(), group));
		buttons.add(new CameraControlButton(new RotateYawReverseAction(), group));
		buttons.add(new CameraControlButton(new RotateRollAction(), group));
		buttons.add(new CameraControlButton(new RotateRollReverseAction(), group));
		return buttons;
	}

	protected JToggleButton createRepeatButton() {
		JToggleButton button = new JToggleButton(RenderUIResources.repeatIcon);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorder(BorderFactory.createEtchedBorder(1));
		button.setFocusPainted(false);
		button.setToolTipText(RenderUIResources.repeatToolTipText);
		return button;
	}

	protected JSlider createIncrementSlider() {
		JSlider slider = new JSlider(1, 16, 4);
		slider.setMajorTickSpacing(15);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setToolTipText(RenderUIResources.incrementSliderToolTipText);
		return slider;
	}

	protected void buildUI() {
		add(new JLabel(RenderUIResources.cameraIcon));
		add(Box.createHorizontalStrut(16));
		CameraControlButton previous = null;
		for (CameraControlButton button : getControlButtons()) {
			if (previous != null && !previous.getGroup().equals(button.getGroup())) {
				add(Box.createHorizontalStrut(16));
			}
			add(button);
			previous = button;
		}
		add(Box.createHorizontalStrut(16));
		add(getRepeatButton());
		add(Box.createHorizontalStrut(16));
		add(getIncrementSlider());
		add(Box.createHorizontalGlue());
	}

	protected void updateCameraControlsEnablement() {
		Camera camera = getCamera();
		for (CameraControlButton button : getControlButtons()) {
			boolean enabled = false;
			if (isEnabled()) {
				if (camera != null && button.getAction().isSupportedByCamera(camera)) {
					enabled = true;
				}
			}
			button.setEnabled(enabled);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		updateCameraControlsEnablement();
	}

	public void connect(Camera camera) {
		if (isCameraConnected()) {
			disconnectCamera();
		}
		setCamera(camera);
		updateCameraControlsEnablement();
	}

	public void disconnectCamera() {
		setCamera(null);
		setLastControlAction(null);
		updateCameraControlsEnablement();
	}

	protected boolean isCameraConnected() {
		return getCamera() != null;
	}

	public boolean isRepeatActivated() {
		return getRepeatButton().isSelected();
	}

	public void repeatLastControlAction() {
		if (getLastControlAction() != null) {
			waitBetweenRepeatActions();
			getLastControlAction().actionPerformed(null);
		}
	}

	private void waitBetweenRepeatActions() {
		long waitTime = getLastControlActionTime() + getMinimumTimeBetweenRepeatActions() - System.currentTimeMillis();
		waitTime = Math.max(20L, waitTime); // prevent flashing by waiting at least 20ms
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
		}
	}

	protected double getRelativeIncrementValue() {
		int value = getIncrementSlider().getValue();
		int min = getIncrementSlider().getMinimum();
		int max = getIncrementSlider().getMaximum();
		return Math.pow((value - min + 1.0) / (max - min + 1.0), 2.0);
	}

	protected double getRotationAngleInRadiansIncrement() {
		return getRelativeIncrementValue() * getMajorRotationAngleInRadians();
	}

	protected double getTranslationDistanceIncrement() {
		return getRelativeIncrementValue() * getMajorTranslationDistance();
	}

	protected Camera getCamera() {
		return camera;
	}

	private void setCamera(Camera camera) {
		this.camera = camera;
	}

	private CameraControlAction getLastControlAction() {
		return lastControlAction;
	}

	private void setLastControlAction(CameraControlAction lastControlAction) {
		this.lastControlAction = lastControlAction;
	}

	private long getLastControlActionTime() {
		return lastControlActionTime;
	}

	private void setLastControlActionTime(long lastControlActionTime) {
		this.lastControlActionTime = lastControlActionTime;
	}

	private long getMinimumTimeBetweenRepeatActions() {
		return minimumTimeBetweenRepeatActions;
	}

	private List<CameraControlButton> getControlButtons() {
		return controlButtons;
	}

	private JToggleButton getRepeatButton() {
		return repeatButton;
	}

	private JSlider getIncrementSlider() {
		return incrementSlider;
	}

	public double getMajorTranslationDistance() {
		return majorTranslationDistance;
	}

	public void setMajorTranslationDistance(double majorTranslationDistance) {
		this.majorTranslationDistance = majorTranslationDistance;
	}

	public double getMajorRotationAngleInRadians() {
		return majorRotationAngleInRadians;
	}

	public void setMajorRotationAngleInRadians(double majorRotationAngleInRadians) {
		this.majorRotationAngleInRadians = majorRotationAngleInRadians;
	}

	private static class CameraControlButton extends JButton {

		public static final String REVOLVING_GROUP = "Revolve";

		public static final String SLIDING_GROUP = "Slide";

		public static final String ROTATING_GROUP = "Rotate";

		private String group;

		protected CameraControlButton(CameraControlAction action, String group) {
			super(action);
			this.group = group;
			setMargin(new Insets(0, 0, 0, 0));
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1),
					BorderFactory.createRaisedBevelBorder()));
			setFocusPainted(false);
		}

		@Override
		public CameraControlAction getAction() {
			return (CameraControlAction) super.getAction();
		}

		public String getGroup() {
			return group;
		}

	}

	private abstract class CameraControlAction extends AbstractAction {

		protected CameraControlAction(String name) {
			super(name);
		}

		protected CameraControlAction(Icon icon) {
			super("", icon);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			Camera camera = getActionCamera();
			if (camera != null) {
				if (isSupportedByCamera(camera)) {
					setLastControlAction(this);
					setLastControlActionTime(System.currentTimeMillis());
					perform();
				}
			}
		}

		public abstract boolean isSupportedByCamera(Camera camera);

		protected abstract void perform();

		protected Camera getActionCamera() {
			return CameraControlsPanel.this.getCamera();
		}

		protected void setToolTipText(String text) {
			putValue(Action.SHORT_DESCRIPTION, text);
		}

	}

	private abstract class RevolvingCameraAction extends CameraControlAction {

		protected RevolvingCameraAction(String name) {
			super(name);
		}

		protected RevolvingCameraAction(Icon icon) {
			super(icon);
		}

		@Override
		public boolean isSupportedByCamera(Camera camera) {
			return camera instanceof RevolvingCamera;
		}

		@Override
		protected RevolvingCamera getActionCamera() {
			return (RevolvingCamera) super.getActionCamera();
		}

	}

	private class RevolveLongitudinalAction extends RevolvingCameraAction {

		public RevolveLongitudinalAction() {
			super(RenderUIResources.revolveLongitudinalIcon);
			setToolTipText(RenderUIResources.revolveLongitudinalToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().revolveLongitudinal(getRotationAngleInRadiansIncrement());
		}
	}

	private class RevolveLongitudinalReverseAction extends RevolvingCameraAction {

		public RevolveLongitudinalReverseAction() {
			super(RenderUIResources.revolveLongitudinalReverseIcon);
			setToolTipText(RenderUIResources.revolveLongitudinalReverseToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().revolveLongitudinal(-getRotationAngleInRadiansIncrement());
		}
	}

	private class RevolveLatitudinalAction extends RevolvingCameraAction {

		public RevolveLatitudinalAction() {
			super(RenderUIResources.revolveLatitudinalIcon);
			setToolTipText(RenderUIResources.revolveLatitudinalToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().revolveLatitudinal(getRotationAngleInRadiansIncrement());
		}
	}

	private class RevolveLatitudinalReverseAction extends RevolvingCameraAction {

		public RevolveLatitudinalReverseAction() {
			super(RenderUIResources.revolveLatitudinalReverseIcon);
			setToolTipText(RenderUIResources.revolveLatitudinalReverseToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().revolveLatitudinal(-getRotationAngleInRadiansIncrement());
		}
	}

	private class RevolveCloserAction extends RevolvingCameraAction {

		public RevolveCloserAction() {
			super(RenderUIResources.revolveCloserIcon);
			setToolTipText(RenderUIResources.revolveCloserToolTipText);
		}

		@Override
		protected void perform() {
			double d = getTranslationDistanceIncrement();
			if (d < getActionCamera().getDistance()) {
				getActionCamera().alterDistance(-d);
			}
		}
	}

	private class RevolveFartherAction extends RevolvingCameraAction {

		public RevolveFartherAction() {
			super(RenderUIResources.revolveFartherIcon);
			setToolTipText(RenderUIResources.revolveFartherToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().alterDistance(getTranslationDistanceIncrement());
		}
	}

	private abstract class MovableCameraAction extends CameraControlAction {

		protected MovableCameraAction(String name) {
			super(name);
		}

		protected MovableCameraAction(Icon icon) {
			super(icon);
		}

		@Override
		public boolean isSupportedByCamera(Camera camera) {
			return camera instanceof MovableCamera;
		}

		@Override
		protected MovableCamera getActionCamera() {
			return (MovableCamera) super.getActionCamera();
		}

	}

	private class SlideRightAction extends MovableCameraAction {

		public SlideRightAction() {
			super(RenderUIResources.slideRightIcon);
			setToolTipText(RenderUIResources.slideRightToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().slide(getTranslationDistanceIncrement(), 0, 0);
		}
	}

	private class SlideLeftAction extends MovableCameraAction {

		public SlideLeftAction() {
			super(RenderUIResources.slideLeftIcon);
			setToolTipText(RenderUIResources.slideLeftToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().slide(-getTranslationDistanceIncrement(), 0, 0);
		}
	}

	private class SlideUpAction extends MovableCameraAction {

		public SlideUpAction() {
			super(RenderUIResources.slideUpIcon);
			setToolTipText(RenderUIResources.slideUpToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().slide(0, getTranslationDistanceIncrement(), 0);
		}
	}

	private class SlideDownAction extends MovableCameraAction {

		public SlideDownAction() {
			super(RenderUIResources.slideDownIcon);
			setToolTipText(RenderUIResources.slideDownToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().slide(0, -getTranslationDistanceIncrement(), 0);
		}
	}

	private class SlideBackwardAction extends MovableCameraAction {

		public SlideBackwardAction() {
			super(RenderUIResources.slideBackwardIcon);
			setToolTipText(RenderUIResources.slideBackwardToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().slide(0, 0, getTranslationDistanceIncrement());
		}
	}

	private class SlideForwardAction extends MovableCameraAction {

		public SlideForwardAction() {
			super(RenderUIResources.slideForwardIcon);
			setToolTipText(RenderUIResources.slideForwardToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().slide(0, 0, -getTranslationDistanceIncrement());
		}
	}

	private class RotatePitchAction extends MovableCameraAction {

		public RotatePitchAction() {
			super(RenderUIResources.rotatePitchIcon);
			setToolTipText(RenderUIResources.rotatePitchToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().pitch(getRotationAngleInRadiansIncrement());
		}
	}

	private class RotatePitchReverseAction extends MovableCameraAction {

		public RotatePitchReverseAction() {
			super(RenderUIResources.rotatePitchReverseIcon);
			setToolTipText(RenderUIResources.rotatePitchReverseToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().pitch(-getRotationAngleInRadiansIncrement());
		}
	}

	private class RotateYawAction extends MovableCameraAction {

		public RotateYawAction() {
			super(RenderUIResources.rotateYawIcon);
			setToolTipText(RenderUIResources.rotateYawToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().yaw(getRotationAngleInRadiansIncrement());
		}
	}

	private class RotateYawReverseAction extends MovableCameraAction {

		public RotateYawReverseAction() {
			super(RenderUIResources.rotateYawReverseIcon);
			setToolTipText(RenderUIResources.rotateYawReverseToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().yaw(-getRotationAngleInRadiansIncrement());
		}
	}

	private class RotateRollAction extends MovableCameraAction {

		public RotateRollAction() {
			super(RenderUIResources.rotateRollIcon);
			setToolTipText(RenderUIResources.rotateRollToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().roll(getRotationAngleInRadiansIncrement());
		}
	}

	private class RotateRollReverseAction extends MovableCameraAction {

		public RotateRollReverseAction() {
			super(RenderUIResources.rotateRollReverseIcon);
			setToolTipText(RenderUIResources.rotateRollReverseToolTipText);
		}

		@Override
		protected void perform() {
			getActionCamera().roll(-getRotationAngleInRadiansIncrement());
		}
	}

}