// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.team4276.frc2024;

import java.util.Optional;

import com.revrobotics.CANSparkBase.IdleMode;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.team4276.frc2024.Constants.DriveConstants;
import frc.team4276.frc2024.Constants.OIConstants;
import frc.team4276.frc2024.Constants.SuperstructureConstants;
import frc.team4276.frc2024.auto.AutoModeBase;
import frc.team4276.frc2024.auto.AutoModeExecutor;
import frc.team4276.frc2024.auto.AutoModeSelector;
import frc.team4276.frc2024.controlboard.ControlBoard;
import frc.team4276.frc2024.field.AllianceChooser;
import frc.team4276.frc2024.subsystems.ClimberSubsystem;
import frc.team4276.frc2024.subsystems.DriveSubsystem;
import frc.team4276.frc2024.subsystems.FlywheelSubsystem;
// import frc.team4276.frc2024.subsystems.FourBarSubsystem;
import frc.team4276.frc2024.subsystems.IntakeSubsystem;
import frc.team4276.frc2024.subsystems.LimeLight;
import frc.team4276.frc2024.subsystems.RobotStateEstimator;
import frc.team4276.frc2024.subsystems.SimpleFourbarSubsystem;
import frc.team4276.frc2024.subsystems.Superstructure;
// import frc.team4276.frc2024.subsystems.FlywheelSubsystem.DesiredFlywheelMode;
import frc.team4276.frc2024.subsystems.IntakeSubsystem.IntakeState;
import frc.team4276.frc2024.subsystems.Superstructure.GoalState;
import frc.team4276.frc2024.statemachines.FlywheelState;

