package org.maia.cgi;

public interface MemorizedCompute {

	/**
	 * Free up memory that is either unused or that can be recomputed transparently
	 */
	void compactMemoryUsage();

}