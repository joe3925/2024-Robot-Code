// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.team1678.lib.swerve;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;

/**
 * Class for swerve drive odometry. Odometry allows you to track the robot's
 * position on the field
 * over a course of a match using readings from your swerve drive encoders and
 * swerve azimuth
 * encoders.
 *
 * <p>
 * Teams can use odometry during the autonomous period for complex tasks like
 * path following.
 * Furthermore, odometry can be used for latency compensation when using
 * computer-vision systems.
 */
public class SwerveDriveOdometry {
	private final SwerveDriveKinematics m_kinematics;
	private Pose2d m_poseMeters;
	private Rotation2d m_prevRotation;

	private final int m_numModules;
	private SwerveModulePosition[] m_previousModulePositions;

	/**
	 * Constructs a SwerveDriveOdometry object.
	 *
	 * @param kinematics      The swerve drive kinematics for your drivetrain.
	 * @param modulePositions The wheel positions reported by each module.
	 * @param initialPose     The starting position of the robot on the field.
	 */
	public SwerveDriveOdometry(
		SwerveDriveKinematics kinematics, SwerveModulePosition[] modulePositions, Pose2d initialPose) {
		m_kinematics = kinematics;
		m_poseMeters = initialPose;
		m_prevRotation = initialPose.getRotation();
		m_numModules = modulePositions.length;
		m_previousModulePositions = modulePositions;
	}

	/**
	 * Constructs a SwerveDriveOdometry object with the default pose at the origin.
	 *
	 * @param kinematics      The swerve drive kinematics for your drivetrain.
	 * @param gyroAngle       The angle reported by the gyroscope.
	 * @param modulePositions The wheel positions reported by each module.
	 */
	public SwerveDriveOdometry(
			SwerveDriveKinematics kinematics,
			SwerveModulePosition[] modulePositions) {
		this(kinematics, modulePositions, new Pose2d());
	}

	/**
	 * Resets the robot's position on the field.
	 *
	 * <p>
	 * Module positions do not need to be reset in user code.
	 *
	 * @param modulePositions The wheel positions reported by each module.,
	 * @param pose            The position on the field that your robot is at.
	 */
	public void resetPosition(SwerveModulePosition[] modulePositions, Pose2d pose) {
		m_poseMeters = pose;
		m_prevRotation = pose.getRotation();
		m_previousModulePositions = modulePositions;
	}

	/**
	 * Use when reseting gyro. Prevents inaccurate deltas for pose estimation.
	 * If the gyro was at 15 deg then was reset to 0, the offset is -15.
	 * @param gyroAngle Gyro offset
	 */
	public void offsetGyro(Rotation2d offset) {
		m_prevRotation = m_prevRotation.plus(offset);
	}

	/**
	 * Returns the position of the robot on the field.
	 *
	 * @return The pose of the robot (x and y are in meters).
	 */
	public Pose2d getPoseMeters() {
		return m_poseMeters;
	}

	/**
	 * Updates the robot's position on the field using forward kinematics and
	 * integration of the pose
	 * over time. This method automatically calculates the current time to calculate
	 * period
	 * (difference between two timestamps). The period is used to calculate the
	 * change in distance
	 * from a velocity. This also takes in an angle parameter which is used instead
	 * of the angular
	 * rate that is calculated from forward kinematics.
	 *
	 * @param gyroAngle       The angle reported by the gyroscope.
	 * @param modulePositions The current position of all swerve modules. Please
	 *                        provide the positions
	 *                        in the same order in which you instantiated your
	 *                        SwerveDriveKinematics.
	 * @return The new pose of the robot.
	 */
	public Pose2d update(Rotation2d gyroAngle, SwerveModulePosition[] modulePositions) {
		if (modulePositions.length != m_numModules) {
			throw new IllegalArgumentException(
					"Number of modules is not consistent with number of wheel locations provided in "
							+ "constructor");
		}

		var moduleDeltas = new SwerveModulePosition[m_numModules];
		for (int index = 0; index < m_numModules; index++) {
			var current = modulePositions[index];
			var previous = m_previousModulePositions[index];

			moduleDeltas[index] = new SwerveModulePosition(current.distanceMeters - previous.distanceMeters,
					current.angle);
			previous.distanceMeters = current.distanceMeters;
		}

		var twist = m_kinematics.toTwist2d(moduleDeltas);
		twist.dtheta = gyroAngle.minus(m_prevRotation).getRadians();

		var newPose = m_poseMeters.exp(twist);

		m_prevRotation = gyroAngle;
		m_poseMeters = new Pose2d(newPose.getTranslation(), gyroAngle);

		return m_poseMeters;
	}
}
