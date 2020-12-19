package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.params.FieldConstants;
import org.firstinspires.ftc.teamcode.opmodes.auto.params.Auto;

@Autonomous(name="BlueRightAuto", group="Iterative Opmode", preselectTeleOp="MainTele")
@Config //for FTCDash
public class BlueRightAuto extends Auto {

    @Override
    public void init(){
        sequenceName = makeSequenceName(FieldConstants.BlueAlliance, FieldConstants.RightSide);
        super.init();
    }
}
