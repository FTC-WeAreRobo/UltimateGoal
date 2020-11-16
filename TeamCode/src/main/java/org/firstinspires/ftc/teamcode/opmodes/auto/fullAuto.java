package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.robot.ControllerManager;
import org.firstinspires.ftc.teamcode.robot.camera.CameraController;

@Autonomous(name="fullAuto", group="Iterative Opmode")
public class fullAuto extends OpMode {

    private CameraController vuforia;

    private ControllerManager controllers;

    @Override
    public void init() {
        telemetry.addLine("Initializing...");
        vuforia = new CameraController(hardwareMap, telemetry);

        controllers = new ControllerManager(vuforia);

        controllers.init();
        telemetry.addLine("Initialized");

        //telemetry.addLine(vuforia.rankRings()); //Before we start game

    }

    @Override
    public void init_loop() {
        telemetry.addLine(vuforia.rankRings());
        vuforia.trackTargets();

    }

    @Override
    public void start() { //code to run once when play is hit
        controllers.start(); //stop vuforia instance
    }

    @Override
    public void loop() {

    }

    @Override
    public void stop() {
        controllers.stop();
    }
}
