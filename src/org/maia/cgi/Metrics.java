package org.maia.cgi;

import java.text.NumberFormat;

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

	private long eyeRayWithObjectIntersectionChecks;

	private long eyeRayWithObjectIntersections;

	private long lightRayWithObjectIntersectionChecks;

	private long lightRayWithObjectIntersections;

	private long boundingBoxComputations;

	private long pointInsidePolygonChecks;

	private long surfacePositionToLightSourceTraversals;

	private static NumberFormat numberFormat;

	static {
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setGroupingUsed(true);
	}

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
		builder.append("\tMatrix multiplications: ").append(format(matrixMultiplications)).append("\n");
		builder.append("\tMatrix inversions: ").append(format(matrixInversions)).append("\n");
		builder.append("\tPoint transformations: ").append(format(pointTransformations)).append("\n");
		builder.append("\tPoint normalizations: ").append(format(pointNormalizations)).append("\n");
		builder.append("\tPoint inside polygon checks: ").append(format(pointInsidePolygonChecks)).append("\n");
		builder.append("\tVector dot products: ").append(format(vectorDotProducts)).append("\n");
		builder.append("\tVector cross products: ").append(format(vectorCrossProducts)).append("\n");
		builder.append("\tVector normalizations: ").append(format(vectorNormalizations)).append("\n");
		builder.append("\tVector angles: ").append(format(vectorAnglesInBetween)).append("\n");
		builder.append("\t---\n");
		builder.append("\tLine with line intersections: ").append(format(lineWithLineIntersections)).append("\n");
		builder.append("\tLine with plane intersections: ").append(format(lineWithPlaneIntersections)).append("\n");
		builder.append("\tBounding box computations: ").append(format(boundingBoxComputations)).append("\n");
		builder.append("\t---\n");
		builder.append("\tEye ray object intersection checks: ").append(format(eyeRayWithObjectIntersectionChecks))
				.append("\n");
		builder.append("\tEye ray object intersections: ").append(format(eyeRayWithObjectIntersections)).append("\n");
		builder.append("\tPoint to light source traversals: ").append(format(surfacePositionToLightSourceTraversals))
				.append("\n");
		builder.append("\tLight ray object intersection checks: ").append(format(lightRayWithObjectIntersectionChecks))
				.append("\n");
		builder.append("\tLight ray object intersections: ").append(format(lightRayWithObjectIntersections))
				.append("\n");
		builder.append("}");
		return builder.toString();
	}

	public static String format(long value) {
		return numberFormat.format(value);
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
		eyeRayWithObjectIntersectionChecks = 0;
		eyeRayWithObjectIntersections = 0;
		lightRayWithObjectIntersectionChecks = 0;
		lightRayWithObjectIntersections = 0;
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

	public void incrementEyeRayWithObjectIntersectionChecks() {
		eyeRayWithObjectIntersectionChecks++;
	}

	public void incrementEyeRayWithObjectIntersections() {
		eyeRayWithObjectIntersections++;
	}

	public void incrementLightRayWithObjectIntersectionChecks() {
		lightRayWithObjectIntersectionChecks++;
	}

	public void incrementLightRayWithObjectIntersections() {
		lightRayWithObjectIntersections++;
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

	public long getEyeRayWithObjectIntersectionChecks() {
		return eyeRayWithObjectIntersectionChecks;
	}

	public long getEyeRayWithObjectIntersections() {
		return eyeRayWithObjectIntersections;
	}

	public long getLightRayWithObjectIntersectionChecks() {
		return lightRayWithObjectIntersectionChecks;
	}

	public long getLightRayWithObjectIntersections() {
		return lightRayWithObjectIntersections;
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
