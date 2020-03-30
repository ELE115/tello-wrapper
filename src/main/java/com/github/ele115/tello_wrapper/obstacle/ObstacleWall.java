package com.github.ele115.tello_wrapper.obstacle;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;

public class ObstacleWall extends Obstacle {

    private final double v, shift;
    private final int dir;

    public ObstacleWall(double v, int dir, double shift, Color c) {
        super(new Box(), c);
        switch (dir) {
            case 0: // z < v
            case 1: // z > v
                ((Box) o).setWidth(680);
                ((Box) o).setHeight(0.1);
                ((Box) o).setDepth(680);
                o.getTransforms().add(new Translate(shift, -v, 0));
                ((PhongMaterial) o.getMaterial()).setDiffuseMap(new Image("/htree.png"));
                break;
            case 2: // x < v
            case 3: // x > v
                ((Box) o).setWidth(680);
                ((Box) o).setHeight(200);
                ((Box) o).setDepth(0.1);
                o.getTransforms().add(new Translate(shift, -100, -v));
                break;
            case 4: // y < v
            case 5: // y > v
                ((Box) o).setWidth(0.1);
                ((Box) o).setHeight(200);
                ((Box) o).setDepth(680);
                o.getTransforms().add(new Translate(shift + v, -100, 0));
                break;
            default:
                throw new RuntimeException("dir not allowed");
        }
        this.v = v;
        this.shift = shift;
        this.dir = dir;
    }

    @Override
    public boolean contains(double x, double y, double z) {
        switch (dir) {
            case 0: // z < v
                return z <= v;
            case 1: // z > v
                return z >= v;
            case 2: // x < v
                return x <= v;
            case 3: // x > v
                return x >= v;
            case 4: // y < v
                return y <= shift + v;
            case 5: // y > v
                return y >= shift + v;
            default:
                throw new RuntimeException("dir not allowed");
        }
    }

    @Override
    public String toString() {
        return "a wall";
    }
}
