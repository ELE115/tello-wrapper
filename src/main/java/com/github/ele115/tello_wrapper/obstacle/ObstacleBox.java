package com.github.ele115.tello_wrapper.obstacle;

import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;

public class ObstacleBox extends Obstacle {

    private final double x, y, z;

    public ObstacleBox(double x, double y, double z, Color c) {
        super(new Box(30, 30, 30), c);
        this.x = x;
        this.y = y;
        this.z = z;
        o.getTransforms().add(new Translate(y, -15 - z, -x));
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return Math.abs(x - this.x) <= 30.0 / 2 && Math.abs(y - this.y) <= 30.0 / 2 && Math.abs(z - 15 - this.z) <= 30.0 / 2;
    }

    @Override
    public String toString() {
        return "a box";
    }
}
