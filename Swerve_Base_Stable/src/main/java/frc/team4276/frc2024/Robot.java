// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.team4276.frc2024;

import java.util.Optional;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.team1678.lib.loops.Looper;
import frc.team1678.lib.swerve.ChassisSpeeds;
import frc.team4276.frc2024.Constants.DriveConstants;
import frc.team4276.frc2024.Constants.LimelightConstants;
import frc.team4276.frc2024.auto.AutoModeBase;
import frc.team4276.frc2024.auto.AutoModeExecutor;
import frc.team4276.frc2024.auto.AutoModeSelector;
import frc.team4276.frc2024.controlboard.ControlBoard;
import frc.team4276.frc2024.field.AllianceChooser;
import frc.team4276.frc2024.subsystems.DriveSubsystem;
import frc.team4276.frc2024.subsystems.LimeLight;
import frc.team4276.frc2024.subsystems.RobotStateEstimator;

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

  private final DriveSubsystem mDriveSubsystem = DriveSubsystem.getInstance();
  private final LimeLight mLimeLight = LimeLight.getInstance();
  private final RobotStateEstimator mRobotStateEstimator = RobotStateEstimator.getInstance();

  private final Looper mEnabledLooper = new Looper();
  private final Looper mDisabledLooper = new Looper();

  private final AutoModeSelector mAutoModeSelector = new AutoModeSelector();
  private AutoModeExecutor mAutoModeExecutor;

  private final AllianceChooser mAllianceChooser = AllianceChooser.getInstance();

  //TODO: implement custom objects for as many things as possible

  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    try {
      mSubsystemManager.setSubsystems(
          mDriveSubsystem,
          mLimeLight,
          mRobotStateEstimator);

      mSubsystemManager.registerEnabledLoops(mEnabledLooper);
      mSubsystemManager.registerDisabledLoops(mDisabledLooper);
      mRobotStateEstimator.registerEnabledLoops(mDisabledLooper);
      mRobotStateEstimator.resetOdometry(new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(180)));
      RobotState.getInstance().resetKalmanFilters();

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
    mSubsystemManager.outputToSmartDashboard();
    mEnabledLooper.outputToSmartDashboard();

  }

  /** This function is called once each time the robot enters Disabled mode. */
  @Override
  public void disabledInit() {
    try {
      mEnabledLooper.stop();
      mDisabledLooper.start();
      mLimeLight.start();
      mLimeLight.setDisableProcessing(true);

    } catch (Throwable t) {
      throw t;

    }

    if (mAutoModeExecutor != null) {
      mAutoModeExecutor.stop();
    }

    mAutoModeSelector.reset();
    mAutoModeSelector.updateModeCreator(false);
    mAutoModeExecutor = new AutoModeExecutor();
  }

  @Override
  public void disabledPeriodic() {
    try {
      if (mAllianceChooser.getAlliance() == Alliance.Red){
        mLimeLight.setRedTagMap();
      } else {
        mLimeLight.setBlueTagMap();
      }

      mAutoModeSelector.updateModeCreator(mAllianceChooser.isAllianceChanged());
      Optional<AutoModeBase> autoMode = mAutoModeSelector.getAutoMode();
      if (autoMode.isPresent()) {
        mAutoModeExecutor.setAutoMode(autoMode.get());
      }

    } catch (Throwable t) {
      throw t;
    }

  }

  /**
   * This autonomous runs the autonomous command selected by your
   * {@link RobotContainer} class.
   */
  @Override
  public void autonomousInit() {
    try {

      mDisabledLooper.stop();

      Optional<AutoModeBase> autoMode = mAutoModeSelector.getAutoMode();
      if (autoMode.isPresent()) {
        mAutoModeExecutor.setAutoMode(autoMode.get());
      }

      mEnabledLooper.start();
      mAutoModeExecutor.start();
      
      mRobotStateEstimator.resetOdometry(new Pose2d(0, 0, new Rotation2d(180)));

      mLimeLight.setDisableProcessing(true);
      RobotState.getInstance().setHasBeenEnabled(true);

    } catch (Throwable t) {
      throw t;
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
    try {
      mDisabledLooper.stop();
      mEnabledLooper.start();
      
      mLimeLight.setDisableProcessing(LimelightConstants.disableAfterTeleop);
      
      RobotState.getInstance().setHasBeenEnabled(true);

    } catch (Throwable t) {
      throw t;
    }
  }

  //TODO: check if need to flip heading

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    try {
      if (mControlBoard.driver.getController().getAButtonPressed()) {
        mDriveSubsystem.zeroHeading(0);
      }

      if (mControlBoard.driver.getController().getRightStickButtonPressed()){
        mDriveSubsystem.flipHeading();
      }

      if (mControlBoard.driver.getController().getXButton()) {
        mDriveSubsystem.setX();
      } else {
        mDriveSubsystem.teleopDrive(ChassisSpeeds.fromFieldRelativeSpeeds(
            mControlBoard.getSwerveTranslation().x(),
            mControlBoard.getSwerveTranslation().y(),
            mControlBoard.getSwerveRotation(),
            mDriveSubsystem.getWPIHeading()));
      }

      if (mControlBoard.driver.getController().getYButtonPressed()){
        mDriveSubsystem.setHeadingSetpoint(0);
      } else if (mControlBoard.driver.getController().getBButtonPressed()) {
        mDriveSubsystem.setHeadingSetpoint(180);
      }

      if (mControlBoard.driver.getController().getRightBumperPressed()) {
        mDriveSubsystem.setKinematicLimits(DriveConstants.kUncappedLimits);
      }

      if (mControlBoard.driver.getController().getLeftBumperPressed()) {
        mDriveSubsystem.setKinematicLimits(DriveConstants.kDemoLimits);
      }

    } catch (Throwable t) {
      throw t;
    }
  }

  @Override
  public void testInit() {
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
  }
}
