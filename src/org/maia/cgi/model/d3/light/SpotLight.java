package org.maia.cgi.model.d3.light;

import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.model.d3.camera.Camera;
import org.maia.cgi.model.d3.scene.Scene;

public class SpotLight extends BaseLight implements PositionalLightSource {

	private Point3D positionInWorld; // in world coordinates

	private boolean stationary;

	private Point3D positionInCamera;

	public SpotLight(Point3D positionInWorld) {
		this(positionInWorld, 1.0);
	}

	public SpotLight(Point3D positionInWorld, double brightness) {
		this(positionInWorld, brightness, false);
	}

	public SpotLight(Point3D positionInWorld, double brightness, boolean stationary) {
		super(brightness);
		this.positionInWorld = positionInWorld;
		this.stationary = stationary;
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		invalidatePositionInCamera();
	}

	@Override
	public Point3D getPositionInCamera(Scene scene) {
		if (positionInCamera == null) {
			positionInCamera = derivePositionInCamera(scene);
		}
		return positionInCamera;
	}

	private void invalidatePositionInCamera() {
		positionInCamera = null;
	}

	private Point3D derivePositionInCamera(Scene scene) {
		Point3D position = getPositionInWorld();
		if (!isStationary()) {
			position = scene.getCamera().getViewingMatrix().transform(position);
		}
		return position;
	}

	@Override
	public Point3D getPositionInWorld() {
		return positionInWorld;
	}

	@Override
	public boolean isStationary() {
		return stationary;
	}

}