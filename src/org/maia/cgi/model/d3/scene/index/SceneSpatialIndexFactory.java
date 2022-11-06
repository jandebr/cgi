package org.maia.cgi.model.d3.scene.index;

import org.maia.cgi.CGISystem;
import org.maia.cgi.model.d3.scene.Scene;
import org.maia.cgi.model.d3.scene.index.BinnedSceneSpatialIndex.BinStatistics;

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

	public SceneSpatialIndex createSpatialIndex(Scene scene) {
		SceneSpatialIndex index = null;
		BinnedSceneSpatialIndex uniformIndex = createUniformlyBinnedIndex(scene);
		BinnedSceneSpatialIndex nonUniformIndex = createNonUniformlyBinnedIndex(scene);
		BinStatistics uniformStats = uniformIndex.getBinStatistics();
		BinStatistics nonUniformStats = nonUniformIndex.getBinStatistics();
		if (nonUniformStats.getMaximumObjectsPerBin() < uniformStats.getMaximumObjectsPerBin()
				|| nonUniformStats.getAverageObjectsPerUnitSpace() < uniformStats.getAverageObjectsPerUnitSpace()) {
			index = nonUniformIndex;
			uniformIndex.dispose();
		} else {
			index = uniformIndex;
			nonUniformIndex.dispose();
		}
		CGISystem.releaseMemory();
		return index;
	}

	public SceneViewPlaneIndex createViewPlaneIndex(Scene scene) {
		NonUniformlyBinnedSceneViewPlaneIndex index = new NonUniformlyBinnedSceneViewPlaneIndex(scene, 250000);
		index.buildIndex();
		return index;
	}

	private BinnedSceneSpatialIndex createUniformlyBinnedIndex(Scene scene) {
		BinnedSceneSpatialIndex index = new UniformlyBinnedSceneSpatialIndex(scene, 50, 50, 50);
		index.buildIndex();
		return index;
	}

	private BinnedSceneSpatialIndex createNonUniformlyBinnedIndex(Scene scene) {
		BinnedSceneSpatialIndex index = new NonUniformlyBinnedSceneSpatialIndex(scene, 125000);
		index.buildIndex();
		return index;
	}

}