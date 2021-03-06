package org.firstinspires.ftc.teamcode.robot.camera.algorithms;


import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.teamcode.robot.camera.algorithms.RingDetector.ringNames.FOUR;

public class RingDetector extends OpenCvPipeline {

    private Telemetry telemetry;
    private boolean debug = false;

    private static final Scalar lowerOrange = new Scalar(0.0, 141.0, 0.0);
    private static final Scalar upperOrange = new Scalar(255.0, 230.0, 95.0);

    private double centerX;
    private double centerY;

    private int CAMERA_WIDTH = 640;
    private static double HORIZON = 200; //(100.0 / 320.0) * CAMERA_WIDTH;
    private static double MIN_CONTOUR_WIDTH = 80; // (50.0 / 320.0) * CAMERA_WIDTH
    private static double ASPECT_RATIO_THRES = 0.7;

    private int ringCount = 0;
    private String ringCountStr = "";


    public static enum ringNames {
        NO, ONE, FOUR
    }

    public RingDetector(Telemetry telemetry, boolean debug) {
        this.telemetry = telemetry;
        this.debug = debug;
    }

    public RingDetector(Telemetry telemetry) {
        this(telemetry, false);
    }

    public int getRingCount() {
        return ringCount;
    }

    public String getRingCountStr() {
        return ringCountStr;
    }

    public double getCameraCenterX() {
        return (double)CAMERA_WIDTH / 2;
    }

    public double getDisplacementFromCenter() {
        return centerX - getCameraCenterX();
    }

    @Override
    public Mat processFrame(Mat input) {
        CAMERA_WIDTH = input.width();

        Mat ret = new Mat();
        Mat mat = new Mat();
        Mat hierarchy = new Mat();
        Mat mask = null;

        try { // try catch in order for opMode to not crash and force a restart
            /**converting from RGB color space to YCrCb color space**/
            Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGB2YCrCb);

            /**checking if any pixel is within the orange bounds to make a black and white mask**/
            mask = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC1); // variable to store mask in
            Core.inRange(mat, lowerOrange, upperOrange, mask);

            /**applying to input and putting it on ret in black or yellow**/
            Core.bitwise_and(input, input, ret, mask);

            /**applying GaussianBlur to reduce noise when finding contours**/
            Imgproc.GaussianBlur(mask, mask, new Size(5.0, 15.0), 0.00);

            /**finding contours on mask**/
            List<MatOfPoint> contours = new ArrayList();
            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

            /**drawing contours to ret in green**/
            Imgproc.drawContours(ret, contours, -1, new Scalar(0.0, 255.0, 0.0), 3);

            /**finding widths of each contour, comparing, and storing the widest**/
            double maxWidth = 0;
            Rect maxRect = new Rect();
            for (MatOfPoint c : contours) {
                MatOfPoint2f copy = new MatOfPoint2f(c.toArray());
                Rect rect = Imgproc.boundingRect(copy);

                double w = rect.width;
                // checking if the rectangle is below the horizon
                if (w > maxWidth && rect.y + rect.height > HORIZON) {
                    maxWidth = w;
                    maxRect = rect;
                }
                c.release(); // releasing the buffer of the contour, since after use, it is no longer needed
                copy.release(); // releasing the buffer of the copy of the contour, since after use, it is no longer needed
            }

            /**drawing widest bounding rectangle to ret**/
            Scalar color = new Scalar(0,255,255);
            Imgproc.rectangle(ret, maxRect, color, 4);

            centerX = maxRect.x + (double)maxRect.width/2;
            centerY = maxRect.y + (double)maxRect.height/2;

            Point center = new Point(centerX, centerY);
            Imgproc.drawMarker(ret, center, color, 0, 35);

            /** drawing a red line to show the horizon (any above the horizon is not checked to be a ring stack **/
            Imgproc.line(
                    ret,
                    new Point(.0, HORIZON),
                    new Point(CAMERA_WIDTH, HORIZON),
                    new Scalar(255.0, .0, 255.0));

            if (debug) telemetry.addData("Vision: maxW", maxWidth);

            /** checking if widest width is greater than equal to minimum width
             * using Java ternary expression to set height variable
             *
             * height = maxWidth >= MIN_WIDTH ? aspectRatio > BOUND_RATIO ? FOUR : ONE : ZERO
             **/

            if (maxWidth >= MIN_CONTOUR_WIDTH) {
                double aspectRatio = (double)maxRect.height / (double)maxRect.width;

                if(debug) telemetry.addData("Vision: Aspect Ratio", aspectRatio);

                /** checks if aspectRatio is greater than ASPECT_RATIO_THRES
                 * to determine whether stack is ONE or FOUR
                 */
                if (aspectRatio > ASPECT_RATIO_THRES) {
                    ringCount = 4; // FOUR
                    ringCountStr = FOUR.toString();
                }
                else {
                    ringCount = 1; // ONE
                    ringCountStr = ringNames.ONE.toString();
                }
            } else {
                ringCount = 0; // ZERO
                ringCountStr = ringNames.NO.toString();
            }

            if (debug) telemetry.addData("Vision: Height", ringCountStr);

            /**returns the contour mask combined with original image for context**/
            Core.addWeighted(ret, 0.65, input, 0.35, 0, input);

        } catch (Exception e) {
            /**error handling, prints stack trace for specific debug**/
            telemetry.addData("[ERROR]", e.toString());
            telemetry.addData("[ERROR]", e.getStackTrace().toString());
        }
        finally {
            // releasing all mats after use
            mask.release();
            hierarchy.release();
            mat.release();
            ret.release();
        }

        return input;
    }
}
