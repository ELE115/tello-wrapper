package com.github.ele115.alavrov.lab4;

import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoFrame;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoListener;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoWindow;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.concurrent.atomic.AtomicBoolean;

public class FrameGrabber implements VideoListener
{
    private static int framesToSave;
    private BufferedImage[] frameBuffer;
    private VideoWindow[] videoWindows;
    private AtomicBoolean recording = new AtomicBoolean(false);

    public FrameGrabber(int bufferSize)
    {
        frameBuffer = new BufferedImage[bufferSize];
        videoWindows = new VideoWindow[bufferSize];
        recording.set(false);
        String window_name;

        for (int i = 0; i < frameBuffer.length; i++) {
            window_name = "frame_" + i;
            videoWindows[i] = new VideoWindow(window_name);
        }
    }

    public void recordFrameBuffer()
    {
        recording.set(true);
        framesToSave = frameBuffer.length;
    }

    public void recordAndDisplay()
    {
        this.recordFrameBuffer();
        while (recording.get())
            ;
        this.displayBufferedFrames();
    }

    public void displayBufferedFrames()
    {
        for (int i = 0; i < frameBuffer.length; i++) {
            videoWindows[i].setFrame(new TelloVideoFrame(frameBuffer[i]));
        }
    }

    public void displayImages(BufferedImage[] images)
    {
        this.setImages(images);
        this.displayBufferedFrames();
    }

    public BufferedImage[] getImages()
    {
        return frameBuffer;
    }

    public void setImages(BufferedImage[] images)
    {
        frameBuffer = images;
    }

    @Override
    public void onFrameReceived(TelloVideoFrame frame)
    {
        if (framesToSave > 0)
        {
            System.err.println("Recording frame #" + framesToSave);
            frameBuffer[frameBuffer.length-framesToSave] = deepCopyBufferedImage(frame.getImage());
            framesToSave--;
            if (framesToSave == 0)
            {
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
