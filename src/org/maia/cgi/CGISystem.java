package org.maia.cgi;

import java.lang.management.ManagementFactory;

import org.maia.cgi.model.d3.scene.Scene;

import com.sun.management.OperatingSystemMXBean;

public class CGISystem {

	private CGISystem() {
	}

	public static double getCpuLoad() {
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		return Math.max(0, osBean.getSystemCpuLoad());
	}

	public static long getTotalMemoryInBytes() {
		return Runtime.getRuntime().totalMemory();
	}

	public static long getFreeMemoryInBytes() {
		return Runtime.getRuntime().freeMemory();
	}

	public static long getUsedMemoryInBytes() {
		return getTotalMemoryInBytes() - getFreeMemoryInBytes();
	}

	public static void releaseMemoryAfterRendering(Scene scene) {
		releaseMemory();
	}

	public static void releaseMemory(Scene scene) {
		scene.releaseMemory();
		releaseMemory();
	}

	public static void releaseMemory() {
		System.gc();
	}

	public static void exit() {
		System.exit(0);
	}

}