package com.github.ele115.tello_wrapper.obstacle;

import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Translate;

public class ObstacleCylinder extends Obstacle {

    private final double x, y;

    public ObstacleCylinder(double x, double y, Color c) {
        super(new Cylinder(30, 200), c);
        this.x = x;
        this.y = y;
        o.getTransforms().add(new Translate(y, -100, -x));
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2) <= Math.pow(30, 2);
    }

    @Override
    public String toString() {
        return "a cylinder";
    }
}
