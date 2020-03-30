package com.github.ele115.tello_wrapper;

import com.github.ele115.tello_wrapper.obstacle.IObstacle;
import com.github.ele115.tello_wrapper.obstacle.ObstacleWall;
import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoFrame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Tello3D extends Application {
    private static final AtomicReference<Tello3D> me = new AtomicReference<>(null);
    private static final int droneWidth = 960, droneHeight = 720;
    private static int defaultWidth = 960, defaultHeight = 720;
    private boolean delayUpdate = false;
    private boolean noisy = false;
    private String onCollision = null;
    private final List<Drone> drones = new ArrayList<>();
    private final List<IObstacle> obstacles = new ArrayList<>();
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

    public static void setWindowSize(int w, int h) {
        defaultWidth = w;
        defaultHeight = h;
    }

    public void setDelayUpdate(boolean v) {
        delayUpdate = v;
    }

    public void setNoisy(boolean v) {
        noisy = v;
    }

    public void setOnCollision(String v) {
        onCollision = v;
    }

    void addSimulator(TelloSimulator sim) {
        Platform.runLater(() -> this.drones.add(new Drone(sim)));
    }

    private int snapshotInt = 50;

    public void setSnapshotInt(int i) {
        this.snapshotInt = i;
    }

    private final static double SCALE_FACTOR = 1.9; // pixels / cm
    private final static double SHIFT = 170; // position of drone w.r.t. room center
    private final static double COLLIDE_BOX = 25; // length and width of a drone
    private Group universe, uobstacles;

    private class Drone {
        private TelloSimulator sim;
        private Translate droneTranslate;
        private Rotate droneXRotate;
        private Rotate droneYRotate;
        private Rotate droneZRotate;
        private SnapshotParameters dronePars;
        private TelloMicroState micro;

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
                c.setFieldOfView(50);
                c.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
                c.getTransforms().add(new Translate(0, 0, 722));
                c.getTransforms().add(new Translate(-droneWidth / 2.0, -droneHeight / 2.0 - 1.25 * SCALE_FACTOR, 0));
                drone.getChildren().add(c);

                dronePars = new SnapshotParameters();
                dronePars.setCamera(c);
                dronePars.setFill(Color.LIGHTGRAY);
                dronePars.setViewport(new Rectangle2D(0, 0, droneWidth, droneHeight));
                dronePars.setDepthBuffer(true);
            }
            droneXRotate = new Rotate(0, Rotate.X_AXIS);
            droneYRotate = new Rotate(0, Rotate.Y_AXIS);
            droneZRotate = new Rotate(0, Rotate.Z_AXIS);
            droneTranslate = new Translate(0, 0, 0);
            drone.getTransforms().add(new Translate(-SHIFT * SCALE_FACTOR, -1.25 * SCALE_FACTOR, 0));
            drone.getTransforms().add(droneTranslate);
            drone.getTransforms().add(droneYRotate);
            drone.getTransforms().add(droneZRotate);
            drone.getTransforms().add(droneXRotate);

            universe.getChildren().add(drone);

            this.sim = sim;
            sim.addMicroListener((s) -> Platform.runLater(() -> {
                updateDrone(s);
                for (var obj : obstacles)
                    obj.clear();
                var flag = false;
                for (var d : drones)
                    flag |= d.check();
                if (flag) {
                    if ("hang".equals(onCollision)) {
                        sim.hang();
                    } else if ("exit".equals(onCollision)) {
                        System.exit(23);
                    }
                }
            }));
        }

        private void updateDrone(TelloMicroState micro) {
            droneTranslate.setZ(-micro.rX * SCALE_FACTOR);
            droneTranslate.setX(micro.rY * SCALE_FACTOR);
            droneTranslate.setY(-micro.rZ * SCALE_FACTOR);
            droneXRotate.setAngle(micro.roll);
            droneYRotate.setAngle(90 - micro.rAngle);
            droneZRotate.setAngle(-micro.pitch);
            this.micro = micro;
        }

        private boolean checkRotated(IObstacle o, double d0x, double d0y) {
            var d1x = d0x;
            var d1y = Math.cos(Math.toRadians(micro.roll)) * d0y;
            var d1z = Math.sin(Math.toRadians(micro.roll)) * d0y;

            var d2x = Math.cos(Math.toRadians(micro.pitch)) * d1x - Math.sin(Math.toRadians(micro.pitch)) * d1z;
            var d2y = d1y;
            var d2z = Math.sin(Math.toRadians(micro.pitch)) * d1x + Math.cos(Math.toRadians(micro.pitch)) * d1z;

            var d3x = Math.cos(Math.toRadians(micro.rAngle)) * d2x - Math.sin(Math.toRadians(micro.rAngle)) * d2y;
            var d3y = Math.sin(Math.toRadians(micro.rAngle)) * d2x + Math.cos(Math.toRadians(micro.rAngle)) * d2y;
            var d3z = d2z;

            return o.check(micro.rX + d3x, micro.rY + d3y, micro.rZ + d3z);
        }

        public boolean check() {
            if (micro == null)
                return false;

            var dx = COLLIDE_BOX / 2;
            var dy = COLLIDE_BOX / 2 * 0.6;

            var flag = false;
            for (var obj : obstacles) {
                if (checkRotated(obj, dx, dy)
                        || checkRotated(obj, dy, dx)
                        || checkRotated(obj, dx, -dy)
                        || checkRotated(obj, dy, -dx)
                        || checkRotated(obj, -dx, dy)
                        || checkRotated(obj, -dy, dx)
                        || checkRotated(obj, -dx, -dy)
                        || checkRotated(obj, -dy, -dx)) {
                    System.err.println("Your drone hits " + obj);
                    flag = true;
                }
            }
            return flag;
        }

        private Random rnd = new Random();

        private void makeSnapshot() {
            var img = new WritableImage(droneWidth, droneHeight);
            var snapshot = SwingFXUtils.fromFXImage(universe.snapshot(dronePars, img), null);
            if (noisy) {
                for (var i = 0; i < snapshot.getWidth(); i++)
                    for (var j = 0; j < snapshot.getHeight(); j++) {
                        var rgb = snapshot.getRGB(i, j);
                        if (rnd.nextDouble() > 0.1)
                            continue;
                        rgb = rnd.nextInt();
                        snapshot.setRGB(i, j, rgb);
                    }
            }
            var frame = new TelloVideoFrame(snapshot, null);
            this.sim.issueFrame(frame);
        }
    }

    @Override
    public void start(Stage stage) {
        // Create the room
        universe = new Group();
        uobstacles = new Group();
        uobstacles.getTransforms().add(new Scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR));
        uobstacles.getTransforms().add(new Translate(-SHIFT, 0, 0));
        universe.getChildren().add(uobstacles);
        addObstacle(new ObstacleWall(-0.1, 0, SHIFT, Color.DARKGRAY)); // Floor
        addObstacle(new ObstacleWall(340, 5, SHIFT, Color.LIGHTGREEN)); // Front
        addObstacle(new ObstacleWall(-340, 4, SHIFT, Color.PINK)); // Back
        addObstacle(new ObstacleWall(-340, 2, SHIFT, Color.LIGHTBLUE)); // Left

        // The main window
        var mainCamera = new PerspectiveCamera();
        var mainX = new AtomicReference<>(0.0);
        var mainY = new AtomicReference<>(0.0);
        var mainMX = new AtomicReference<>(0.0);
        var mainMY = new AtomicReference<>(0.0);
        var mainRotateX = new Rotate(0, Rotate.X_AXIS);
        var mainRotateY = new Rotate(0, Rotate.Y_AXIS);
        mainCamera.setFieldOfView(50);
        mainCamera.getTransforms().add(mainRotateY);
        mainCamera.getTransforms().add(mainRotateX);
        mainCamera.getTransforms().add(new Translate(-defaultWidth / 2.0, -defaultHeight / 2.0 - 100 * SCALE_FACTOR, 0));
        var mainMoveZ = new Translate(0, 0, 0);
        mainCamera.getTransforms().add(mainMoveZ);
        var mainMoveXY = new Translate(0, 0, 0);
        mainCamera.getTransforms().add(mainMoveXY);

        var mainScene = new Scene(universe, defaultWidth, defaultHeight, true);
        mainScene.setFill(Color.LIGHTGRAY);
        mainScene.setCamera(mainCamera);

        mainScene.setOnScroll((ScrollEvent e) -> {
            mainMoveZ.setZ(mainMoveZ.getZ() + e.getDeltaY());
        });

        mainScene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
        });

        mainScene.setOnMouseDragged((MouseEvent me) -> {
            var dx = (mousePosX - me.getSceneX());
            var dy = (mousePosY - me.getSceneY());
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            if (me.isPrimaryButtonDown()) {
                mainX.updateAndGet(v -> v + dy / defaultHeight * 120);
                mainY.updateAndGet(v -> v - dx / defaultWidth * 120);
                if (!delayUpdate) {
                    mainRotateX.setAngle(mainX.get());
                    mainRotateY.setAngle(mainY.get());
                }
            }
            if (me.isSecondaryButtonDown()) {
                mainMX.updateAndGet(v -> v + dx);
                mainMY.updateAndGet(v -> v + dy);
                if (!delayUpdate) {
                    mainMoveXY.setX(mainMX.get());
                    mainMoveXY.setY(mainMY.get());
                }
            }
        });

        mainScene.setOnMouseReleased((MouseEvent me) -> {
            mainRotateX.setAngle(mainX.get());
            mainRotateY.setAngle(mainY.get());
            mainMoveXY.setX(mainMX.get());
            mainMoveXY.setY(mainMY.get());
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

    public void addObstacle(IObstacle o) {
        Platform.runLater(() -> {
            obstacles.add(o);
            uobstacles.getChildren().add(o.getNode());
        });
    }
}
