package com.github.ele115.tello_wrapper.obstacle;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;

public abstract class Obstacle implements IObstacle {

    protected final Shape3D o;
    private final Color c;
    private final PhongMaterial m;
    private int state = 0;

    public Obstacle(Shape3D o, Color c) {
        this.o = o;
        this.c = c;
        this.m = new PhongMaterial(c);
        this.m.setSpecularColor(c);
        o.setMaterial(this.m);
    }

    public abstract boolean contains(double x, double y, double z);

    @Override
    public boolean check(double x, double y, double z) {
        if (!contains(x, y, z)) {
            return false;
        }
        if (++state % 4 < 3) {
            m.setSpecularPower(8);
        } else {
            m.setSpecularPower(3);
        }
        return true;
    }

    @Override
    public void clear() {
        m.setDiffuseColor(c);
        m.setSpecularPower(Double.MAX_VALUE);
    }

    @Override
    public final Node getNode() {
        return o;
    }
}
