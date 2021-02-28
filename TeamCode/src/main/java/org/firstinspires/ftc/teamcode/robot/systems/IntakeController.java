package org.firstinspires.ftc.teamcode.robot.systems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.Controller;

@Config
public class IntakeController implements Controller {
    private Telemetry telemetry;
    private HardwareMap hardwareMap;
    private DcMotorEx intake;
    private DcMotorEx intake2;
    private Servo arm;
    public static String ControllerName;

    public static double ArmStartPos = 0.262;
    public static double ArmDropPos = 0.6;
    public static double IntakePower = 0.6;
    public static double Intake2Power = 0.6;

    /*
    * Do not change motor direction to avoid breaking odometry
    * which uses the same encoder ports
    * */

    public static Servo.Direction ArmDirection = Servo.Direction.REVERSE;

    public IntakeController(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        this.hardwareMap = hardwareMap;
        ControllerName = getClass().getSimpleName();
    }

    @Override
    public void init() {
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        intake2 = hardwareMap.get(DcMotorEx.class, "intake2");
        arm = hardwareMap.get(Servo.class, "intake_arm");
        arm.setDirection(ArmDirection);


        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intake2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        arm.setPosition(ArmStartPos);
    }

    @Override
    public void start() {

    }

    public void extend() {
        arm.setPosition(ArmDropPos);
    }

    @Override
    public void stop() {
        stopIntake();
        arm.setPosition(ArmStartPos);
    }

    public void stopIntake() {
        intake.setPower(0);
        intake2.setPower(0);
    }

    public void run(DcMotorEx.Direction Direction) {
        telemetry.addData(ControllerName, "Intaking");
        if (Direction == DcMotorSimple.Direction.FORWARD) {
            intake.setPower(IntakePower);
            intake2.setPower(Intake2Power);
        }
        else {
            intake.setPower(-IntakePower);
            intake2.setPower(-Intake2Power);
        }
    }
}
