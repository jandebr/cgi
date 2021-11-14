package org.maia.cgi.render.d3.raytrace;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.maia.cgi.compose.Compositing;
import org.maia.cgi.compose.d2.Convolution;
import org.maia.cgi.compose.d2.ConvolutionMatrix;
import org.maia.cgi.compose.d3.DepthBlurOperation.DepthBlurOperationProgressTracker;
import org.maia.cgi.compose.d3.DepthBlurParameters;
import org.maia.cgi.geometry.d2.Rectangle2D;
import org.maia.cgi.geometry.d3.LineSegment3D;
import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.metrics.Metrics;
import org.maia.cgi.model.d3.camera.ViewVolume;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3D;
import org.maia.cgi.model.d3.object.ObjectSurfacePoint3DImpl;
import org.maia.cgi.model.d3.object.RaytraceableObject3D;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.model.d3.scene.SceneUtils;
import org.maia.cgi.render.d3.BaseSceneRenderer;
import org.maia.cgi.render.d3.view.ColorDepthBuffer;
import org.maia.cgi.render.d3.view.ViewPort;
import org.maia.cgi.shading.d2.TextureMapRegistry;

public class RaytraceRenderer extends BaseSceneRenderer {

	private int pixelWidth;

	private int pixelHeight;

	private int samplesPerPixelX;

	private int samplesPerPixelY;

	private DepthBlurParameters depthBlurParams;

	public RaytraceRenderer(int pixelWidth, int pixelHeight, int samplesPerPixelX, int samplesPerPixelY) {
		this(pixelWidth, pixelHeight, samplesPerPixelX, samplesPerPixelY, null);
	}

	public RaytraceRenderer(int pixelWidth, int pixelHeight, int samplesPerPixelX, int samplesPerPixelY,
			DepthBlurParameters depthBlurParams) {
		setPixelWidth(pixelWidth);
		setPixelHeight(pixelHeight);
		setSamplesPerPixelX(samplesPerPixelX);
		setSamplesPerPixelY(samplesPerPixelY);
		setDepthBlurParams(depthBlurParams);
	}

	@Override
	protected void renderImpl(Scene scene, Collection<ViewPort> outputs) {
		RenderState state = new RenderState(scene);
		System.out.println(state);
		System.out.println(scene.getSpatialIndex().getBinStatistics());
		state.incrementStep();
		renderRaster(state, outputs);
		if (getDepthBlurParams() != null) {
			state.incrementStep();
			applyDepthBlur(state, outputs);
		}
		System.out.println(Metrics.getInstance());
	}

