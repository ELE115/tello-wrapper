package com.github.ele115.tello_wrapper.obstacle;

import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class ObstacleBall extends Obstacle {

    private final double x, y, z;

    public ObstacleBall(double x, double y, double z, Color c) {
        super(new Sphere(15), c);
        this.x = x;
        this.y = y;
        this.z = z;
        o.getTransforms().add(new Translate(y, -15 - z, -x));
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2) + Math.pow(z - this.z, 2) <= Math.pow(15, 2);
    }

    @Override
    public String toString() {
        return "a ball";
    }
}
