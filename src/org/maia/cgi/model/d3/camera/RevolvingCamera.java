package org.maia.cgi.model.d3.camera;

import org.maia.cgi.geometry.d3.Point3D;

public interface RevolvingCamera extends MovableCamera {

	/**
	 * Returns the central point around which this camera revolves
	 * 
	 * @return The central point
	 */
	Point3D getPivotPoint();

	/**
	 * Sets or updates the central point around which this camera revolves
	 * 
	 * @param pivotPoint
	 *            The new central point
	 */
	void setPivotPoint(Point3D pivotPoint);

	/**
	 * Revolves this camera in longitudinal direction around its central point
	 * 
	 * @param angleInRadians
	 *            The revolving angle, measured in radians
	 * @return This camera (for chaining movements)
	 */
	RevolvingCamera revolveLongitudinal(double angleInRadians);

	/**
	 * Revolves this camera in latitudinal direction around its central point
	 * 
	 * @param angleInRadians
	 *            The revolving angle, measured in radians
	 * @return This camera (for chaining movements)
	 */
	RevolvingCamera revolveLatitudinal(double angleInRadians);

	/**
	 * Moves this camera closer to or farther from its central point
	 * 
	 * @param distance
	 *            The distance to move closer (if negative) or move further away (if positive)
	 * @return This camera (for chaining movements)
	 * @throws IllegalArgumentException
	 *             When moving passed or at this camera's central point. More concrete, when <code>distance &lt; 0 &&
	 *             -distance &gt;= getDistance()</code>
	 * @see #getDistance()
	 */
	RevolvingCamera alterDistance(double distance);

	/**
	 * Returns the distance between this camera and its central point
	 * 
	 * @return The distance &gt;= 0
	 */
	double getDistance();

}