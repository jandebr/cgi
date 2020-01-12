package org.maia.cgi.metrics;

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

	private long boundingBoxComputations;

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
		builder.append("\tpointTransformations: ").append(pointTransformations).append("\n");
		builder.append("\tpointNormalizations: ").append(pointNormalizations).append("\n");
		builder.append("\tmatrixMultiplications: ").append(matrixMultiplications).append("\n");
		builder.append("\tmatrixInversions: ").append(matrixInversions).append("\n");
		builder.append("\tvectorDotProducts: ").append(vectorDotProducts).append("\n");
		builder.append("\tvectorCrossProducts: ").append(vectorCrossProducts).append("\n");
		builder.append("\tvectorNormalizations: ").append(vectorNormalizations).append("\n");
		builder.append("\tvectorAnglesInBetween: ").append(vectorAnglesInBetween).append("\n");
		builder.append("\tlineWithPlaneIntersections: ").append(lineWithPlaneIntersections).append("\n");
		builder.append("\tlineWithLineIntersections: ").append(lineWithLineIntersections).append("\n");
		builder.append("\tboundingBoxComputations: ").append(boundingBoxComputations).append("\n");
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
		boundingBoxComputations = 0;
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

	public void incrementBoundingBoxComputations() {
		boundingBoxComputations++;
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
		return lineWithPlaneIntersections;
	}

	public long getBoundingBoxComputations() {
		return boundingBoxComputations;
	}

}
