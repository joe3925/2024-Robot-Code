package frc.team4276.frc2024.subsystems;

import com.revrobotics.RelativeEncoder;

import frc.team4276.frc2024.Ports;
import frc.team4276.frc2024.Constants.FlywheelConstants;
import frc.team4276.lib.drivers.Subsystem;
import frc.team4276.lib.rev.VIKCANSparkMax;
import frc.team4276.lib.rev.CANSparkMaxFactory;

import frc.team1678.lib.loops.ILooper;
import frc.team1678.lib.loops.Loop;
import frc.team1678.lib.requests.Request;

public class FlywheelSubsystem extends Subsystem {
    private VIKCANSparkMax motor;

    private RelativeEncoder encoder;


    private static FlywheelSubsystem mInstance;

    public static FlywheelSubsystem getInstance() {
        if (mInstance == null) {
            mInstance = new FlywheelSubsystem();
        }

        return mInstance;
    }

    private FlywheelSubsystem() {
        motor = CANSparkMaxFactory.createDefault(Ports.FLYWHEEL_TOP);
        motor.setInverted(true);
        motor.setIdleMode(FlywheelConstants.kIdleMode);
        motor.setSmartCurrentLimit(FlywheelConstants.kSmartCurrentLimit);
        
        encoder = motor.getEncoder();
        encoder.setAverageDepth(FlywheelConstants.kAvgSamplingDepth);
        encoder.setMeasurementPeriod(FlywheelConstants.kMeasurementPeriod);
        encoder.setVelocityConversionFactor(FlywheelConstants.kUnitsPerRotation);

        motor.burnFlash();
    }

    public Request rpmRequest(double RPM) {
        return new Request() {
            @Override
            public void act() {
                setTargetRPM(RPM);
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        };

    }

    public void setOpenLoop(double voltage) {
    }

    public void setOpenLoop(double des_top_voltage, double des_bottom_voltage) {
    }

    public void setTargetRPM(double RPM) {
        setTargetRPM(RPM, RPM);
    }

    public void setTargetRPM(double top_RPM, double bottom_RPM) {
    }

    @Override
    public void stop() {
        setOpenLoop(0, 0);
    }

    private class PeriodicIO {

    }

    @Override
    public void readPeriodicInputs() {
    }

    @Override
    public void registerEnabledLoops(ILooper enabledLooper) {
        enabledLooper.register(new Loop() {
            @Override
            public void onStart(double timestamp) {
                setOpenLoop(0.0);
            }

            @Override
            public void onLoop(double timestamp) {
            }

            @Override
            public void onStop(double timestamp) {
                stop();
            }
        });

    }

    @Override
    public void writePeriodicOutputs() {
    }

    @Override
    public void outputTelemetry() {        
    }
}