import frc.team1678.lib.loops.Looper;
import frc.team1678.lib.swerve.ChassisSpeeds;

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
  // private final FourBarSubsystem mFourBarSubsystem = FourBarSubsystem.getInstance();
  private final IntakeSubsystem mIntakeSubsystem = IntakeSubsystem.getInstance();
  private final FlywheelSubsystem mFlywheelSubsystem = FlywheelSubsystem.getInstance();
  private final SimpleFourbarSubsystem mSimpleFourbarSubsystem = SimpleFourbarSubsystem.getInstance();
  private final ClimberSubsystem mClimberSubsystem = ClimberSubsystem.getInstance();

  private final Superstructure mSuperstructure = Superstructure.getInstance();

  private final Looper mEnabledLooper = new Looper();
  private final Looper mDisabledLooper = new Looper();

  private final AutoModeSelector mAutoModeSelector = new AutoModeSelector();
  private AutoModeExecutor mAutoModeExecutor;

  private final AllianceChooser mAllianceChooser = AllianceChooser.getInstance();

  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    try {

      CameraServer.startAutomaticCapture();

      // Set subsystems
      mSubsystemManager.setSubsystems(
          mDriveSubsystem,
          mRobotStateEstimator,
          mSuperstructure,
          // mFourBarSubsystem,
          mIntakeSubsystem,
          mFlywheelSubsystem,
          mSimpleFourbarSubsystem,
          mLimeLight,
          mClimberSubsystem
      );

      mSubsystemManager.registerEnabledLoops(mEnabledLooper);
      mSubsystemManager.registerDisabledLoops(mDisabledLooper);
      mRobotStateEstimator.registerEnabledLoops(mDisabledLooper);
      mRobotStateEstimator.resetOdometry(new Pose2d());
      RobotState.getInstance().resetKalmanFilters();

      SmartDashboard.putNumber("Fourbar des voltage input", 0.0);

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

  private boolean mHasFlippedClimberSetting = false;

  /** This function is called once each time the robot enters Disabled mode. */
  @Override
  public void disabledInit() {
    try {
      mEnabledLooper.stop();
      mDisabledLooper.start();
      mSubsystemManager.stop();
      mLimeLight.start();
      mLimeLight.setDisableProcessing(false);

    } catch (Throwable t) {
      throw t;

    }

    if (mAutoModeExecutor != null) {
      mAutoModeExecutor.stop();
    }

    mAutoModeSelector.reset();
    mAutoModeSelector.updateModeCreator(false);
    mAutoModeExecutor = new AutoModeExecutor();

    mHasFlippedClimberSetting = false;
  }

  @Override
  public void disabledPeriodic() {
    try {
      if (mAllianceChooser.getAlliance() == Alliance.Red) {
        RobotState.getInstance().setRed();
        mLimeLight.setRedTagMap();
      } else {
        RobotState.getInstance().setBlue();
        mLimeLight.setBlueTagMap();
      }

      mAutoModeSelector.updateModeCreator(mAllianceChooser.isAllianceChanged());
      Optional<AutoModeBase> autoMode = mAutoModeSelector.getAutoMode();
      if (autoMode.isPresent()) {
        mAutoModeExecutor.setAutoMode(autoMode.get());
      }

      if (mControlBoard.wantClimberCoastMode() && mHasFlippedClimberSetting){
        mClimberSubsystem.setIdleMode(IdleMode.kCoast);

      } else {
        mClimberSubsystem.setIdleMode(IdleMode.kBrake);

        mHasFlippedClimberSetting = true;
      
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

        mRobotStateEstimator.resetOdometry(autoMode.get().getStartingPose());
      } else {
        mRobotStateEstimator.resetOdometry(new Pose2d(0, 0, new Rotation2d(0.0)));

      }

      mEnabledLooper.start();
      mAutoModeExecutor.start();

      mLimeLight.setDisableProcessing(false);
      RobotState.getInstance().setHasBeenEnabled(true);

    } catch (Throwable t) {
      throw t;
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
  }

  private Rotation2d zeroHeading; // Degrees

  @Override
  public void teleopInit() {
    try {
      mDisabledLooper.stop();
      mEnabledLooper.start();

      mLimeLight.setDisableProcessing(false);

      RobotState.getInstance().setHasBeenEnabled(true);

      //TODO: put this in drive subsystem
      zeroHeading = mAllianceChooser.getAlliance() == Alliance.Red ? Rotation2d.fromDegrees(180.0) : Rotation2d.fromDegrees(0.0);

    } catch (Throwable t) {
      throw t;
    }
  }

  private GoalState state;
  private GoalState return_state = GoalState.READY_MIDDLE;

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    try {
      if (mControlBoard.wantZeroHeading()) {
        mDriveSubsystem.zeroHeading(zeroHeading.getDegrees());
      }

      if (mControlBoard.wantXBrake()) {
        mDriveSubsystem.setX();
      } else {
        mDriveSubsystem.teleopDrive(ChassisSpeeds.fromFieldRelativeSpeeds(
            mControlBoard.getSwerveTranslation().x(),
            mControlBoard.getSwerveTranslation().y(),
            mControlBoard.getSwerveRotation(),
            mDriveSubsystem.getHeading().toWPI().rotateBy(zeroHeading)  
        ));
      }

      if (mControlBoard.wantDemoLimits()) {
        mDriveSubsystem.setKinematicLimits(DriveConstants.kDemoLimits);
      } else {
        mDriveSubsystem.setKinematicLimits(DriveConstants.kUncappedLimits);
      }

      if(mControlBoard.operator.getYButtonReleased()){
        mSuperstructure.addFourbarScoringOffset(Math.toRadians(0.5));
      } else if(mControlBoard.operator.getLeftStickButtonReleased()){
        mSuperstructure.addFourbarScoringOffset(Math.toRadians(-0.5));
      } else if(mControlBoard.operator.getLeftYDeadband() > 0.0){
        mSuperstructure.addFourbarScoringOffset(Math.toRadians(-mControlBoard.operator.getLeftYDeadband() * 10.0));
      }

      // if(mControlBoard.wantAutoLock()){
      //   state = GoalState.DYNAMIC;
      // }

      if (mControlBoard.wantFastake()) {
        state = GoalState.FASTAKE;

      } else if (mControlBoard.wantSlowtake()) {
        state = GoalState.SLOWTAKE;

      }

      if (mControlBoard.wantReadyMiddle()) {
        return_state = GoalState.READY_MIDDLE;

      }

      if (state != null) {
        mSuperstructure.setGoalState(state);  

      } else if (return_state != null) {
        mSuperstructure.setGoalState(return_state);

      }

      state = null;

      if (mControlBoard.operator.getLT()) {
        mSuperstructure.setFlywheelState(SuperstructureConstants.kNormalShot);
      } else if (mControlBoard.operator.getLeftBumper()) {
        mSuperstructure.setFlywheelState(SuperstructureConstants.kWhatTheFlip);
      } else {
        mSuperstructure.setFlywheelState(FlywheelState.identity());
      }

      // if(Math.abs(mControlBoard.operator.getLeftY()) >
      // OIConstants.kJoystickDeadband){
      // // mSuperstructure.

      // } else
      if (mControlBoard.operator.getRT()) {
        mSuperstructure.setIntakeState(IntakeState.FOOT);

      } else if (mControlBoard.driver.getBButton()) {
        mSuperstructure.setIntakeState(IntakeState.FAST_DEFEED);
        
      } else if (mControlBoard.operator.getXButton()) {
        mSuperstructure.setIntakeState(IntakeState.REVERSE);

      } else {
        mSuperstructure.setIntakeState(IntakeState.IDLE);

      }

      if (mControlBoard.operator.getRightStickButtonPressed()) {
        mSuperstructure.toggleBrakeModeOnFourbar();
      }

      if (mControlBoard.operator.getAButtonPressed()) {
        mSuperstructure.toggleFourbarVoltageMode();
      }

      if (Math.abs(mControlBoard.operator.getRightY()) > OIConstants.kJoystickDeadband) {
        mSuperstructure.setFourBarVoltage(mControlBoard.operator.getRightYDeadband() * 6.0);

      }else if (mControlBoard.operator.isPOVDOWNPressed()) {
        mSimpleFourbarSubsystem.setCalibrating();
      } else if (mControlBoard.operator.isPOVLEFTPressed()) {
        mSimpleFourbarSubsystem.setTestTrapezoid();
      } else {
        mSuperstructure.setFourBarVoltage(0.0);
      }

      if (mControlBoard.operator.getRightBumper()){
        mClimberSubsystem.setDesiredState(ClimberSubsystem.DesiredState.RAISE);

      } else if(mControlBoard.driver.getLeftBumper()){
        mClimberSubsystem.setDesiredState(ClimberSubsystem.DesiredState.S_LOWER);

      } else if(mControlBoard.driver.getLT()){
        mClimberSubsystem.setDesiredState(ClimberSubsystem.DesiredState.F_LOWER);

      } else {
        mClimberSubsystem.setDesiredState(ClimberSubsystem.DesiredState.IDLE);

      }
    } catch (Throwable t) {
      System.out.println(t.getMessage());
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
