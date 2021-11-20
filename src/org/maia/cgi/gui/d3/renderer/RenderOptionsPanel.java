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

import org.maia.cgi.gui.d3.renderer.RenderOptions.SamplingMode;

@SuppressWarnings("serial")
public class RenderOptionsPanel extends Box {

	private RenderOptions renderOptions;

	private int originalRenderWidth;

	private int originalRenderHeight;

	private ButtonGroup magnificationButtonGroup;

	private ButtonGroup samplingButtonGroup;

	private RenderOptionCheckbox shadowsCheckbox;

	private RenderOptionCheckbox backdropCheckbox;

	private RenderOptionCheckbox depthBlurCheckbox;

	private RenderOptionCheckbox depthDarknessCheckbox;

	private Collection<RenderOptionsPanelObserver> observers;

	public RenderOptionsPanel() {
		this(RenderOptions.createDefaultOptions());
	}

	public RenderOptionsPanel(RenderOptions renderOptions) {
		super(BoxLayout.Y_AXIS);
		this.magnificationButtonGroup = createMagnificationButtonGroup();
		this.samplingButtonGroup = createSamplingButtonGroup();
		this.shadowsCheckbox = createShadowsCheckbox();
		this.backdropCheckbox = createBackdropCheckbox();
		this.depthBlurCheckbox = createDepthBlurCheckbox();
		this.depthDarknessCheckbox = createDepthDarknessCheckbox();
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

	protected ButtonGroup createSamplingButtonGroup() {
		ButtonGroup group = new ButtonGroup();
		group.add(new SamplingButton(new DirectSamplingAction()));
		group.add(new SamplingButton(new SuperSamplingAction()));
		group.add(new SamplingButton(new UltraSamplingAction()));
		return group;
	}

	protected RenderOptionCheckbox createShadowsCheckbox() {
		return new RenderOptionCheckbox(new ShadowsAction());
	}

	protected RenderOptionCheckbox createBackdropCheckbox() {
		return new RenderOptionCheckbox(new BackdropAction());
	}

	protected RenderOptionCheckbox createDepthBlurCheckbox() {
		return new RenderOptionCheckbox(new DepthBlurAction());
	}

	protected RenderOptionCheckbox createDepthDarknessCheckbox() {
		return new RenderOptionCheckbox(new DepthDarknessAction());
	}

	protected void buildUI() {
		add(buildMagnificationButtonPanel());
		add(Box.createVerticalStrut(16));
		add(buildSamplingButtonPanel());
		add(Box.createVerticalStrut(16));
		add(getShadowsCheckbox());
		add(getDepthBlurCheckbox());
		add(getDepthDarknessCheckbox());
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

	protected JComponent buildSamplingButtonPanel() {
		Box box = new Box(BoxLayout.X_AXIS);
		Enumeration<AbstractButton> buttons = getSamplingButtonGroup().getElements();
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
		for (Enumeration<AbstractButton> e = getSamplingButtonGroup().getElements(); e.hasMoreElements();) {
			AbstractButton button = e.nextElement();
			if (((SamplingAction) button.getAction()).getSamplingMode().equals(renderOptions.getSamplingMode()))
				button.setSelected(true);
		}
		getShadowsCheckbox().setSelected(renderOptions.isShadowsEnabled());
		getBackdropCheckbox().setSelected(renderOptions.isBackdropEnabled());
		getDepthBlurCheckbox().setSelected(renderOptions.isDepthBlurEnabled());
		getDepthDarknessCheckbox().setSelected(renderOptions.isDepthDarknessEnabled());
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Enumeration<AbstractButton> e = getMagnificationButtonGroup().getElements(); e.hasMoreElements();) {
			e.nextElement().setEnabled(enabled);
		}
		for (Enumeration<AbstractButton> e = getSamplingButtonGroup().getElements(); e.hasMoreElements();) {
			e.nextElement().setEnabled(enabled);
		}
		getShadowsCheckbox().setEnabled(enabled);
		getBackdropCheckbox().setEnabled(enabled);
		getDepthBlurCheckbox().setEnabled(enabled);
		getDepthDarknessCheckbox().setEnabled(enabled);
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

	private ButtonGroup getSamplingButtonGroup() {
		return samplingButtonGroup;
	}

	private RenderOptionCheckbox getShadowsCheckbox() {
		return shadowsCheckbox;
	}

	private RenderOptionCheckbox getBackdropCheckbox() {
		return backdropCheckbox;
	}

	private RenderOptionCheckbox getDepthBlurCheckbox() {
		return depthBlurCheckbox;
	}

	private RenderOptionCheckbox getDepthDarknessCheckbox() {
		return depthDarknessCheckbox;
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

	private class DepthDarknessAction extends AbstractAction {

		public DepthDarknessAction() {
			super(RenderUIResources.depthDarknessLabel);
			putValue(Action.SHORT_DESCRIPTION, RenderUIResources.depthDarknessToolTipText);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getRenderOptions().setDepthDarknessEnabled(getDepthDarknessCheckbox().isSelected());
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

	private static class SamplingButton extends JToggleButton {

		public SamplingButton(SamplingAction action) {
			super(action);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
					BorderFactory.createEmptyBorder(6, 3, 6, 3)));
			setFocusPainted(false);
		}

	}

	private abstract class SamplingAction extends AbstractAction {

		protected SamplingAction(String name) {
			super(name);
		}

		protected SamplingAction(Icon icon) {
			super("", icon);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getRenderOptions().setSamplingMode(getSamplingMode());
			fireRenderOptionsChangedEvent();
		}

		protected abstract SamplingMode getSamplingMode();

		protected void setToolTipText(String text) {
			putValue(Action.SHORT_DESCRIPTION, text);
		}

	}

	private class DirectSamplingAction extends SamplingAction {

		public DirectSamplingAction() {
			super(RenderUIResources.sampleDirectIcon);
			setToolTipText(RenderUIResources.sampleDirectToolTipText);
		}

		@Override
		protected SamplingMode getSamplingMode() {
			return SamplingMode.DIRECT;
		}

	}

	private class SuperSamplingAction extends SamplingAction {

		public SuperSamplingAction() {
			super(RenderUIResources.sampleSuperIcon);
			setToolTipText(RenderUIResources.sampleSuperToolTipText);
		}

		@Override
		protected SamplingMode getSamplingMode() {
			return SamplingMode.SUPER;
		}

	}

	private class UltraSamplingAction extends SamplingAction {

		public UltraSamplingAction() {
			super(RenderUIResources.sampleUltraIcon);
			setToolTipText(RenderUIResources.sampleUltraToolTipText);
		}

		@Override
		protected SamplingMode getSamplingMode() {
			return SamplingMode.ULTRA;
		}

	}

}
