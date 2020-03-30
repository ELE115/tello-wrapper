package GateDetector;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class GateDetector {
    private BufferedImage frame;
    private int[][] frameFiltered;
    private int[][] gateEdges;
    private int frameWidth;
    private int frameHeight;
    private static int CHANNEL_THRES = 160;
    private int leftEdge;
    private int rightEdge;
    private int upperEdge;
    private int bottomEdge;
    private double alignmentError = 0.05;


    public GateDetector() {
    }

    public void setImage(BufferedImage frame) {
        this.frame = frame;
        this.frameWidth = frame.getWidth();
        this.frameHeight = frame.getHeight();
        this.frameFiltered = new int[frameHeight][frameWidth];
        this.gateEdges = new int[frameHeight][frameWidth];
    }

    public void applyFilter() {
        java.awt.Color c;
        int val;
        for (int i = 0; i < frameWidth; i++) {
            for (int j = 0; j < frameHeight; j++) {
                c = new java.awt.Color(frame.getRGB(i, j));
                val = c.getRed() > CHANNEL_THRES ? 1 : 0;
                frameFiltered[j][i] = val;
            }
        }
    }

    private BufferedImage getImageFromArray(int[][] a) {
        Color c;
        int midWidthMin = (int)((1-alignmentError)*frameWidth/2);
        int midWidthMax = (int)((1+alignmentError)*frameWidth/2);
        int midHeightMin = (int)((1-alignmentError)*frameHeight/2);
        int midHeightMax = (int)((1+alignmentError)*frameHeight/2);
        int gateMidWidthMin = getMidX() - 5;
        int gateMidWidthMax = getMidX() + 5;
        int gateMidHeightMin = getMidY() - 5;
        int gateMidHeightMax = getMidY() + 5;

        System.out.println("Min: " + midWidthMin + " Max: " + midWidthMax);
        BufferedImage img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < frameHeight; i++) {
            for (int j = 0; j < frameWidth; j++) {
                if (a[i][j] == 1)
                    c = Color.BLACK;
                else if (j >= gateMidWidthMin && j <= gateMidWidthMax && i >= gateMidHeightMin && i <= gateMidHeightMax)
                    c = Color.RED;
                else if (j >= midWidthMin && j <= midWidthMax && i >= midHeightMin && i <= midHeightMax)
                    c = Color.YELLOW;
                else
                    c = Color.WHITE;
                img.setRGB(j, i, c.getRGB());
            }
        }
        return img;
    }

    public BufferedImage getEdgeImage() {
        return getImageFromArray(gateEdges);
    }

    public BufferedImage getFilteredFrameImage() {
        return getImageFromArray(frameFiltered);
    }

    public void detectAllEdges() {
        leftEdge = getEdgeStart("left");
        rightEdge = getEdgeStart("right");
        upperEdge = getEdgeStart("upper");
        bottomEdge = getEdgeStart("bottom");
        System.out.println("Left: " + leftEdge + " Right: " + rightEdge + " Upper: " + upperEdge + " Bottom: " + bottomEdge);
    }

    public String horizontalMoveDirection() {
        int gateMid, frameMid;

        gateMid = (getEdgeStart("left") + getEdgeStart("right")) / 2;
        frameMid = frameWidth / 2;
        if (gateMid < (1-alignmentError)*frameMid) {
            return "left";
        }
        else if (gateMid > (1+alignmentError)*frameMid){
            return "right";
        }
        else {
            return "good";
        }
    }

    public String verticalMoveDirection() {
        int gateMid, frameMid;

        gateMid = (getEdgeStart("upper") + getEdgeStart("bottom")) / 2;
        frameMid = frameHeight / 2;
        if (gateMid < (1-alignmentError)*frameMid) {
            return "up";
        }
        else if (gateMid > (1+alignmentError)*frameMid){
            return "down";
        }
        else {
            return "good";
        }
    }

    public int getEdgeStart(String edge) {
        int outerMax;
        int innerStart, innerEnd, innerIncr;
        int avgWinSize = 16;
        int val;

        if (edge.equals("left")) {
            outerMax = frameHeight;
            innerStart = 0;
            innerEnd = frameWidth - 1;
            innerIncr = 1;
        } else if (edge.equals("right")) {
            outerMax = frameHeight;
            innerStart = frameWidth - 1;
            innerEnd = 0;
            innerIncr = -1;
        } else if (edge.equals("upper")) {
            outerMax = frameWidth;
            innerStart = 0;
            innerEnd = frameHeight - 1;
            innerIncr = 1;
        } else if (edge.equals("bottom")) {
            outerMax = frameWidth;
            innerStart = frameHeight - 1;
            innerEnd = 0;
            innerIncr = -1;
        } else {
            System.err.println("Unknown edge: " + edge);
            innerStart = 0;
            innerEnd = 0;
            innerIncr = 0;
            outerMax = 0;
        }

        int edgeTotal = 0;
        int edgeCnt = 0;
        double upperThreshVal = 1 * 0.8;
        boolean inRowDetection = edge.equals("left") || edge.equals("right");
        for (int i = 0; i != outerMax; i++) {
            // detect first edge of an obstacle
            SlidingAverage slidingAverage = new SlidingAverage(avgWinSize);
            for (int j = innerStart; j != innerEnd; j += innerIncr) {
                if (inRowDetection) {
                    val = frameFiltered[i][j];
                } else {
                    val = frameFiltered[j][i];
                }
                slidingAverage.addValue(val);
                if (slidingAverage.isFilled()) {
                    if (slidingAverage.getAverage() > upperThreshVal) {
                        edgeTotal += j;
                        edgeCnt++;
                        if (inRowDetection)
                            gateEdges[i][j] = 1;
                        else
                            gateEdges[j][i] = 1;
                        break;
                    }
                }
            }
        }

        return edgeTotal / edgeCnt;
    }

    private int getMidX() {
        return (leftEdge + rightEdge) / 2;
    }

    private int getMidY() {
        return (upperEdge + bottomEdge) / 2;
    }
}
