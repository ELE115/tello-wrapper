package com.github.ele115.tello_wrapper.obstacle;

import javafx.scene.Node;

public interface IObstacle {

    boolean check(double x, double y, double z);

    void clear();

    Node getNode();
}
