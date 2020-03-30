import com.github.ele115.tello_wrapper.FrameGrabber;
import com.github.ele115.tello_wrapper.ITelloDrone;
import com.github.ele115.tello_wrapper.Tello;
import com.github.ele115.tello_wrapper.obstacle.ObstacleGate;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoWindow;
import com.github.ele115.tello_wrapper.tello4j.api.world.FlipDirection;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Requires three arguments: x y z");
            System.exit(1);
        }

        // You may reduce the size of all windows.
        // However, your program is NOT guaranteed to work.
        Tello.setWindowSize(960, 720);

        // Add a gate to the scene.
        Tello.getSimulator().addObstacle(new ObstacleGate(60, 250, 50, 20, Color.RED));

        // Tweak some settings of the simulator
        Tello.getSimulator().setNoisy(true);
        Tello.getSimulator().setOnCollision("hang"); // "hang" or "exit" or "nothing"

        // Uncomment the following lines if you don't have a good video card
        // Tello.getSimulator().setSnapshotInt(300);
        // Tello.getSimulator().setDelayUpdate(true);

        int droneX = Integer.parseInt(args[0]);
        int droneY = Integer.parseInt(args[1]);
        int droneZ = Integer.parseInt(args[2]);

        // The 90 (degrees) here means that the drone is facing forward.
        ITelloDrone drone = Tello.Connect("simulator", droneX, droneY, 90);

        // You may opt-out for this window by commenting out this line.
        drone.addVideoListener(new VideoWindow());

        // ... but you don't want to comment out this line of course
        FrameGrabber frameGrabber = new FrameGrabber(1);
        drone.addVideoListener(frameGrabber);

        drone.setStreaming(true);
        drone.takeoff();
        drone.up(droneZ - 50); // Move to the desired location

        alignGateAndFrameCenters(drone, frameGrabber);

        drone.forward(-droneY + 400); // Fly to the other end of the room
        drone.flip(FlipDirection.FORWARD); // Celebrate

        drone.land();
        System.exit(0);
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
                System.out.println("Adjustment: right " + adjustmentDistance);
                drone.right(adjustmentDistance);
            }

            if (verticalMove.equals("up")) {
                System.out.println("Adjustment: up " + adjustmentDistance);
                drone.up(adjustmentDistance);
            } else if (verticalMove.equals("down")) {
                System.out.println("Adjustment: down " + adjustmentDistance);
                drone.down(adjustmentDistance);
            }
        } while (!horizontalMove.equals("good") || !verticalMove.equals("good"));
    }
}
