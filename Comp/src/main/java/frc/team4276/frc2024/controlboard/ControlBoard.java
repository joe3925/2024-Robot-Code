package frc.team4276.frc2024.controlboard;

import frc.team4276.frc2024.Constants.OIConstants;
import frc.team4276.frc2024.subsystems.Superstructure;

public class ControlBoard {
    public final BetterXboxController driver;
    public final BetterXboxController operator;

    private Superstructure mSuperstructure;
    private static ControlBoard mInstance;

    public static ControlBoard getInstance() {
        if (mInstance == null) {
            mInstance = new ControlBoard();
        }
        return mInstance;
    }

    private ControlBoard() {
        driver = new BetterXboxController(OIConstants.kDriverControllerPort);
        operator = new BetterXboxController(OIConstants.kOpControllerPort);

        mSuperstructure = Superstructure.getInstance();
    }

    public void updateTuning() {

    }

    public void update() {
        driver.update();
        operator.update();
    }

    public void updateNominal() {
    }

    public void updateManual() {
    }

}