package org.maia.cgi.gui.d3.renderer;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class RenderOptionsPanel extends Box {

	private RenderOptions renderOptions;

	private int originalRenderWidth;

	private int originalRenderHeight;

	private ButtonGroup magnificationButtonGroup;

	private RenderOptionCheckbox shadowsCheckbox;

	private RenderOptionCheckbox backdropCheckbox;

	private RenderOptionCheckbox superSamplingCheckbox;

	private RenderOptionCheckbox depthBlurCheckbox;

	private Collection<RenderOptionsPanelObserver> observers;

	public RenderOptionsPanel() {
		this(RenderOptions.createDefaultOptions());
	}

	public RenderOptionsPanel(RenderOptions renderOptions) {
		super(BoxLayout.Y_AXIS);
		this.magnificationButtonGroup = createMagnificationButtonGroup();
		this.shadowsCheckbox = createShadowsCheckbox();
		this.backdropCheckbox = createBackdropCheckbox();
		this.superSamplingCheckbox = createSuperSamplingCheckbox();
		this.depthBlurCheckbox = createDepthBlurCheckbox();
		this.observers = new Vector<RenderOptionsPanelObserver>();
		buildUI();
		updateRenderOptions(renderOptions);
	}

	protected ButtonGroup createMagnificationButtonGroup() {
		ButtonGroup group = new ButtonGroup();
		group.add(new MagnificationButton(new OriginalMagnificationAction()));
		group.add(new MagnificationButton(new DoubleMagnificationAction()));
		group.add(new MagnificationButton(new TripleMagnificationAction()));
		return group;
	}

	protected RenderOptionCheckbox createShadowsCheckbox() {
		return new RenderOptionCheckbox(new ShadowsAction());
	}

	protected RenderOptionCheckbox createBackdropCheckbox() {
		return new RenderOptionCheckbox(new BackdropAction());
	}

	protected RenderOptionCheckbox createSuperSamplingCheckbox() {
		return new RenderOptionCheckbox(new SuperSamplingAction());
	}

	protected RenderOptionCheckbox createDepthBlurCheckbox() {
		return new RenderOptionCheckbox(new DepthBlurAction());
	}

	protected void buildUI() {
		add(buildMagnificationButtonPanel());
		add(Box.createVerticalStrut(16));
		add(getShadowsCheckbox());
		add(getSuperSamplingCheckbox());
		add(getDepthBlurCheckbox());
		add(getBackdropCheckbox());
	}

	protected JComponent buildMagnificationButtonPanel() {
		Box box = new Box(BoxLayout.X_AXIS);
		Enumeration<AbstractButton> buttons = getMagnificationButtonGroup().getElements();
		while (buttons.hasMoreElements()) {
			box.add(buttons.nextElement());
		}
		box.setAlignmentX(0);
		return box;
	}

	public void updateRenderOptions(RenderOptions renderOptions) {
		setRenderOptions(renderOptions);
		setOriginalRenderWidth(renderOptions.getRenderWidth());
		setOriginalRenderHeight(renderOptions.getRenderHeight());
		getMagnificationButtonGroup().getElements().nextElement().setSelected(true); // original size
		getShadowsCheckbox().setSelected(renderOptions.isShadowsEnabled());
		getBackdropCheckbox().setSelected(renderOptions.isBackdropEnabled());
		getSuperSamplingCheckbox().setSelected(renderOptions.isSuperSamplingEnabled());
		getDepthBlurCheckbox().setSelected(renderOptions.isDepthBlurEnabled());
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Enumeration<AbstractButton> buttons = getMagnificationButtonGroup().getElements();
		while (buttons.hasMoreElements()) {
			buttons.nextElement().setEnabled(enabled);
		}
		getShadowsCheckbox().setEnabled(enabled);
		getBackdropCheckbox().setEnabled(enabled);
		getSuperSamplingCheckbox().setEnabled(enabled);
		getDepthBlurCheckbox().setEnabled(enabled);
	}

	void restoreRenderOptionsSize() {
		getRenderOptions().setRenderWidth(getOriginalRenderWidth());
		getRenderOptions().setRenderHeight(getOriginalRenderHeight());
	}

	public void addObserver(RenderOptionsPanelObserver observer) {
		getObservers().add(observer);
	}

	public void removeObserver(RenderOptionsPanelObserver observer) {
		getObservers().remove(observer);
	}

	protected void fireRenderOptionsChangedEvent() {
		for (RenderOptionsPanelObserver observer : getObservers()) {
			observer.renderOptionsChanged(getRenderOptions());
		}
	}

	protected RenderOptions getRenderOptions() {
		return renderOptions;
	}

	private void setRenderOptions(RenderOptions renderOptions) {
		this.renderOptions = renderOptions;
	}

	private int getOriginalRenderWidth() {
		return originalRenderWidth;
	}

	private void setOriginalRenderWidth(int originalRenderWidth) {
		this.originalRenderWidth = originalRenderWidth;
	}

	private int getOriginalRenderHeight() {
		return originalRenderHeight;
	}

	private void setOriginalRenderHeight(int originalRenderHeight) {
		this.originalRenderHeight = originalRenderHeight;
	}

	private ButtonGroup getMagnificationButtonGroup() {
		return magnificationButtonGroup;
	}

	private RenderOptionCheckbox getShadowsCheckbox() {
		return shadowsCheckbox;
	}

	private RenderOptionCheckbox getBackdropCheckbox() {
		return backdropCheckbox;
	}

	private RenderOptionCheckbox getSuperSamplingCheckbox() {
		return superSamplingCheckbox;
	}

	private RenderOptionCheckbox getDepthBlurCheckbox() {
		return depthBlurCheckbox;
	}

	protected Collection<RenderOptionsPanelObserver> getObservers() {
		return observers;
	}

	public static interface RenderOptionsPanelObserver {

		void renderOptionsChanged(RenderOptions renderOptions);

	}

	private static class RenderOptionCheckbox extends JCheckBox {

		public RenderOptionCheckbox(Action action) {
			this(action, false);
		}

		public RenderOptionCheckbox(Action action, boolean selected) {
			super(action);
			setSelected(selected);
			setFocusPainted(false);
		}

	}

	private class ShadowsAction extends AbstractAction {

		public ShadowsAction() {
			super(RenderUIResources.shadowsLabel);
			putValue(Action.SHORT_DESCRIPTION, RenderUIResources.shadowsToolTipText);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getRenderOptions().setShadowsEnabled(getShadowsCheckbox().isSelected());
			fireRenderOptionsChangedEvent();
		}

	}

	private class BackdropAction extends AbstractAction {

		public BackdropAction() {
			super(RenderUIResources.backdropLabel);
			putValue(Action.SHORT_DESCRIPTION, RenderUIResources.backdropToolTipText);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getRenderOptions().setBackdropEnabled(getBackdropCheckbox().isSelected());
			fireRenderOptionsChangedEvent();
		}

	}

	private class SuperSamplingAction extends AbstractAction {

		public SuperSamplingAction() {
			super(RenderUIResources.superSamplingLabel);
			putValue(Action.SHORT_DESCRIPTION, RenderUIResources.superSamplingToolTipText);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getRenderOptions().setSuperSamplingEnabled(getSuperSamplingCheckbox().isSelected());
			fireRenderOptionsChangedEvent();
		}

	}

	private class DepthBlurAction extends AbstractAction {

		public DepthBlurAction() {
			super(RenderUIResources.depthBlurLabel);
			putValue(Action.SHORT_DESCRIPTION, RenderUIResources.depthBlurToolTipText);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getRenderOptions().setDepthBlurEnabled(getDepthBlurCheckbox().isSelected());
			fireRenderOptionsChangedEvent();
		}

	}

	private static class MagnificationButton extends JToggleButton {

		public MagnificationButton(MagnificationAction action) {
			super(action);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
					BorderFactory.createEmptyBorder(6, 3, 6, 3)));
			setFocusPainted(false);
		}

	}

	private abstract class MagnificationAction extends AbstractAction {

		protected MagnificationAction(String name) {
			super(name);
		}

		protected MagnificationAction(Icon icon) {
			super("", icon);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			int factor = getMagnificationFactor();
			getRenderOptions().setRenderWidth(factor * getOriginalRenderWidth());
			getRenderOptions().setRenderHeight(factor * getOriginalRenderHeight());
			fireRenderOptionsChangedEvent();
		}

		protected abstract int getMagnificationFactor();

		protected void setToolTipText(String text) {
			putValue(Action.SHORT_DESCRIPTION, text);
		}

	}

	private class OriginalMagnificationAction extends MagnificationAction {

		public OriginalMagnificationAction() {
			super(RenderUIResources.magnifyOriginalIcon);
			setToolTipText(RenderUIResources.magnifyOriginalToolTipText);
		}

		@Override
		protected int getMagnificationFactor() {
			return 1;
		}

	}

	private class DoubleMagnificationAction extends MagnificationAction {

		public DoubleMagnificationAction() {
			super(RenderUIResources.magnifyDoubleIcon);
			setToolTipText(RenderUIResources.magnifyDoubleToolTipText);
		}

		@Override
		protected int getMagnificationFactor() {
			return 2;
		}

	}

	private class TripleMagnificationAction extends MagnificationAction {

		public TripleMagnificationAction() {
			super(RenderUIResources.magnifyTripleIcon);
			setToolTipText(RenderUIResources.magnifyTripleToolTipText);
		}

		@Override
		protected int getMagnificationFactor() {
			return 3;
		}

	}

}
