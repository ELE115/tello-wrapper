package com.github.ele115.tello_wrapper.obstacle;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class ObstacleGate implements IObstacle {

    private final static double GHEIGHT = 120;
    private final static double GWIDTH = 120;
    private final static double BLOCKWIDTH = 20;

    private final double x, y, z, angle;
    private final Group g;
    private final PhongMaterial m;
    private int state = 0;

    public ObstacleGate(double x, double y, double z, double angle) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
        this.g = new Group();
        this.m = new PhongMaterial();
        this.m.setDiffuseMap(new Image("/brick.jpg"));
        this.m.setSpecularColor(Color.DARKRED);

        this.g.getTransforms().add(new Translate(y, -GHEIGHT / 2 - z, -x));
        this.g.getTransforms().add(new Rotate(90 - angle, Rotate.Y_AXIS));

        {
            var o = new Box(BLOCKWIDTH, GHEIGHT, BLOCKWIDTH);
            o.setTranslateX(-GWIDTH / 2 + BLOCKWIDTH / 2);
            o.setMaterial(this.m);
            this.g.getChildren().add(o);
        }
        {
            var o = new Box(BLOCKWIDTH, GHEIGHT, BLOCKWIDTH);
            o.setTranslateX(GWIDTH / 2 - BLOCKWIDTH / 2);
            o.setMaterial(this.m);
            this.g.getChildren().add(o);
        }
        {
            var o = new Box(GWIDTH - BLOCKWIDTH * 2, BLOCKWIDTH, BLOCKWIDTH);
            o.setTranslateY(GHEIGHT / 2 - BLOCKWIDTH / 2);
            o.setMaterial(this.m);
            this.g.getChildren().add(o);
        }
        {
            var o = new Box(GWIDTH - BLOCKWIDTH * 2, BLOCKWIDTH, BLOCKWIDTH);
            o.setTranslateY(-GHEIGHT / 2 + BLOCKWIDTH / 2);
            o.setMaterial(this.m);
            this.g.getChildren().add(o);
        }
    }

    public boolean contains(double x, double y, double z) {
        var x0 = -Math.sin(Math.toRadians(angle)) * (x - this.x) + Math.cos(Math.toRadians(angle)) * (y - this.y);
        var y0 = Math.cos(Math.toRadians(angle)) * (x - this.x) + Math.sin(Math.toRadians(angle)) * (y - this.y);
        return Math.abs(x0) <= BLOCKWIDTH / 2
                && Math.abs(y0) <= GWIDTH / 2
                && Math.abs(z - GHEIGHT / 2 - this.z) <= GHEIGHT / 2
                && !(Math.abs(y0) < GWIDTH / 2 - BLOCKWIDTH
                && Math.abs(z - GHEIGHT / 2 - this.z) < GHEIGHT / 2 - BLOCKWIDTH);
    }

    @Override
    public String toString() {
        return "a gate";
    }

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
        m.setSpecularPower(Double.MAX_VALUE);
    }

    @Override
    public Node getNode() {
        return g;
    }
}
