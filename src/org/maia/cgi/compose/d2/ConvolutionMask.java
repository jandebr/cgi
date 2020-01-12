package org.maia.cgi.compose.d2;

public interface ConvolutionMask {

	boolean isMasked(int row, int col);

	public static ConvolutionMask ALL_INCLUSIVE = new ConvolutionMask() {

		@Override
		public boolean isMasked(int row, int col) {
			return false;
		}
	};

}