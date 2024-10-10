// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.team4276.frc2024;

import com.revrobotics.CANSparkBase.IdleMode;


public final class Constants {
    public static final double kLooperDt = 0.02;

    public static class ArmConstants {
        public static double voltageLimit = 7;

        public static double kS = 0.188;
        public static double kV = 0.002;
        public static double kA = 0;
    }

    public static final class SuperstructureConstants {

        public static final int kNormalShotRPM = 3500;
        public static final int kFerryRPM = 3500;

        public static final double kShotWaitTime = 0.5;
        public static final double kExhaustWaitTime = 0.5;

        public static final int kSpinUpRPM = 2500;
        public static final double kSpinUpDistance = 6.0;
    }
    public static final class OIConstants {
        public static final int kDriverControllerPort = 0;
        public static final int kOpControllerPort = 1;
        public static final double kJoystickDeadband = 0.1;
    }
}
