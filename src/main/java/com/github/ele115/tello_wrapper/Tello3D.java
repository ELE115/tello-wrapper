package com.github.ele115.tello_wrapper;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Tello3D extends Application {
    private static Tello3D me;
    private TelloSimulator sim;

    public Tello3D() {
        me = this;
    }

    public static Tello3D getInstance() {
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
    }

    @Override
    public void start(Stage stage) {
        var javaVersion = System.getProperty("java.version");
        var javafxVersion = System.getProperty("javafx.version");
        var l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        var scene = new Scene(new StackPane(l), 640, 480);
        stage.setScene(scene);
        stage.show();
    }
}
