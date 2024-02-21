package frc.team4276.lib.drivers;

import frc.team4276.lib.util.Util;

import frc.team254.lib.geometry.Translation2d;
import frc.team254.lib.geometry.Rotation2d;

public class FixedFourBarFeedForward {
    // Constants
    private final double kS; // Volts
    private final double kV; // Volts * s / rad

    private final double kGearRatio;
    private final double kStallTorque;
    private final int kMotorAmnt;
    private final double kEfficiency;

    /*
     * NOTE:
     * Angles are in standard position.
     * Motor Leg is on the left and support leg is on the right.
     */

    private final double kBottomLength;
    private final double kMotorLegLength;
    private final double kTopLength;
    private final double kSupportLegLength;

    /*
     * Lay the line between the rotating points on the X AXIS
     * (if y value = 0 then Com is on the line between the two points of rotation)
     * The STATIC POINT should be on the origin.
     */
    private final Translation2d kMotorToCom;
    private final Translation2d kMotorLegToTopCom;
    private final Translation2d kSupportToCom;

    // Dynamics
    
    // Angles on the INSIDE of the quadrilateral (always positive)
    private double motor_inside_angle_;
    private double motor_leg_to_top_inside_angle_;
    private double support_inside_angle_;
    private double support_leg_to_top_inside_angle_;

    // Angles relevant for us
    private double bottom_to_motor_leg_radians_;
    private double bottom_to_top_radians_;
    private double bottom_to_support_leg_radians_; // Supplement of the inside angle
    /*
     * for bottom_to_support_leg_radians_ imagine the bottom support extends
     * to the RIGHT of the support leg and measure from there
     */

    private double motor_to_leg_Com_x;
    private double support_to_leg_Com_x;

    private double motor_leg_to_top_Com_x;

    public class FixedFourBarFeedForwardConstants {
        public double kS;

        public double kMotorFreeSpeedRpm;
        public double kGearRatio;
        public double kStallTorque;
        public int kMotorAmnt;
        public double kEfficiency;

        public double kBottomLength;
        public double kMotorLegLength;
        public double kTopLength;
        public double kSupportLegLength;

        public Translation2d kMotorToCom;
        public Translation2d kMotorLegToTopCom;
        public Translation2d kSupportToCom;
    }

    public FixedFourBarFeedForward(FixedFourBarFeedForwardConstants constants) {
        this.kS = constants.kS;
        // max voltage / max speed RPM converted to radians per second
        this.kV = (12 / (constants.kMotorFreeSpeedRpm / constants.kGearRatio)) * 60 / (2 * Math.PI);

        this.kGearRatio = constants.kGearRatio;
        this.kStallTorque = constants.kStallTorque;
        this.kMotorAmnt = constants.kMotorAmnt;
        this.kEfficiency = constants.kEfficiency;

        this.kBottomLength = constants.kBottomLength;
        this.kMotorLegLength = constants.kMotorLegLength;
        this.kTopLength = constants.kTopLength;
        this.kSupportLegLength = constants.kSupportLegLength;

        this.kMotorToCom = constants.kMotorToCom;
        this.kMotorLegToTopCom = constants.kMotorLegToTopCom;
        this.kSupportToCom = constants.kSupportToCom;

    }

    /**
     * @param position_setpoint radians
     * @param velocity_setpoint radians / s
     * @return
     */
    public double calculate(double position_setpoint, double velocity_setpoint) {
        return calcGravityVoltage(position_setpoint) + kS * Math.signum(velocity_setpoint) + kV * velocity_setpoint;
    }

    private double calcGravityVoltage(position_setpoint) {
        updateInsideAngles(position);
        updateRelevantAngles();
        updateComs();
        
        // desired torque * max volts / max torque
        return calcGravityTorque() * kEfficiency * 12 / (kStallTorque * kMotorAmnt * kGearRatio);
    }

    private void updateInsideAngles(double position){
        motor_inside_angle_ = position;

        double top_left_to_bottom_right = Util.LoCLength(kBottomLength, kMotorLegLength, motor_inside_angle_);

        support_leg_to_top_inside_angle_ = Util.LoCAngle(kTopLength, kSupportLegLength, top_left_to_bottom_right);

        motor_leg_to_top_inside_angle_ = Util.LoSAngle(top_left_to_bottom_right, motor_inside_angle_, kBottomLength)
            + Util.LoSAngle(top_left_to_bottom_right, support_leg_to_top_inside_angle_, kSupportLegLength);

        support_inside_angle_ = Util.LoSAngle(top_left_to_bottom_right, motor_inside_angle_, kMotorLegLength)
            + Util.LoSAngle(top_left_to_bottom_right, support_leg_to_top_inside_angle_, kTopLength);

    }

    private void updateRelevantAngles() {
        bottom_to_motor_leg_radians_ = motor_inside_angle_;

        bottom_to_support_leg_radians_ = Math.PI - support_inside_angle_;

        bottom_to_top_radians_ = motor_leg_to_top_inside_angle_ - (bottom_to_motor_leg_radians_ + 90);
    }

    private void updateComs() {
        motor_to_leg_C5om_x = kMotorToCom.rotateBy(Rotation2d.fromRadians(
            bottom_to_motor_leg_radians_)).x_;

        motor_leg_to_top_Com_x

        

        

    }

    private double calcGravityTorque() {
        return 0.0;
    }

    private double calcMotorLegTorque() {
        return 0.0;
    }

    private double calcSupportLegTorque() {
        return 0.0;
    }

    private double calcShooterToMotorLeg() {
        return 0.0;
    }

    private double calcShooterToSupportLeg() {
        return 0.0;
    }

    private double calcTorque(double force, double distance, double radians) {
        return force * distance * Math.cos(radians);
    }

    /**
     * @param angle_radians from dynamics
     * @param x_            from constants
     * @param y_            from constants
     */
    private double calcComOnLine(double angle_radians, double x_, double y_) {
        return 0.0;
    }

}