	private synchronized void renderRaster(RenderState state, Collection<ViewPort> outputs) {
		ThreadGroup workers = new ThreadGroup("Raytrace workers");
		int n = state.getScene().getRenderParameters().getSafeNumberOfRenderThreads();
		System.out.println("Spawning " + n + " raytrace worker" + (n > 1 ? "s" : ""));
		for (int i = 0; i < n; i++) {
			Thread t = new Thread(workers, new RenderRasterWorker(state, outputs), "Raytrace worker #" + i);
			t.setDaemon(true);
			t.start();
			state.setActiveRenderRasterWorkers(state.getActiveRenderRasterWorkers() + 1);
		}
		while (state.getActiveRenderRasterWorkers() > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	private synchronized void notifyRenderRasterWorkerCompletion(RenderRasterWorker worker) {
		RenderState state = worker.getState();
		state.setActiveRenderRasterWorkers(state.getActiveRenderRasterWorkers() - 1);
		notifyAll();
	}

	private void applyDepthBlur(RenderState state, Collection<ViewPort> outputs) {
		// Blur by depth
		ColorDepthBuffer raster = state.getRaster();
		int sppx = getSamplesPerPixelX();
		int sppy = getSamplesPerPixelY();
		DepthBlurParameters params = getDepthBlurParams().clone();
		params.setMaxBlurPixelRadius(params.getMaxBlurPixelRadius() * Math.max(sppx, sppy)); // radius in samples
		raster.replaceImage(Compositing.blurImageByDepth(raster, params, new DepthBlurTracker(state)));
		// Update outputs
		state.incrementStep();
		for (ViewPort output : outputs) {
			output.clear();
		}
		ConvolutionMatrix avgMatrix = state.getPixelAveragingConvolutionMatrix();
		int pw = getPixelWidth();
		int ph = getPixelHeight();
		for (int iy = 0; iy < ph; iy++) {
			for (int ix = 0; ix < pw; ix++) {
				if (getSamplesPerPixel() == 1) {
					renderPixelAtViewPorts(ix, iy, raster.getColor(ix, iy), outputs);
				} else {
					renderPixelAtViewPorts(ix, iy, raster.convoluteColor(ix * sppx, iy * sppy, avgMatrix), outputs);
				}
			}
			fireRenderingProgressUpdate(state.getScene(), state.getCurrentStep(), (iy + 1.0) / ph,
					state.getTotalSteps());
		}
	}

	private void renderPixelAtViewPorts(int ix, int iy, Color color, Collection<ViewPort> outputs) {
		for (ViewPort output : outputs) {
			output.paintPixelInWindowCoordinates(ix, iy, color);
		}
	}

	public int getPixelWidth() {
		return pixelWidth;
	}

	public void setPixelWidth(int pixelWidth) {
		this.pixelWidth = pixelWidth;
	}

	public int getPixelHeight() {
		return pixelHeight;
	}

	public void setPixelHeight(int pixelHeight) {
		this.pixelHeight = pixelHeight;
	}

	public int getSamplesPerPixel() {
		return getSamplesPerPixelX() * getSamplesPerPixelY();
	}

	public int getSamplesPerPixelX() {
		return samplesPerPixelX;
	}

	public void setSamplesPerPixelX(int samples) {
		this.samplesPerPixelX = samples;
	}

	public int getSamplesPerPixelY() {
		return samplesPerPixelY;
	}

	public void setSamplesPerPixelY(int samples) {
		this.samplesPerPixelY = samples;
	}

	public DepthBlurParameters getDepthBlurParams() {
		return depthBlurParams;
	}

	public void setDepthBlurParams(DepthBlurParameters depthBlurParams) {
		this.depthBlurParams = depthBlurParams;
	}

	private class RenderState {

		private Scene scene;

		private Rectangle2D viewPlaneBounds;

		private double viewPlaneZ;

		private ColorDepthBuffer raster;

		private ConvolutionMatrix pixelAveragingConvolutionMatrix;

		private RaytraceableObjectViewPlaneIndex objectIndex;

		private int currentStep;

		private int totalSteps;

		private int nextRenderLineIndex;

		private int activeRenderRasterWorkers;

		public RenderState(Scene scene) {
			ViewVolume vv = scene.getCamera().getViewVolume();
			this.scene = scene;
			this.viewPlaneBounds = vv.getViewPlaneRectangle();
			this.viewPlaneZ = vv.getViewPlaneZ();
			this.raster = new ColorDepthBuffer(getPixelWidth() * getSamplesPerPixelX(), getPixelHeight()
					* getSamplesPerPixelY(), scene.getRenderParameters().getAmbientColor());
			this.pixelAveragingConvolutionMatrix = Convolution.getScaledGaussianBlurMatrix(getSamplesPerPixelY(),
					getSamplesPerPixelX(), 2.0);
			int xBins = Math.min((int) Math.ceil(getPixelWidth() * getSamplesPerPixelX() / 8), 500);
			int yBins = Math.min((int) Math.ceil(getPixelHeight() * getSamplesPerPixelY() / 8), 500);
			this.objectIndex = new RaytraceableObjectViewPlaneIndex(scene.getCamera(), xBins, yBins);
			this.currentStep = 0;
			this.totalSteps = getDepthBlurParams() == null ? 1 : 3;
			this.nextRenderLineIndex = 0;
			this.activeRenderRasterWorkers = 0;
			init();
		}

		private void init() {
			getObjectIndex().addAllRaytraceableObjectsFromScene(getScene());
			int n = getObjectIndex().getBinStatistics().getMaximumObjectsPerBinRow();
			int c = TextureMapRegistry.getInstance().getCapacity();
			if (c < n) {
				System.err
						.println("WARNING: Texture map swapping could occur. Consider increasing TextureMapRegistry's capacity (now "
								+ c + "). A safe value would be " + n);
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("RenderState {\n");
			sb.append("\tObjects {\n");
			sb.append("\t\tTop level objects: ").append(getScene().getTopLevelObjects().size()).append("\n");
			sb.append("\t\tRaytraceable objects: ")
					.append(SceneUtils.getAllRaytraceableObjectsInScene(getScene()).size()).append("\n");
			sb.append("\t}\n");
			sb.append("\tCamera {\n");
			sb.append("\t\tPosition: ").append(getScene().getCamera().getPosition()).append("\n");
			sb.append("\t}\n");
			sb.append("\tView plane {\n");
			sb.append("\t\tXY bounds: ").append(getViewPlaneBounds()).append("\n");
			sb.append("\t\tZ: ").append(getViewPlaneZ()).append("\n");
			sb.append("\t\t").append(getObjectIndex().getBinStatistics().toString().replace("\n", "\n\t\t"))
					.append("\n");
			sb.append("\t}\n");
			sb.append("}");
			return sb.toString();
		}

		public void incrementStep() {
			currentStep++;
		}

		public synchronized boolean hasNextRenderLine() {
			return getNextRenderLineIndex() < getPixelHeight();
		}

		public synchronized int nextRenderLine() {
			if (!hasNextRenderLine())
				throw new NoSuchElementException("All lines were rendered");
			int index = getNextRenderLineIndex();
			setNextRenderLineIndex(index + 1);
			return index;
		}

		public Scene getScene() {
			return scene;
		}

		public Rectangle2D getViewPlaneBounds() {
			return viewPlaneBounds;
		}

		public double getViewPlaneZ() {
			return viewPlaneZ;
		}

		public ColorDepthBuffer getRaster() {
			return raster;
		}

		public ConvolutionMatrix getPixelAveragingConvolutionMatrix() {
			return pixelAveragingConvolutionMatrix;
		}

		private RaytraceableObjectViewPlaneIndex getObjectIndex() {
			return objectIndex;
		}

		public int getCurrentStep() {
			return currentStep;
		}

		public int getTotalSteps() {
			return totalSteps;
		}

		public double getRasterRenderProgress() {
			return getNextRenderLineIndex() / (double) getPixelHeight();
		}

		private int getNextRenderLineIndex() {
			return nextRenderLineIndex;
		}

		private void setNextRenderLineIndex(int nextRenderLineIndex) {
			this.nextRenderLineIndex = nextRenderLineIndex;
		}

		public int getActiveRenderRasterWorkers() {
			return activeRenderRasterWorkers;
		}

		public void setActiveRenderRasterWorkers(int activeRenderRasterWorkers) {
			this.activeRenderRasterWorkers = activeRenderRasterWorkers;
		}

	}

	private class RenderRasterWorker implements Runnable {

		private RenderState state;

		private Collection<ViewPort> outputs;

		private List<ObjectSurfacePoint3D> intersections; // reusable

		private List<Color> colorList; // reusable

		public RenderRasterWorker(RenderState state, Collection<ViewPort> outputs) {
			this.state = state;
			this.outputs = outputs;
			this.intersections = new Vector<ObjectSurfacePoint3D>();
			this.colorList = new Vector<Color>();
		}

		@Override
		public void run() {
			RenderState state = getState();
			int pw = getPixelWidth();
			int ph = getPixelHeight();
			double vw = state.getViewPlaneBounds().getWidth();
			double vh = state.getViewPlaneBounds().getHeight();
			double vz = state.getViewPlaneZ();
			double vx0 = state.getViewPlaneBounds().getLeft();
			double vy0 = state.getViewPlaneBounds().getBottom();
			while (state.hasNextRenderLine()) {
				int iy = state.nextRenderLine();
				double vy = vy0 + (ph - iy - 0.5) / ph * vh;
				for (int ix = 0; ix < pw; ix++) {
					double vx = vx0 + (ix + 0.5) / pw * vw;
					Point3D viewPoint = new Point3D(vx, vy, vz);
					renderPixel(ix, iy, viewPoint);
				}
				fireRenderingProgressUpdate(state.getScene(), state.getCurrentStep(), state.getRasterRenderProgress(),
						state.getTotalSteps());
			}
			notifyRenderRasterWorkerCompletion(this);
		}

		private void renderPixel(int ix, int iy, Point3D viewPoint) {
			if (getSamplesPerPixel() == 1) {
				renderPixelWithoutSupersampling(ix, iy, viewPoint);
			} else {
				renderPixelBySupersampling(ix, iy, viewPoint);
			}
		}

		private void renderPixelWithoutSupersampling(int ix, int iy, Point3D viewPoint) {
			RenderState state = getState();
			ColorDepthBuffer raster = state.getRaster();
			LineSegment3D ray = new LineSegment3D(viewPoint, viewPoint.plus(viewPoint.minus(Point3D.origin())), true,
					false);
			Collection<ObjectSurfacePoint3D> intersections = getSceneIntersectionsWithRay(ray, ix, iy);
			if (!intersections.isEmpty()) {
				sortIntersectionsByDepth();
				raster.setColorAndDepth(ix, iy, getCombinedColor(), getNearestDepth());
			}
			renderPixelAtViewPorts(ix, iy, raster.getColor(ix, iy), getOutputs());
		}

		private void renderPixelBySupersampling(int ix, int iy, Point3D viewPoint) {
			RenderState state = getState();
			ColorDepthBuffer raster = state.getRaster();
			int sppx = getSamplesPerPixelX();
			int sppy = getSamplesPerPixelY();
			double pvw = state.getViewPlaneBounds().getWidth() / getPixelWidth(); // pixel view width
			double pvh = state.getViewPlaneBounds().getHeight() / getPixelHeight(); // pixel view height
			for (int si = 0; si < sppy; si++) {
				int iry = iy * sppy + si;
				double vsy = viewPoint.getY() + pvh / 2 - (si + 0.5) / sppy * pvh;
				for (int sj = 0; sj < sppx; sj++) {
					int irx = ix * sppx + sj;
					double vsx = viewPoint.getX() - pvw / 2 + (sj + 0.5) / sppx * pvw;
					Point3D samplePoint = new Point3D(vsx, vsy, viewPoint.getZ());
					LineSegment3D ray = new LineSegment3D(samplePoint, samplePoint.plus(samplePoint.minus(Point3D
							.origin())), true, false);
					Collection<ObjectSurfacePoint3D> intersections = getSceneIntersectionsWithRay(ray, ix, iy);
					if (!intersections.isEmpty()) {
						sortIntersectionsByDepth();
						raster.setColorAndDepth(irx, iry, getCombinedColor(), getNearestDepth());
					}
				}
			}
			renderPixelAtViewPorts(ix, iy,
					raster.convoluteColor(ix * sppx, iy * sppy, state.getPixelAveragingConvolutionMatrix()),
					getOutputs());
		}

		private Collection<ObjectSurfacePoint3D> getSceneIntersectionsWithRay(LineSegment3D ray, int ix, int iy) {
			Collection<ObjectSurfacePoint3D> intersections = getIntersections();
			intersections.clear();
			// From scene objects
			RenderState state = getState();
			Scene scene = state.getScene();
			Point3D pointOnViewPlane = ray.getP1();
			Collection<RaytraceableObject3D> objects = state.getObjectIndex().getObjects(pointOnViewPlane.getX(),
					pointOnViewPlane.getY());
			if (objects != null) {
				for (RaytraceableObject3D object : objects) {
					object.intersectWithRay(ray, scene, intersections, true);
				}
			}
			// From backdrop, if any
			ColorDepthBuffer backDrop = scene.getBackdrop();
			if (backDrop != null && scene.getRenderParameters().isBackdropEnabled()) {
				Color color = backDrop.getColor(ix, iy);
				double depth = backDrop.getDepth(ix, iy);
				double z = -depth;
				double zf = z / pointOnViewPlane.getZ();
				if (zf >= 1.0) {
					double x = pointOnViewPlane.getX() * zf;
					double y = pointOnViewPlane.getY() * zf;
					intersections.add(new ObjectSurfacePoint3DImpl(null, new Point3D(x, y, z), color));
				}
			}
			return intersections;
		}

		private void sortIntersectionsByDepth() {
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			if (intersections.size() > 1) {
				Collections.sort(intersections, SurfacePointSorterByDepth.instance);
			}
		}

		private Color getCombinedColor() {
			Color color = null;
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			if (intersections.size() == 1) {
				color = intersections.get(0).getColor();
			} else if (intersections.size() > 1) {
				List<Color> colors = getColorList();
				colors.clear();
				for (ObjectSurfacePoint3D intersection : intersections) {
					colors.add(intersection.getColor());
				}
				color = Compositing.combineColorsByTransparency(colors);
			}
			return color;
		}

		private double getNearestDepth() {
			double depth = 0;
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			if (!intersections.isEmpty()) {
				depth = -intersections.get(0).getPositionInCamera().getZ();
			}
			return depth;
		}

		private RenderState getState() {
			return state;
		}

		private Collection<ViewPort> getOutputs() {
			return outputs;
		}

		private List<ObjectSurfacePoint3D> getIntersections() {
			return intersections;
		}

		private List<Color> getColorList() {
			return colorList;
		}

	}

	private static class SurfacePointSorterByDepth implements Comparator<ObjectSurfacePoint3D> {

		public static final SurfacePointSorterByDepth instance = new SurfacePointSorterByDepth();

		@Override
		public int compare(ObjectSurfacePoint3D sp1, ObjectSurfacePoint3D sp2) {
			double z1 = sp1.getPositionInCamera().getZ();
			double z2 = sp2.getPositionInCamera().getZ();
			if (z1 == z2) {
				return 0;
			} else if (z1 > z2) {
				return -1;
			} else {
				return 1;
			}
		}

	}

	private class DepthBlurTracker implements DepthBlurOperationProgressTracker {

		private RenderState state;

		public DepthBlurTracker(RenderState state) {
			this.state = state;
		}

		@Override
		public void operationStarted() {
		}

		@Override
		public void operationUpdate(double progress) {
			RenderState state = getState();
			fireRenderingProgressUpdate(state.getScene(), state.getCurrentStep(), progress, state.getTotalSteps());
		}

		@Override
		public void operationCompleted() {
		}

		private RenderState getState() {
			return state;
		}

	}

}