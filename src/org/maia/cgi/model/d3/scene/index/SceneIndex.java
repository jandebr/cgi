package org.maia.cgi.model.d3.scene.index;

import org.maia.cgi.model.d3.scene.Scene;

/**
 * Auxiliary data structure for a <code>Scene</code>, allowing faster lookup operations
 * 
 * @see Scene
 */
public interface SceneIndex {

	/**
	 * Builds the index from the current scene state
	 */
	void buildIndex();

	/**
	 * Disposes the index to free up memory, after which it cannot be used anymore
	 */
	void dispose();

	/**
	 * Returns the scene for which this is an index
	 * 
	 * @return The scene
	 */
	Scene getScene();

}