package org.maia.cgi.gui.d3.renderer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class RenderUIResources {

	private static String iconFolder = "/icons/";

	public static final String frameTitle = "3D Renderer";

	public static final String fileMenuTitle = "File";

	public static final String fileMenuItemExit = "Exit";

	public static final String sceneSelectionMenuTitle = "Open scene";

	public static final String renderMenuTitle = "Rendering";

	public static final String extraMenuTitle = "Extra";

	public static final String extraMenuItemShowMetrics = "Show metrics";

	public static final String extraMenuItemAbout = "About";

	public static final Icon cameraIcon = loadIcon(iconFolder + "camera32.png");

	public static final Icon renderIcon = loadIcon(iconFolder + "brushes48.png");

	public static final String renderLabel = "Render";

	public static final String renderToolTipText = "Renders the 3D model on the canvas";

	public static final Icon exportIcon = loadIcon(iconFolder + "download32.png");

	public static final String exportLabel = "Export image...";

	public static final String exportToolTipText = "Export the canvas to an image file";

	public static final String exportFileChooserDialogTitle = "Export canvas to file";

	public static final String exportFileChooserFilterName = "PNG Images (*.png)";

	public static final Icon repeatIcon = loadIcon(iconFolder + "repeat32.png");

	public static final String repeatToolTipText = "Automatic repeat on/off. To be used in combination with a camera movement button";

	public static final Icon revolveLongitudinalIcon = loadIcon(iconFolder + "revlonp32.png");

	public static final String revolveLongitudinalToolTipText = "Orbit camera by longitude";

	public static final Icon revolveLongitudinalReverseIcon = loadIcon(iconFolder + "revlonn32.png");

	public static final String revolveLongitudinalReverseToolTipText = "Orbit camera by reverse longitude";

	public static final Icon revolveLatitudinalIcon = loadIcon(iconFolder + "revlatp32.png");

	public static final String revolveLatitudinalToolTipText = "Orbit camera by latitude";

	public static final Icon revolveLatitudinalReverseIcon = loadIcon(iconFolder + "revlatn32.png");

	public static final String revolveLatitudinalReverseToolTipText = "Orbit camera by reverse latitude";

	public static final Icon revolveCloserIcon = loadIcon(iconFolder + "revclose32.png");

	public static final String revolveCloserToolTipText = "Move camera to a closer orbit";

	public static final Icon revolveFartherIcon = loadIcon(iconFolder + "revfar32.png");

	public static final String revolveFartherToolTipText = "Move camera to a more distant orbit";

	public static final Icon slideRightIcon = loadIcon(iconFolder + "slideright32.png");

	public static final String slideRightToolTipText = "Slide camera to the right";

	public static final Icon slideLeftIcon = loadIcon(iconFolder + "slideleft32.png");

	public static final String slideLeftToolTipText = "Slide camera to the left";

	public static final Icon slideUpIcon = loadIcon(iconFolder + "slideup32.png");

	public static final String slideUpToolTipText = "Slide camera upwards";

	public static final Icon slideDownIcon = loadIcon(iconFolder + "slidedown32.png");

	public static final String slideDownToolTipText = "Slide camera downwards";

	public static final Icon slideBackwardIcon = loadIcon(iconFolder + "slideback32.png");

	public static final String slideBackwardToolTipText = "Slide camera backwards";

	public static final Icon slideForwardIcon = loadIcon(iconFolder + "slidefront32.png");

	public static final String slideForwardToolTipText = "Slide camera forwards";

	public static final Icon rotatePitchIcon = loadIcon(iconFolder + "pitchp32.png");

	public static final String rotatePitchToolTipText = "Pitch camera upwards";

	public static final Icon rotatePitchReverseIcon = loadIcon(iconFolder + "pitchn32.png");

	public static final String rotatePitchReverseToolTipText = "Pitch camera downwards";

	public static final Icon rotateYawIcon = loadIcon(iconFolder + "yawp32.png");

	public static final String rotateYawToolTipText = "Yaw camera leftwards";

	public static final Icon rotateYawReverseIcon = loadIcon(iconFolder + "yawn32.png");

	public static final String rotateYawReverseToolTipText = "Yaw camera rightwards";

	public static final Icon rotateRollIcon = loadIcon(iconFolder + "rollp32.png");

	public static final String rotateRollToolTipText = "Roll camera counter-clockwise";

	public static final Icon rotateRollReverseIcon = loadIcon(iconFolder + "rolln32.png");

	public static final String rotateRollReverseToolTipText = "Roll camera clockwise";

	public static final String incrementSliderToolTipText = "Controls the step size for all camera movements";

	public static final String shadowsLabel = "Shadows";

	public static final String shadowsToolTipText = "Renders shadows cast by light sources";

	public static final String superSamplingLabel = "Super-sampling";

	public static final String superSamplingToolTipText = "Produces a higher quality rendering by interpolating a set of sample points per pixel";

	public static final String depthBlurLabel = "Depth blur";

	public static final String depthBlurToolTipText = "Blurs distant surfaces as if they are out of camera focus";

	public static final Icon magnifyOriginalIcon = loadIcon(iconFolder + "magnify-s.png");

	public static final String magnifyOriginalToolTipText = "Canvas size 1x1";

	public static final Icon magnifyDoubleIcon = loadIcon(iconFolder + "magnify-m.png");

	public static final String magnifyDoubleToolTipText = "Canvas size 2x2";

	public static final Icon magnifyTripleIcon = loadIcon(iconFolder + "magnify-l.png");

	public static final String magnifyTripleToolTipText = "Canvas size 3x3";

	public static final Icon usageCpuIcon = loadIcon(iconFolder + "cpu16.png");

	public static final Icon usageMemoryIcon = loadIcon(iconFolder + "ram16.png");

	public static final String metricsDialogTitle = "Metrics";

	public static final String metricsModelTabTitle = "Model";

	public static final String metricsComputeTabTitle = "Compute";

	public static final String metricsComputeDescription = "Compute metrics from the last drawing on canvas";

	public static final String aboutDialogTitle = "About";

	public static final Icon aboutLogoIcon = loadIcon(iconFolder + "3drenderer.png");

	private static Icon loadIcon(String resourceName) {
		return new ImageIcon(RenderUIResources.class.getResource(resourceName));
	}

}