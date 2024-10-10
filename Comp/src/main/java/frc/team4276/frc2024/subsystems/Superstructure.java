package frc.team4276.frc2024.subsystems;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.team4276.lib.drivers.Subsystem;

import frc.team1678.lib.loops.ILooper;
import frc.team1678.lib.loops.Loop;


public class Superstructure extends Subsystem {
    private ArmSubsystem mFlywheelSubsystem = ArmSubsystem.getInstance();

    private double mRegressionTuningFlywheelSetpoint = 0.0;
    private double mPrevShotFlywheelSetpoint = 0.0;


    private static Superstructure mInstance;

    public static Superstructure getInstance() {
        if (mInstance == null) {
            mInstance = new Superstructure();
        }

        return mInstance;
    }


    @Override
    public synchronized void readPeriodicInputs() {
        
    }

    @Override
    public synchronized void registerEnabledLoops(ILooper enabledLooper) {
        enabledLooper.register(new Loop() {
            @Override
            public void onStart(double timestamp) {
            }

            @Override
            public void onLoop(double timestamp) {
                synchronized(this) {
                }
            }

            @Override
            public void onStop(double timestamp) {
                stop();
            }
        });
    }
    @Override
    public void writePeriodicOutputs() {
    } // Leave empty

    @Override
    public synchronized void outputTelemetry() {
        

        SmartDashboard.putNumber("Debug/Regression Tuning/Flywheel Setpoint", mRegressionTuningFlywheelSetpoint);

        SmartDashboard.putNumber("Debug/Regression Tuning/Prev Shot Flywheel Setpoint", mPrevShotFlywheelSetpoint);
        
    }
}
