import com.github.ele115.tello_wrapper.FrameGrabber;
import com.github.ele115.tello_wrapper.ITelloDrone;
import com.github.ele115.tello_wrapper.Tello;
import com.github.ele115.tello_wrapper.obstacle.ObstacleBall;
import com.github.ele115.tello_wrapper.obstacle.ObstacleBox;
import com.github.ele115.tello_wrapper.obstacle.ObstacleCylinder;
import com.github.ele115.tello_wrapper.obstacle.ObstacleGate;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoWindow;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.Scanner;

import GateDetector.GateDetector;

enum FrameObstacleState {
    BEFORE_FIRST, FIRST_BORDER, MIDDLE, SECOND_BORDER, AFTER_SECOND;
}

enum BorderValue {
    LEFT, UPPER, RIGHT, BOTTOM;
}

public class Main {
    private static double droneX = 0;
    private static double droneY = -400;
    private static int droneZ = 100;

    public static void main(String[] args) {
        Tello.setWindowSize(960, 720);
//        Tello.getSimulator().addObstacle(new ObstacleCylinder(150, 200, Color.AQUA));
//        Tello.getSimulator().addObstacle(new ObstacleCylinder(0, 150, Color.ORANGE));
//        Tello.getSimulator().addObstacle(new ObstacleBox(0, 0, 30, Color.LIGHTBLUE));
//        Tello.getSimulator().addObstacle(new ObstacleBall(0, 30, 0, Color.RED));
        Tello.getSimulator().addObstacle(new ObstacleGate(0, 0, 50, 20, Color.RED));
//        Tello.getSimulator().addObstacle(new ObstacleGate(100, 400, 0, 0, Color.GREEN));
//        Tello.getSimulator().addObstacle(new ObstacleGate(0, 800, 0, 40, Color.RED));
        var d1 = Tello.Connect("simulator", droneX, droneY, 90);
//        var d2 = Tello.Connect("simulator", 0, 0, 90);
        d1.addVideoListener(new VideoWindow());
        FrameGrabber frameGrabber = new FrameGrabber(1);
        d1.addVideoListener(frameGrabber);
//        d2.addVideoListener(new VideoWindow());
        d1.setStreaming(true);
//        d2.setStreaming(true);
        d1.takeoff();
        d1.up(droneZ);
//        d2.takeoff();
        interactiveControl(d1, frameGrabber);


//        d2.forward(300);
        d1.land();
//        d2.land();

        System.exit(0);
    }

    public static void interactiveControl(ITelloDrone drone, FrameGrabber frameGrabber) {
        Scanner kb = new Scanner(System.in);
        String cmd;
        int distance;
        BufferedImage[] imgs;
        BufferedImage img;
        GateDetector gateDetector = new GateDetector();

        while (true) {
            System.out.print("> ");
            cmd = kb.next();
            switch (cmd) {
                case "quit":
                    return;
                case "left":
                    distance = kb.nextInt();
                    drone.left(distance);
                    droneX -= distance;
                    break;
                case "right":
                    distance = kb.nextInt();
                    drone.right(distance);
                    break;
                case "forward":
                    distance = kb.nextInt();
                    drone.forward(distance);
                    break;
                case "backward":
                    distance = kb.nextInt();
                    drone.backward(distance);
                    break;
                case "up":
                    distance = kb.nextInt();
                    drone.up(distance);
                    break;
                case "down":
                    distance = kb.nextInt();
                    drone.down(distance);
                    break;
                case "snap":
                    frameGrabber.recordAndDisplay();
                    break;
                case "filter":
                    img = frameGrabber.getImage(0);
                    gateDetector.setImage(img);
                    gateDetector.applyFilter();
                    img = gateDetector.getFilteredFrameImage();
                    frameGrabber.displayImage(img, 0);
                    break;
                case "detect":
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
                            System.out.println("Adjustment: left");
                            drone.left(10);
                        } else if (horizontalMove.equals("right")) {
                            System.out.println("Adjustment: right");
                            drone.right(10);
                        }

                        if (verticalMove.equals("up")) {
                            System.out.println("Adjustment: up");
                            drone.up(10);
                        } else if (verticalMove.equals("down")) {
                            System.out.println("Adjustment: down");
                            drone.down(10);
                        }
                    } while (!horizontalMove.equals("good") || !verticalMove.equals("good"));
                    break;
                default:
                    System.err.println("Wrong command. Try again!");
                    break;
            }
        }
    }

    public static void printDroneLocation() {
        System.out.println("Drone X: " + droneX + " Drone Y: " + droneY);
    }


}