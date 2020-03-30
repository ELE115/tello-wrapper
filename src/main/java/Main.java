import GateDetector.GateDetector;
import com.github.ele115.tello_wrapper.FrameGrabber;
import com.github.ele115.tello_wrapper.ITelloDrone;
import com.github.ele115.tello_wrapper.Tello;
import com.github.ele115.tello_wrapper.obstacle.ObstacleGate;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoWindow;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

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
        Tello.getSimulator().setNoisy(true);
        var d1 = Tello.Connect("simulator", droneX, droneY, 90);
        d1.addVideoListener(new VideoWindow());
        FrameGrabber frameGrabber = new FrameGrabber(1);
        d1.addVideoListener(frameGrabber);
        d1.setStreaming(true);
        d1.takeoff();
        d1.up(droneZ - 50);

        // Fly through a gate
        alignGateAndFrameCenters(d1, frameGrabber);
        d1.forward(-droneY + 400);

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