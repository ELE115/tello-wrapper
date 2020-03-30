import GateDetector.GateDetector;
import com.github.ele115.tello_wrapper.FrameGrabber;
import com.github.ele115.tello_wrapper.ITelloDrone;
import com.github.ele115.tello_wrapper.Tello;
import com.github.ele115.tello_wrapper.obstacle.ObstacleGate;
import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoFrame;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoWindow;
import javafx.scene.paint.Color;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.opencv.opencv_core.*;

import java.awt.image.BufferedImage;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_ximgproc.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Required three arguments: x y z");
            System.exit(1);
        }
        int droneX = Integer.parseInt(args[0]);
        int droneY = Integer.parseInt(args[1]);
        int droneZ = Integer.parseInt(args[2]);

        Tello.setWindowSize(960, 720);
        Tello.getSimulator().addObstacle(new ObstacleGate(60, 250, 50, 20, Color.RED));
        // Tello.getSimulator().setNoisy(true);
        var d1 = Tello.Connect("simulator", droneX, droneY, 90);
        var win = new VideoWindow();
        d1.addVideoListener((frame) -> {
            var src = Java2DFrameUtils.toIplImage(frame.getImage());
            var dst = cvCreateImage(cvGetSize(src), src.depth(), 1);
            var colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);
            var storage = cvCreateMemStorage(0);
            var lines = new CvSeq();

            cvInRangeS(src, cvScalar(0, 100, 0, 0), cvScalar(255, 255, 155, 155), dst);
            {
                var m = new Mat(dst);
                medianBlur(m, m, 41);
                // thinning(m, m);
                dst = new IplImage(m);
            }
            // cvCanny(src, dst, 50, 200, 3);
            cvFloodFill(dst, new CvPoint(0, 0), cvScalar(255));
            cvCvtColor(dst, colorDst, CV_GRAY2BGR);

            // lines = cvHoughLines2(dst, storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI / 180, 40, 50, 10, 0, CV_PI);
            // for (int i = 0; i < lines.total(); i++) {
            //     // Based on JavaCPP, the equivalent of the C code:
            //     // CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);
            //     // CvPoint first=line[0], second=line[1]
            //     // is:
            //     var line = cvGetSeqElem(lines, i);
            //     var pt1 = new CvPoint(line).position(0);
            //     var pt2 = new CvPoint(line).position(1);

            //     System.out.println("Line spotted: ");
            //     System.out.println("\t pt1: " + pt1);
            //     System.out.println("\t pt2: " + pt2);
            //     cvLine(colorDst, pt1, pt2, CV_RGB(255, 0, 0), 3, CV_AA, 0); // draw the segment on the image
            // }

            win.onFrameReceived(new TelloVideoFrame(Java2DFrameUtils.toBufferedImage(colorDst), null));
        });
        d1.setStreaming(true);
        d1.takeoff();
        d1.up(droneZ - 50);

        // Fly through a gate
        // d1.forward(-droneY + 400);

        d1.land();
    }

    public static void alignGateAndFrameCenters(ITelloDrone drone, FrameGrabber frameGrabber) {
        int adjustmentDistance = 10;
        BufferedImage img;
        GateDetector gateDetector = new GateDetector(160, 0.05);
        String horizontalMove, verticalMove;
        do {
            frameGrabber.recordAndDisplay();
            img = frameGrabber.getImage(0);
            gateDetector.setImage(img);
            gateDetector.applyFilter();
            gateDetector.detectAllEdges();
            horizontalMove = gateDetector.horizontalMoveDirection();
            verticalMove = gateDetector.verticalMoveDirection();
            System.out.println("Horizontal Direction: " + horizontalMove);
            System.out.println("Vertical Direction: " + verticalMove);
            img = gateDetector.getEdgeImage();
            frameGrabber.displayImage(img, 0);

            if (horizontalMove.equals("left")) {
                System.out.println("Adjustment: left " + adjustmentDistance);
                drone.left(adjustmentDistance);
            } else if (horizontalMove.equals("right")) {
                System.out.println("Adjustment: right" + adjustmentDistance);
                drone.right(adjustmentDistance);
            }

            if (verticalMove.equals("up")) {
                System.out.println("Adjustment: up " + adjustmentDistance);
                drone.up(adjustmentDistance);
            } else if (verticalMove.equals("down")) {
                System.out.println("Adjustment: down" + adjustmentDistance);
                drone.down(adjustmentDistance);
            }
        } while (!horizontalMove.equals("good") || !verticalMove.equals("good"));
    }
}