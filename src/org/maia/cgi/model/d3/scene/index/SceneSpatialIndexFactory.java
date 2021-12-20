package org.maia.cgi.model.d3.scene.index;

import org.maia.cgi.model.d3.scene.Scene;

public class SceneSpatialIndexFactory {

	private static SceneSpatialIndexFactory instance;

	private SceneSpatialIndexFactory() {
	}

	public static SceneSpatialIndexFactory getInstance() {
		if (instance == null) {
			setInstance(new SceneSpatialIndexFactory());
		}
		return instance;
	}

	private static synchronized void setInstance(SceneSpatialIndexFactory factory) {
		if (instance == null) {
			instance = factory;
		}
	}

	public SceneSpatialIndex createIndex(Scene scene) {
		SceneSpatialIndex index = null;
		BinnedSceneSpatialIndex uniform = createUniformlyBinnedIndex(scene);
		BinnedSceneSpatialIndex nonUniform = createNonUniformlyBinnedIndex(scene);
		if (uniform.getBinStatistics().getAverageObjectsPerUnitSpace() <= nonUniform.getBinStatistics()
				.getAverageObjectsPerUnitSpace()) {
			index = uniform;
			nonUniform.dispose();
		} else {
			index = nonUniform;
			uniform.dispose();
		}
		System.gc();
		return index;
	}

	private BinnedSceneSpatialIndex createUniformlyBinnedIndex(Scene scene) {
		return new UniformlyBinnedSceneSpatialIndex(scene, 50, 50, 50);
	}

	private BinnedSceneSpatialIndex createNonUniformlyBinnedIndex(Scene scene) {
		return new NonUniformlyBinnedSceneSpatialIndex(scene, 125000);
	}

}