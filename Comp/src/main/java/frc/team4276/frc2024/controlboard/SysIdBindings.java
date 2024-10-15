package frc.team4276.frc2024.controlboard;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.team4276.frc2024.Constants.OIConstants;
import frc.team4276.frc2024.subsystems.ArmSubsystem;

public class SysIdBindings {
    CommandXboxController mDrivController = new CommandXboxController(OIConstants.kDriverControllerPort);
    private final ArmSubsystem mArmSubsystem = new ArmSubsystem();
    public void bindKeys(){
        Command command = mArmSubsystem.sysIdDynamic(SysIdRoutine.Direction.kForward);
        command.schedule();
        //mDrivController.a().whileTrue(mArmSubsystem.sysIdDynamic(SysIdRoutine.Direction.kForward));
        //mDrivController.b().whileTrue(mArmSubsystem.sysIdDynamic(SysIdRoutine.Direction.kReverse));
    }
}
