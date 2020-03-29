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

enum FrameObstacleState {
    BEFORE_FIRST, FIRST_BORDER, MIDDLE, SECOND_BORDER, AFTER_SECOND;
}

enum BorderValue {
    LEFT, UPPER, RIGHT, BOTTOM;
}

public class Main {
    public static void main(String[] args) {
        Tello.setWindowSize(960, 720);
//        Tello.getSimulator().addObstacle(new ObstacleCylinder(150, 200, Color.AQUA));
//        Tello.getSimulator().addObstacle(new ObstacleCylinder(0, 150, Color.ORANGE));
//        Tello.getSimulator().addObstacle(new ObstacleBox(0, 0, 30, Color.LIGHTBLUE));
//        Tello.getSimulator().addObstacle(new ObstacleBall(0, 30, 0, Color.RED));
        Tello.getSimulator().addObstacle(new ObstacleGate(50, 80, 0, 30));
        var d1 = Tello.Connect("simulator", 75, 0, 90);
//        var d2 = Tello.Connect("simulator", 0, 0, 90);
        d1.addVideoListener(new VideoWindow());
        FrameGrabber frameGrabber = new FrameGrabber(1);
        d1.addVideoListener(frameGrabber);
//        d2.addVideoListener(new VideoWindow());
        d1.setStreaming(true);
//        d2.setStreaming(true);
        d1.takeoff();
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
                case "applyfilter":
                    imgs = frameGrabber.getImages();
                    for (var img : imgs) {
                        applyFilter(img);
                    }
                    frameGrabber.displayImages(imgs);
                    break;
                case "detect":
                    frameGrabber.recordAndDisplay();
                    imgs = frameGrabber.getImages();
                    for (var img : imgs) {
                        applyFilter(img);
                        markObstacleEdges(img);
                    }
                    frameGrabber.displayImages(imgs);
                    break;
                default:
                    System.err.println("Wrong command. Try again!");
                    break;
            }
        }
    }

    public static void applyFilter(BufferedImage img) {
        java.awt.Color c;
        int r, g, b;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                c = new java.awt.Color(img.getRGB(i, j));
                r = c.getRed() > 50 ? 255 : 0;
                g = 0;
                b = 0;
                img.setRGB(i, j, new java.awt.Color(r, g, b).getRGB());
            }
        }
    }

    public static void markObstacleEdges(BufferedImage img) {
        int avgWinSize = 8;
        int val;
        double upperThreshVal = 255 * 0.8;
        java.awt.Color c;
        int leftBorderTotal, leftBorderCnt;
        int rightBorderTotal, rightBorderCnt;

        leftBorderTotal = leftBorderCnt = 0;
        rightBorderTotal = rightBorderCnt = 0;
        System.out.println("Height: " + img.getHeight() + " Width: " + img.getWidth());
        for (int i = 0; i < img.getHeight(); i++) {
            // detect left border
            RunningAverage leftBorderAvg = new RunningAverage(avgWinSize);
            for (int j = 0; j < img.getWidth() - avgWinSize; j++) {
                c = new java.awt.Color(img.getRGB(j, i));
                val = c.getRed();
                leftBorderAvg.addValue(val);
                if (leftBorderAvg.isFilled()) {
                    if (leftBorderAvg.getAverage() > upperThreshVal) {
                        leftBorderTotal += j;
                        leftBorderCnt++;
                        img.setRGB(j, i, new java.awt.Color(0, 255, 0).getRGB());
                        break;
                    }
                }
            }
            // detect right border
            RunningAverage rightBorderAvg = new RunningAverage(avgWinSize);
            for (int j = img.getWidth() - 1; j > avgWinSize; j--) {
                c = new java.awt.Color(img.getRGB(j, i));
                val = c.getRed();
                rightBorderAvg.addValue(val);
                if (rightBorderAvg.isFilled()) {
                    if (rightBorderAvg.getAverage() > upperThreshVal) {
                        rightBorderTotal += j;
                        rightBorderCnt++;
                        img.setRGB(j, i, new java.awt.Color(0, 255, 0).getRGB());
                        break;
                    }
                }
            }
        }

        int leftBorder = leftBorderTotal / leftBorderCnt;
        int rightBorder = rightBorderTotal / rightBorderCnt;
        System.out.println("Left border: " + leftBorder + " Right Border: " + rightBorder);
        int xMiddle = (leftBorder + rightBorder) / 2;
        if (xMiddle < img.getWidth()/2) {
            System.out.println("Has to move left");
        } else {
            System.out.println("Has to move right");
        }
    }
}

class RunningAverage {
    private int size;
    private int[] wnd;
    private int wrIndex;
    private int sum;
    private int validCnt;

    public RunningAverage(int size) {
        this.size = size;
        wrIndex = 0;
        wnd = new int[size];
        validCnt = 0;
    }

    public void addValue(int val) {
        if (validCnt < size) {
            validCnt++;
            sum += val;
        } else {
            sum = sum - wnd[wrIndex] + val;
        }
        wnd[wrIndex] = val;
        wrIndex = wrIndex == (size - 1) ? 0 : wrIndex + 1;
    }

    public boolean isFilled() {
        return validCnt >= size;
    }

    public double getAverage() {
        return ((double) sum) / size;
    }

}