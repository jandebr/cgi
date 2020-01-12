package org.maia.cgi.model.d3.camera;

import org.maia.cgi.geometry.d3.Point3D;
import org.maia.cgi.geometry.d3.Vector3D;

public interface MovableCamera extends Camera {

	/**
	 * Positions this camera in space
	 * 
	 * @param position
	 *            The new camera position
	 * @return This camera (for chaining movements)
	 */
	MovableCamera position(Point3D position);

	/**
	 * Positions this camera in space with a default "upward" direction
	 * 
	 * @param position
	 *            The new camera position
	 * @param lookAt
	 *            A point in space the camera is looking at
	 * @return This camera (for chaining movements)
	 */
	MovableCamera position(Point3D position, Point3D lookAt);

	/**
	 * Positions this camera in space
	 * 
	 * @param position
	 *            The new camera position
	 * @param lookAt
	 *            A point in space the camera is looking at
	 * @param up
	 *            Upward direction of the camera
	 * @return This camera (for chaining movements)
	 */
	MovableCamera position(Point3D position, Point3D lookAt, Vector3D up);

	/**
	 * Translates this camera's position along its three coordinate axes
	 * 
	 * @param du
	 *            Translation distance along the <em>U</em> axis
	 * @param dv
	 *            Translation distance along the <em>V</em> axis
	 * @param dn
	 *            Translation distance along the <em>N</em> axis
	 * @return This camera (for chaining movements)
	 */
	MovableCamera slide(double du, double dv, double dn);

	/**
	 * Rotates this camera along the <em>U</em> axis, also known as "pitch" in aviation
	 * 
	 * @param angleInRadians
	 *            The rotation angle, measured in radians
	 * @return This camera (for chaining movements)
	 */
	MovableCamera pitch(double angleInRadians);

	/**
	 * Rotates this camera along the <em>V</em> axis, also known as "yaw" in aviation
	 * 
	 * @param angleInRadians
	 *            The rotation angle, measured in radians
	 * @return This camera (for chaining movements)
	 */
	MovableCamera yaw(double angleInRadians);

	/**
	 * Rotates this camera along the <em>N</em> axis, also known as "roll" in aviation
	 * 
	 * @param angleInRadians
	 *            The rotation angle, measured in radians
	 * @return This camera (for chaining movements)
	 */
	MovableCamera roll(double angleInRadians);

}