package org.maia.cgi.model.d3.light;

import org.maia.cgi.geometry.d3.Vector3D;
import org.maia.cgi.model.d3.scene.Scene;

public class InboundLight extends BaseLight implements DirectionalLightSource {

	private Vector3D direction;

	private Vector3D scaledDirection;

	private Scene scaledDirectionScene;

	public InboundLight(Vector3D direction) {
		this(direction, 1.0);
	}

	public InboundLight(Vector3D direction, double brightness) {
		super(brightness);
		this.direction = direction.getUnitVector();
	}

	@Override
	public Vector3D getScaledDirectionOutsideOfScene(Scene scene) {
		if (scaledDirection == null || !scene.equals(scaledDirectionScene)) {
			Vector3D v = getDirection().getUnitVector();
			v.scale(scene.getDistanceOutsideScene());
			scaledDirection = v;
			scaledDirectionScene = scene;
		}
		return scaledDirection;
	}

	@Override
	public Vector3D getDirection() {
		return direction;
	}

}