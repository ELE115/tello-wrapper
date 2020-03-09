package com.github.ele115.tello_wrapper;

import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoFrame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

public class Tello3D extends Application {
    private static Tello3D me;
    private TelloSimulator sim;
    private Scene scene;
    private BufferedImage snapshot;

    public Tello3D() {
        me = this;
    }

    static Tello3D getInstance() {
        while (me == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        return me;
    }

    void setSimulator(TelloSimulator sim) {
        this.sim = sim;
        sim.addMicroListener((s) -> Platform.runLater(() -> updateDrone(s)));
        var t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(this::makeSnapshot);
            }
        });
        t.start();
    }

    @Override
    public void start(Stage stage) {
        // TODO: create the drone
        // TODO: create the room

        var javaVersion = System.getProperty("java.version");
        var javafxVersion = System.getProperty("javafx.version");
        var l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

        scene = new Scene(new StackPane(l), 960, 720);

        stage.setResizable(false);
        stage.setScene(scene);
        // Uncomment this line to show JavaFX window
        // Remark: no matter what is the case, drone.addVideoListener will work
        // stage.show();
    }

    private void updateDrone(TelloMicroState micro) {
        // TODO: modify drone translation and rotation

        makeSnapshot();
    }

    private synchronized void makeSnapshot() {
        var img = new WritableImage(960, 720);
        scene.snapshot(img);
        var bi = SwingFXUtils.fromFXImage(img, null);
        this.snapshot = bi;
        sim.issueFrame(new TelloVideoFrame(bi, null));
    }
}
