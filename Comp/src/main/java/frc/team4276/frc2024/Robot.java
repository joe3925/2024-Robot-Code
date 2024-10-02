// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.team4276.frc2024;

import edu.wpi.first.wpilibj.TimedRobot;
import frc.team4276.frc2024.controlboard.ControlBoard;
import frc.team4276.frc2024.subsystems.FlywheelSubsystem;
import frc.team4276.frc2024.subsystems.Superstructure;

import frc.team1678.lib.loops.Looper;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

    private final SubsystemManager mSubsystemManager = SubsystemManager.getInstance();
    private final ControlBoard mControlBoard = ControlBoard.getInstance();

    private final Superstructure mSuperstructure = Superstructure.getInstance();

    private FlywheelSubsystem mFlywheelSubsystem;

    private final Looper mEnabledLooper = new Looper();
    private final Looper mDisabledLooper = new Looper();

    /**
     * This function is run when the robot is first started up and should be used
     * for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        try {
            // mVisionDeviceManager = VisionDeviceManager.getInstance();
            mFlywheelSubsystem = FlywheelSubsystem.getInstance();

            // Set subsystems
            mSubsystemManager.setSubsystems(
                    mSuperstructure,
                    mFlywheelSubsystem
                    // mVisionDeviceManager,
            );
            mSubsystemManager.registerEnabledLoops(mEnabledLooper);
            mSubsystemManager.registerDisabledLoops(mDisabledLooper);
        } catch (Throwable t) {
            throw t;
        }
    }

    /**
     * This function is called every 20 ms, no matter the mode. Use this for items
     * like diagnostics
     * that you want ran during disabled, autonomous, teleoperated and test.
     *
     * <p>
     * This runs after the mode specific periodic functions, but before LiveWindow
     * and
     * SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        mEnabledLooper.outputToSmartDashboard();
    }

    /** This function is called once each time the robot enters Disabled mode. */
    @Override
    public void disabledInit() {
        try {
            mEnabledLooper.stop();
            mDisabledLooper.start();
            mSubsystemManager.stop();

        } catch (Throwable t) {
            throw t;

        }
    }

    @Override
    public void disabledPeriodic() {

    }

    @Override
    public void disabledExit() {
    }

    /**
     * This autonomous runs the autonomous command selected by your
     * {@link RobotContainer} class.
     */
    @Override
    public void autonomousInit() {
        mDisabledLooper.stop();

    }

    /** This function is called periodically during autonomous. */
    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void autonomousExit() {
    }

    @Override
    public void teleopInit() {
        mDisabledLooper.stop();
        mEnabledLooper.start();
    }

    /** This function is called periodically during operator control. */
    @Override
    public void teleopPeriodic() {
    }

    @Override
    public void teleopExit() {
    }

    @Override
    public void testInit() {
    }

    /** This function is called periodically during test mode. */
    @Override
    public void testPeriodic() {
    }
}