package com.github.ele115.tello_wrapper;

import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoFrame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class Tello3D extends Application {
    private static Tello3D me;
    private TelloSimulator sim;

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
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(this::makeSnapshot);
            }
        });
        t.start();
    }

    private final static double SCALE_FACTOR = 1.9; // pixels / cm
    private Translate droneTranslate;
    private Rotate droneYRotate;
    private Group universe;
    private SnapshotParameters dronePars;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void start(Stage stage) {
        // Create the drone
        var drone = new Group();
        {
            var f = new Box(15 * SCALE_FACTOR, 2.5 * SCALE_FACTOR, 16 * SCALE_FACTOR);
            drone.getChildren().add(f);
        }
        {
            var f = new Cylinder(5 * SCALE_FACTOR, 1.5 * SCALE_FACTOR);
            f.setMaterial(new PhongMaterial(Color.BLUE));
            f.setTranslateX(7.5 * SCALE_FACTOR);
            f.setTranslateY(-1.25 * SCALE_FACTOR);
            f.setTranslateZ(7.5 * SCALE_FACTOR);
            drone.getChildren().add(f);
        }
        {
            var f = new Cylinder(5 * SCALE_FACTOR, 1.5 * SCALE_FACTOR);
            f.setMaterial(new PhongMaterial(Color.WHITE));
            f.setTranslateX(-7.5 * SCALE_FACTOR);
            f.setTranslateY(-1.25 * SCALE_FACTOR);
            f.setTranslateZ(7.5 * SCALE_FACTOR);
            drone.getChildren().add(f);
        }
        {
            var f = new Cylinder(5 * SCALE_FACTOR, 1.5 * SCALE_FACTOR);
            f.setMaterial(new PhongMaterial(Color.BLUE));
            f.setTranslateX(7.5 * SCALE_FACTOR);
            f.setTranslateY(-1.25 * SCALE_FACTOR);
            f.setTranslateZ(-7.5 * SCALE_FACTOR);
            drone.getChildren().add(f);
        }
        {
            var f = new Cylinder(5 * SCALE_FACTOR, 1.5 * SCALE_FACTOR);
            f.setMaterial(new PhongMaterial(Color.WHITE));
            f.setTranslateX(-7.5 * SCALE_FACTOR);
            f.setTranslateY(-1.25 * SCALE_FACTOR);
            f.setTranslateZ(-7.5 * SCALE_FACTOR);
            drone.getChildren().add(f);
        }
        {
            var c = new PerspectiveCamera();
            c.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
            c.getTransforms().add(new Translate(0, 0, 1250));
            c.getTransforms().add(new Translate(-960.0 / 2, -720.0 / 2 - 1.25 * SCALE_FACTOR, 0));
            drone.getChildren().add(c);

            dronePars = new SnapshotParameters();
            dronePars.setCamera(c);
            dronePars.setFill(Color.BLACK);
            dronePars.setViewport(new Rectangle2D(0, 0, 960, 720));
            dronePars.setDepthBuffer(true);
        }
        droneYRotate = new Rotate(0, Rotate.Y_AXIS);
        droneTranslate = new Translate(0, 0, 0);
        drone.getTransforms().add(droneYRotate);
        drone.getTransforms().add(droneTranslate);
        drone.getTransforms().add(new Translate(-160 * SCALE_FACTOR, 0, 0));

        // Create the room
        // TODO: make it prettier
        universe = new Group();
        {
            var f = new Box(500 * SCALE_FACTOR, 0.1 * SCALE_FACTOR, 500 * SCALE_FACTOR);
            f.setMaterial(new PhongMaterial(Color.DARKGRAY));
            universe.getChildren().add(f);
        }
        {
            var f = new Box(0.1 * SCALE_FACTOR, 200 * SCALE_FACTOR, 500 * SCALE_FACTOR);
            f.setMaterial(new PhongMaterial(Color.LIGHTBLUE));
            f.getTransforms().add(new Translate(250 * SCALE_FACTOR, -100 * SCALE_FACTOR, 0));
            universe.getChildren().add(f);
        }
        {
            var f = new Box(0.1 * SCALE_FACTOR, 200 * SCALE_FACTOR, 500 * SCALE_FACTOR);
            f.setMaterial(new PhongMaterial(Color.RED));
            f.getTransforms().add(new Translate(-250 * SCALE_FACTOR, -100 * SCALE_FACTOR, 0));
            universe.getChildren().add(f);
        }
        {
            var f = new Box(500 * SCALE_FACTOR, 200 * SCALE_FACTOR, 0.1 * SCALE_FACTOR);
            f.setMaterial(new PhongMaterial(Color.PINK));
            f.getTransforms().add(new Translate(0, -100 * SCALE_FACTOR, 250 * SCALE_FACTOR));
            universe.getChildren().add(f);
        }
        universe.getChildren().add(drone);

        // The main window
        var mainCamera = new PerspectiveCamera();
        mainCamera.setTranslateX(-960.0 / 2);
        mainCamera.setTranslateY(-720.0 / 2 - 100 * SCALE_FACTOR);
        mainCamera.setTranslateZ(0);
        var mainScene = new Scene(universe, 960, 720, true);
        mainScene.setFill(Color.BLACK);
        mainScene.setCamera(mainCamera);
        stage.setResizable(false);
        stage.setTitle("F111");
        stage.setScene(mainScene);
        stage.show();

        initialized.set(true);
    }

    private void updateDrone(TelloMicroState micro) {
        droneTranslate.setZ(-micro.x);
        droneTranslate.setX(micro.y);
        droneTranslate.setY(-micro.z);
        droneYRotate.setAngle(micro.yaw);
        makeSnapshot();
    }

    private synchronized void makeSnapshot() {
        if (!initialized.get())
            return;

        var img = new WritableImage(960, 720);
        BufferedImage snapshot = SwingFXUtils.fromFXImage(universe.snapshot(dronePars, img), null);
        sim.issueFrame(new TelloVideoFrame(snapshot, null));
    }
}
