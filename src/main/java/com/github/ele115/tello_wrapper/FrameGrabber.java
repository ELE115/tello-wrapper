package com.github.ele115.tello_wrapper;

import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoFrame;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoListener;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoWindow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.concurrent.atomic.AtomicBoolean;

public class FrameGrabber implements VideoListener {
    private static int framesToSave;
    private BufferedImage[] frameBuffer;
    private VideoWindow[] videoWindows;
    private AtomicBoolean recording = new AtomicBoolean(false);

    public FrameGrabber(int bufferSize) {
        frameBuffer = new BufferedImage[bufferSize];
        videoWindows = new VideoWindow[bufferSize];
        recording.set(false);
        String windowName;

        for (int i = 0; i < frameBuffer.length; i++) {
            windowName = "frame_" + i;
            videoWindows[i] = new VideoWindow(windowName);
        }
    }

    public void recordFrameBuffer() {
        recording.set(true);
        framesToSave = frameBuffer.length;
    }

    public void recordAndDisplay() {
        this.recordFrameBuffer();
        while (recording.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        this.displayBufferedFrames();
    }

    public void displayBufferedFrames() {
        for (int i = 0; i < frameBuffer.length; i++) {
            videoWindows[i].setFrame(new TelloVideoFrame(frameBuffer[i]));
        }
    }

    public void displayImages(BufferedImage[] images) {
        this.setImages(images);
        this.displayBufferedFrames();
    }

    public BufferedImage[] getImages() {
        return frameBuffer;
    }

    /**
     * Get image pixels
     *
     * @param frameNumber which frame
     * @return a three dimensional array where the first two indices are the the x location and y location
     * the third is the color chanel R, G, B where R is index zero, G is index 1 and R is index 2
     * note that the array is int[x][y][colorchannel] where color channel is {0, 1, 2}
     */
    public int[][][] getImageArray(int frameNumber) {
        int[][][] theArray = new int[frameBuffer[frameNumber].getWidth()][frameBuffer[frameNumber].getHeight()][3];
        for (int i = 0; i < frameBuffer[frameNumber].getWidth(); i++) {
            for (int j = 0; j < frameBuffer[frameNumber].getHeight(); j++) {
                Color pixel = new Color(frameBuffer[frameNumber].getRGB(i, j));
                theArray[i][j][0] = pixel.getRed();
                theArray[i][j][1] = pixel.getGreen();
                theArray[i][j][2] = pixel.getBlue();
            }
        }
        return theArray;
    }

    /**
     * Set image pixels
     *
     * @param frameNumber which frame
     * @param theImage    the three dimensional array you've modified
     */
    public void setImageArray(int frameNumber, int[][][] theImage) {
        BufferedImage tempBufferedImage = new BufferedImage(theImage.length, theImage[0].length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < tempBufferedImage.getWidth(); i++) {
            for (int j = 0; j < tempBufferedImage.getHeight(); j++) {
                Color pixel = new Color(theImage[i][j][0], theImage[i][j][1], theImage[i][j][2]);
                tempBufferedImage.setRGB(i, j, pixel.getRGB());
            }
        }
        frameBuffer[frameNumber] = tempBufferedImage;
    }

    public void setImages(BufferedImage[] images) {
        frameBuffer = images;
    }

    @Override
    public void onFrameReceived(TelloVideoFrame frame) {
        if (framesToSave > 0) {
            System.err.println("Recording frame #" + framesToSave);
            frameBuffer[frameBuffer.length - framesToSave] = deepCopyBufferedImage(frame.getImage());
            framesToSave--;
            if (framesToSave == 0) {
                recording.set(false);
            }
        }
    }

    private static BufferedImage deepCopyBufferedImage(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
