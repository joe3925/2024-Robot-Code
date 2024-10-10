package frc.team4276.frc2024.subsystems;


import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.motorcontrol.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.team4276.frc2024.Constants;
import frc.team4276.lib.characterizations.ArmFeedForward;
import frc.team4276.lib.drivers.Subsystem;


import frc.team1678.lib.loops.ILooper;
import frc.team1678.lib.loops.Loop;
import frc.team1678.lib.requests.Request;

public class ArmSubsystem extends Subsystem {
    private Talon motor;
    private PeriodicIO mPeriodicIO;
    private double limit = 2;
    private double deadZone = 2;
    private Encoder quadratureEncoder;
    private ArmFeedForward mFeedForward;

    private static ArmSubsystem mInstance;

    public static ArmSubsystem getInstance() {
        if (mInstance == null) {
            mInstance = new ArmSubsystem();
        }

        return mInstance;
    }

    private ArmSubsystem() {
        mPeriodicIO = new PeriodicIO();
        
        motor = new Talon(0);
        
        quadratureEncoder = new Encoder(1, 2, false, EncodingType.k2X);
        quadratureEncoder.setDistancePerPulse((2*Math.PI)/2048);
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
    public void setVoltage(double volatge){
        motor.setVoltage(volatge);
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


    @Override
    public void stop() {
    }

    private class PeriodicIO {

        public double RPM;
        public double RPM_demand;

        public double demand_voltage;

    }

    @Override
    public void registerEnabledLoops(ILooper enabledLooper) {
        enabledLooper.register(new Loop() {
            @Override
            public void onStart(double timestamp) {

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
        mPeriodicIO.RPM = (quadratureEncoder.getRate() / 8192.0) * 60.0;
        
        /*if(!isSpunUp()){
            if(isUnderShot()){
                System.out.println("under");
                mPeriodicIO.demand_voltage += 0.06;
            }
            if(isOverShot()){
                System.out.println("over");
                mPeriodicIO.demand_voltage -= 0.012;
             }
        }
        if (mPeriodicIO.demand_voltage > Constants.ArmConstants.voltageLimit){
            mPeriodicIO.demand_voltage = Constants.ArmConstants.voltageLimit;
        }
        if (mPeriodicIO.demand_voltage < -Constants.ArmConstants.voltageLimit){
            mPeriodicIO.demand_voltage = -Constants.ArmConstants.voltageLimit;
        }
        */
    }

    @Override
    public void readPeriodicInputs() {
        setVoltage(mPeriodicIO.demand_voltage);
    }

    @Override
    public void outputTelemetry() {
        SmartDashboard.putNumber("RPM", mPeriodicIO.RPM);
        SmartDashboard.putNumber("Dist", quadratureEncoder.getDistance());
        SmartDashboard.putNumber("demand voltage", mPeriodicIO.demand_voltage);
    }
}
