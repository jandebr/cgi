package org.maia.cgi;

public interface Memoise {

	/**
	 * Free up memory that is either unused or that can be transparently recomputed on-demand
	 */
	void releaseMemory();

}