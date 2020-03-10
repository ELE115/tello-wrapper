package com.github.ele115.tello_wrapper;

import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoFrame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Tello3D extends Application {
    private static final AtomicReference<Tello3D> me = new AtomicReference<>(null);
    private final List<Drone> drones = new ArrayList<>();
    private double mousePosX, mousePosY;

    synchronized static Tello3D getInstance() {
        var m = me.get();
        if (m != null)
            return m;

        var t = new Thread(() -> Application.launch(Tello3D.class));
        t.start();

        while ((m = me.get()) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        return m;
    }

    void addSimulator(TelloSimulator sim) {
        Platform.runLater(() -> this.drones.add(new Drone(sim)));
    }

    private int snapshotInt = 50;

    public void setSnapshotInt(int i) {
        this.snapshotInt = i;
    }

    private final static double SCALE_FACTOR = 1.9; // pixels / cm
    private Group universe;

    private class Drone {
        private TelloSimulator sim;
        private Translate droneTranslate;
        private Rotate droneYRotate;
        private SnapshotParameters dronePars;

        public Drone(TelloSimulator sim) {
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
            drone.getTransforms().add(new Translate(-160 * SCALE_FACTOR, -1.25 * SCALE_FACTOR, 0));
            drone.getTransforms().add(droneTranslate);
            drone.getTransforms().add(droneYRotate);

            universe.getChildren().add(drone);

            this.sim = sim;
            sim.addMicroListener((s) -> Platform.runLater(() -> updateDrone(s)));
        }

        private void updateDrone(TelloMicroState micro) {
            droneTranslate.setZ(-micro.rX * SCALE_FACTOR);
            droneTranslate.setX(micro.rY * SCALE_FACTOR);
            droneTranslate.setY(-micro.rZ * SCALE_FACTOR);
            droneYRotate.setAngle(90 - micro.rAngle);
        }

        private void makeSnapshot() {
            var img = new WritableImage(960, 720);
            BufferedImage snapshot = SwingFXUtils.fromFXImage(universe.snapshot(dronePars, img), null);
            var frame = new TelloVideoFrame(snapshot, null);
            this.sim.issueFrame(frame);
        }
    }

    @Override
    public void start(Stage stage) {
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

        // The main window
        var mainCamera = new PerspectiveCamera();
        var mainX = new AtomicReference<>(0.0);
        var mainY = new AtomicReference<>(0.0);
        var mainRotateX = new Rotate(0, Rotate.X_AXIS);
        var mainRotateY = new Rotate(0, Rotate.Y_AXIS);
        mainCamera.getTransforms().add(new Translate(-960.0 / 2, -720.0 / 2 - 100 * SCALE_FACTOR, 0));
        mainCamera.getTransforms().add(mainRotateX);
        mainCamera.getTransforms().add(mainRotateY);

        var mainScene = new Scene(universe, 960, 720, true);
        mainScene.setFill(Color.BLACK);
        mainScene.setCamera(mainCamera);

        mainScene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
        });

        mainScene.setOnMouseDragged((MouseEvent me) -> {
            var dx = (mousePosX - me.getSceneX());
            var dy = (mousePosY - me.getSceneY());
            if (me.isPrimaryButtonDown()) {
                mainX.updateAndGet(v -> v + dy / 720 * 120);
                mainY.updateAndGet(v -> v - dx / 960 * 120);
            }
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
        });

        mainScene.setOnMouseReleased((MouseEvent me) -> {
            mainRotateX.setAngle(mainX.get());
            mainRotateY.setAngle(mainY.get());
        });

        stage.setResizable(false);
        stage.setTitle("F111");
        stage.setScene(mainScene);
        stage.show();

        me.set(this);

        var t = new Thread(() -> {
            while (true) {
                try {
                    if (snapshotInt > 0)
                        Thread.sleep(snapshotInt);
                    else
                        Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                if (snapshotInt > 0)
                    Platform.runLater(() -> this.drones.forEach(Drone::makeSnapshot));
            }
        });
        t.start();
    }

    public void addObstacle(double x, double y, Color c) {
        Platform.runLater(() -> {
            var o = new Cylinder(30 * SCALE_FACTOR, 200 * SCALE_FACTOR);
            o.getTransforms().add(new Translate(-160 * SCALE_FACTOR, -100 * SCALE_FACTOR, 0));
            o.getTransforms().add(new Translate(y * SCALE_FACTOR, 0, -x * SCALE_FACTOR));
            o.setMaterial(new PhongMaterial(c));
            universe.getChildren().add(o);
        });
    }
}
