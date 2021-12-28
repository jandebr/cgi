package org.maia.cgi.render.d3.shading;

import java.util.HashMap;
import java.util.Map;

import org.maia.cgi.model.d3.light.LightSource;
import org.maia.cgi.model.d3.object.Object3D;

public class ObscuredObjectsCache {

	private Map<Object3D, ObscuredObject3D> objectIndex;

	private int maxObjectSize;

	public ObscuredObjectsCache() {
		this(100);
	}

	public ObscuredObjectsCache(int maxObjectSize) {
		this.objectIndex = new HashMap<Object3D, ObscuredObject3D>(maxObjectSize);
		this.maxObjectSize = maxObjectSize;
	}

	public void addToCache(Object3D obscuredObject, LightSource lightSource, Object3D obscuringObject) {
		ObscuredObject3D entry = getObjectIndex().get(obscuredObject);
		if (entry == null) {
			if (getCurrentObjectSize() == getMaxObjectSize()) {
				getObjectIndex().clear(); // simple reset proved more optimal than LRU overhead
			}
			entry = new ObscuredObject3D(obscuredObject);
			getObjectIndex().put(obscuredObject, entry);
		}
		entry.setObscuringObject(lightSource, obscuringObject);
	}

	public Object3D getObscuringObject(Object3D obscuredObject, LightSource lightSource) {
		Object3D obscuringObject = null;
		ObscuredObject3D entry = getObjectIndex().get(obscuredObject);
		if (entry != null) {
			obscuringObject = entry.getObscuringObject(lightSource);
		}
		return obscuringObject;
	}

	private Map<Object3D, ObscuredObject3D> getObjectIndex() {
		return objectIndex;
	}

	private int getMaxObjectSize() {
		return maxObjectSize;
	}

	private int getCurrentObjectSize() {
		return getObjectIndex().size();
	}

	private static class ObscuredObject3D {

		private Object3D object;

		private Map<LightSource, Object3D> lightIndex;

		public ObscuredObject3D(Object3D object) {
			this.object = object;
			this.lightIndex = new HashMap<LightSource, Object3D>();
		}

		public void setObscuringObject(LightSource lightSource, Object3D obscuringObject) {
			getLightIndex().put(lightSource, obscuringObject);
		}

		public Object3D getObscuringObject(LightSource lightSource) {
			return getLightIndex().get(lightSource);
		}

		public Object3D getObject() {
			return object;
		}

		private Map<LightSource, Object3D> getLightIndex() {
			return lightIndex;
		}

	}

}