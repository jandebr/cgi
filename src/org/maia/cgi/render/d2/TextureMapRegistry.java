package org.maia.cgi.render.d2;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TextureMapRegistry {

	private static int DEFAULT_CAPACITY = 200;

	private static TextureMapRegistry instance;

	private int capacity;

	private Map<TextureMapHandle, TextureMap> textureMaps;

	private Deque<TextureMapHandle> lruList; // least recently used at the beginning

	private TextureMapRegistry(int capacity) {
		this.capacity = capacity;
		this.textureMaps = new HashMap<TextureMapHandle, TextureMap>(capacity);
		this.lruList = new LinkedList<TextureMapHandle>();
	}

	public static TextureMapRegistry getInstance() {
		if (instance == null) {
			setInstance(new TextureMapRegistry(DEFAULT_CAPACITY));
		}
		return instance;
	}

	private static synchronized void setInstance(TextureMapRegistry registry) {
		if (instance == null) {
			instance = registry;
		}
	}

	public synchronized void clear() {
		for (TextureMapHandle handle : getTextureMaps().keySet()) {
			handle.dispose();
		}
		getTextureMaps().clear();
		getLruList().clear();
	}

	public synchronized TextureMap getTextureMap(TextureMapHandle handle) {
		TextureMap textureMap = getTextureMaps().get(handle);
		if (textureMap == null) {
			if (getSize() == getCapacity()) {
				removeLru();
			}
			textureMap = handle.resolve();
			getTextureMaps().put(handle, textureMap);
		}
		getLruList().remove(handle);
		getLruList().addLast(handle);
		return textureMap;
	}

	public synchronized void setCapacity(int capacity) {
		while (getSize() > capacity) {
			removeLru();
		}
		this.capacity = capacity;
	}

	private void removeLru() {
		if (!getLruList().isEmpty()) {
			TextureMapHandle handle = getLruList().removeFirst();
			getTextureMaps().remove(handle);
			handle.dispose();
		}
	}

	public synchronized int getSize() {
		return getLruList().size();
	}

	public synchronized int getCapacity() {
		return capacity;
	}

	private Map<TextureMapHandle, TextureMap> getTextureMaps() {
		return textureMaps;
	}

	private Deque<TextureMapHandle> getLruList() {
		return lruList;
	}

}