package org.maia.cgi;

public class Metrics {

	private static Metrics instance;

	private long pointTransformations;

	private long pointNormalizations;

	private long matrixMultiplications;

	private long matrixInversions;

	private long vectorDotProducts;

	private long vectorCrossProducts;

	private long vectorNormalizations;

	private long vectorAnglesInBetween;

	private long lineWithPlaneIntersections;

	private long lineWithLineIntersections;

	private long eyeRayWithObjectIntersections;

	private long eyeRayWithObjectHits;

	private long lightRayWithObjectIntersections;

	private long lightRayWithObjectHits;

	private long boundingBoxComputations;

	private long pointInsidePolygonChecks;

	private long surfacePositionToLightSourceTraversals;

	private Metrics() {
		resetCounters();
	}

	public static Metrics getInstance() {
		if (instance == null) {
			setInstance(new Metrics());
		}
		return instance;
	}

	private static synchronized void setInstance(Metrics metrics) {
		if (instance == null) {
			instance = metrics;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Metrics {\n");
		builder.append("\tmatrixMultiplications: ").append(matrixMultiplications).append("\n");
		builder.append("\tmatrixInversions: ").append(matrixInversions).append("\n");
		builder.append("\tpointTransformations: ").append(pointTransformations).append("\n");
		builder.append("\tpointNormalizations: ").append(pointNormalizations).append("\n");
		builder.append("\tpointInsidePolygonChecks: ").append(pointInsidePolygonChecks).append("\n");
		builder.append("\tvectorDotProducts: ").append(vectorDotProducts).append("\n");
		builder.append("\tvectorCrossProducts: ").append(vectorCrossProducts).append("\n");
		builder.append("\tvectorNormalizations: ").append(vectorNormalizations).append("\n");
		builder.append("\tvectorAnglesInBetween: ").append(vectorAnglesInBetween).append("\n");
		builder.append("\t---\n");
		builder.append("\tlineWithLineIntersections: ").append(lineWithLineIntersections).append("\n");
		builder.append("\tlineWithPlaneIntersections: ").append(lineWithPlaneIntersections).append("\n");
		builder.append("\tboundingBoxComputations: ").append(boundingBoxComputations).append("\n");
		builder.append("\t---\n");
		builder.append("\teyeRayWithObjectIntersections: ").append(eyeRayWithObjectIntersections).append("\n");
		builder.append("\teyeRayWithObjectHits: ").append(eyeRayWithObjectHits).append("\n");
		builder.append("\tsurfacePositionToLightSourceTraversals: ").append(surfacePositionToLightSourceTraversals)
				.append("\n");
		builder.append("\tlightRayWithObjectIntersections: ").append(lightRayWithObjectIntersections).append("\n");
		builder.append("\tlightRayWithObjectHits: ").append(lightRayWithObjectHits).append("\n");
		builder.append("}");
		return builder.toString();
	}

	public void resetCounters() {
		pointTransformations = 0;
		pointNormalizations = 0;
		matrixMultiplications = 0;
		matrixInversions = 0;
		vectorDotProducts = 0;
		vectorCrossProducts = 0;
		vectorNormalizations = 0;
		vectorAnglesInBetween = 0;
		lineWithPlaneIntersections = 0;
		lineWithLineIntersections = 0;
		eyeRayWithObjectIntersections = 0;
		eyeRayWithObjectHits = 0;
		lightRayWithObjectIntersections = 0;
		lightRayWithObjectHits = 0;
		boundingBoxComputations = 0;
		pointInsidePolygonChecks = 0;
		surfacePositionToLightSourceTraversals = 0;
	}

	public void incrementPointTransformations() {
		pointTransformations++;
	}

	public void incrementPointNormalizations() {
		pointNormalizations++;
	}

	public void incrementMatrixMultiplications() {
		matrixMultiplications++;
	}

	public void incrementMatrixInversions() {
		matrixInversions++;
	}

	public void incrementVectorDotProducts() {
		vectorDotProducts++;
	}

	public void incrementVectorCrossProducts() {
		vectorCrossProducts++;
	}

	public void incrementVectorNormalizations() {
		vectorNormalizations++;
	}

	public void incrementVectorAnglesInBetween() {
		vectorAnglesInBetween++;
	}

	public void incrementLineWithPlaneIntersections() {
		lineWithPlaneIntersections++;
	}

	public void incrementLineWithLineIntersections() {
		lineWithLineIntersections++;
	}

	public void incrementEyeRayWithObjectIntersections() {
		eyeRayWithObjectIntersections++;
	}

	public void incrementEyeRayWithObjectHits() {
		eyeRayWithObjectHits++;
	}

	public void incrementLightRayWithObjectIntersections() {
		lightRayWithObjectIntersections++;
	}

	public void incrementLightRayWithObjectHits() {
		lightRayWithObjectHits++;
	}

	public void incrementBoundingBoxComputations() {
		boundingBoxComputations++;
	}

	public void incrementPointInsidePolygonChecks() {
		pointInsidePolygonChecks++;
	}

	public void incrementSurfacePositionToLightSourceTraversals() {
		surfacePositionToLightSourceTraversals++;
	}

	public long getPointTransformations() {
		return pointTransformations;
	}

	public long getPointNormalizations() {
		return pointNormalizations;
	}

	public long getMatrixMultiplications() {
		return matrixMultiplications;
	}

	public long getMatrixInversions() {
		return matrixInversions;
	}

	public long getVectorDotProducts() {
		return vectorDotProducts;
	}

	public long getVectorCrossProducts() {
		return vectorCrossProducts;
	}

	public long getVectorNormalizations() {
		return vectorNormalizations;
	}

	public long getVectorAnglesInBetween() {
		return vectorAnglesInBetween;
	}

	public long getLineWithPlaneIntersections() {
		return lineWithPlaneIntersections;
	}

	public long getLineWithLineIntersections() {
		return lineWithLineIntersections;
	}

	public long getEyeRayWithObjectIntersections() {
		return eyeRayWithObjectIntersections;
	}

	public long getEyeRayWithObjectHits() {
		return eyeRayWithObjectHits;
	}

	public long getLightRayWithObjectIntersections() {
		return lightRayWithObjectIntersections;
	}

	public long getLightRayWithObjectHits() {
		return lightRayWithObjectHits;
	}

	public long getBoundingBoxComputations() {
		return boundingBoxComputations;
	}

	public long getPointInsidePolygonChecks() {
		return pointInsidePolygonChecks;
	}

	public long getSurfacePositionToLightSourceTraversals() {
		return surfacePositionToLightSourceTraversals;
	}

}
