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

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Required three arguments: x y z");
            System.exit(1);
        }
        double droneX = Double.parseDouble(args[0]);
        double droneY = Double.parseDouble(args[1]);
        double droneZ = Double.parseDouble(args[2]);

        Tello.setWindowSize(960, 720);
        Tello.getSimulator().addObstacle(new ObstacleGate(0, 0, 50, 20, Color.RED));
        var d1 = Tello.Connect("simulator", droneX, droneY, 90);
        d1.addVideoListener(new VideoWindow());
        FrameGrabber frameGrabber = new FrameGrabber(1);
        d1.addVideoListener(frameGrabber);
        d1.setStreaming(true);
        d1.takeoff();
        d1.up((int)(droneZ-50));

        // Fly through a gate
        alignGateAndFrameCenters(d1, frameGrabber);
        d1.forward((int)(-droneY+100));

        d1.land();
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

    public static void interactiveControl(ITelloDrone drone, FrameGrabber frameGrabber) {
        Scanner kb = new Scanner(System.in);
        String cmd;
        int distance;
        BufferedImage[] imgs;
        BufferedImage img;
        GateDetector gateDetector = new GateDetector(160, 0.05);

        while (true) {
            System.out.print("> ");
            cmd = kb.next();
            switch (cmd) {
                case "quit":
                    return;
                case "left":
                    distance = kb.nextInt();
                    drone.left(distance);
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
//                        img = gateDetector.getFilteredFrameImage();
//                        frameGrabber.displayImage(img, 0);
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
//                    } while (false);
                    } while (!horizontalMove.equals("good") || !verticalMove.equals("good"));
                    break;
                default:
                    System.err.println("Wrong command. Try again!");
                    break;
            }
        }
    }

}