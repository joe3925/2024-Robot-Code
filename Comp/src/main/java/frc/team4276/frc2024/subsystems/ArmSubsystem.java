package frc.team4276.frc2024.subsystems;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Volts;
import static edu.wpi.first.units.Units.VoltsPerMeterPerSecond;

import java.util.function.DoubleSupplier;

import static edu.wpi.first.units.Units.Seconds;



import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Voltage;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.motorcontrol.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Config;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.team4276.frc2024.Constants;
import frc.team4276.lib.characterizations.ArmFeedForward;
import frc.team4276.lib.drivers.Subsystem;


import frc.team1678.lib.loops.ILooper;
import frc.team1678.lib.loops.Loop;
import frc.team1678.lib.requests.Request;

public class ArmSubsystem implements edu.wpi.first.wpilibj2.command.Subsystem {
    private final Talon motor = new Talon(0);
    private PeriodicIO mPeriodicIO;
    private double deadZone = 2;
    private Encoder quadratureEncoder;
    private Config config = new SysIdRoutine.Config(null,Volts.of(3),null);

    private final SysIdRoutine mSysIdRoutine = new SysIdRoutine(
          config,
          new SysIdRoutine.Mechanism(
              // Tell SysId how to plumb the driving voltage to the motor(s).
              Voltage -> this.setVoltage(Voltage),
              // Tell SysId how to record a frame of data for each motor on the mechanism being
              // characterized.
                log -> {
                // Record a frame for the shooter motor.
                log.motor("shooter-wheel")
                    .voltage(Volts.of(mPeriodicIO.demand_voltage))
                    .angularPosition(Rotations.of(quadratureEncoder.getDistance()))
                    .angularVelocity(RotationsPerSecond.of(quadratureEncoder.getDistance()));
              },
              // Tell SysId to make generated commands require this subsystem, suffix test state in
              // WPILog with this subsystem's name ("shooter")
              this));

    public ArmSubsystem() {
        mPeriodicIO = new PeriodicIO();
            
        quadratureEncoder = new Encoder(1, 2, false, EncodingType.k2X);
        quadratureEncoder.setDistancePerPulse((2*Math.PI)/2048);
        
    }
    public void setVoltage(Measure<Voltage> volatge){
        motor.setVoltage(volatge.magnitude());
    }

    public void setTargetRPM(double RPM) {
        mPeriodicIO.RPM_demand = RPM;
    }

    public boolean isSpunUp() {
        return !isUnderShot() && !isOverShot();
    }

    private boolean isUnderShot() {
        return (mPeriodicIO.RPM < mPeriodicIO.RPM_demand - deadZone);
    }

    private boolean isOverShot() {
        double deadZone = 2;
        return (mPeriodicIO.RPM > mPeriodicIO.RPM_demand + deadZone);
    }

    private class PeriodicIO {

        public double RPM;
        public double RPM_demand;

        public double demand_voltage;

    }
      public Command runShooter(DoubleSupplier shooterSpeed) {
    // Run shooter wheel at the desired speed using a PID controller and feedforward.
    return run(() -> {
        motor.setVoltage(shooterSpeed.getAsDouble());
     });           
  }
    @Override
    public void periodic() {
    }

    public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
        return mSysIdRoutine.quasistatic(direction);
    }

    public Command sysIdDynamic(SysIdRoutine.Direction direction) {
        return mSysIdRoutine.dynamic(direction);
    }
}
