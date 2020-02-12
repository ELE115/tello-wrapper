/*
 * Copyright 2020 Fritz Windisch
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.ele115.tello_wrapper.tello4j.api.drone;

import com.github.ele115.tello_wrapper.ITelloDrone;
import com.github.ele115.tello_wrapper.tello4j.api.state.StateListener;
import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;
import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoExportType;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoListener;
import com.github.ele115.tello_wrapper.tello4j.api.world.FlipDirection;
import com.github.ele115.tello_wrapper.tello4j.api.world.MovementDirection;
import com.github.ele115.tello_wrapper.tello4j.api.world.TurnDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents all tello drones with their possible sdk actions and their current data.
 * You can construct a tello drone by using a tello drone factory (e.g. {@link com.github.ele115.tello_wrapper.tello4j.api.drone.WifiDroneFactory}).
 *
 * @author Fritz Windisch
 */
public abstract class TelloDrone implements ITelloDrone, AutoCloseable {
    private List<VideoListener> videoListeners = new ArrayList<>();
    private List<StateListener> stateListeners = new ArrayList<>();
    private TelloDroneState cachedState;
    private TelloVideoExportType videoExportType = TelloVideoExportType.BUFFERED_IMAGE;

    /**
     * Disconnects and frees the resources of this tello drone.
     */
    @Override
    public void close() {
        this.disconnect();
    }

    /**
     * Estabilishes a connection with a tello drone at the default address (192.168.10.1). Can only be used once, please construct a new tello drone object
     * when reconnecting.
     * You still need to connect to the tello wifi manually before invoking this call.
     */
    public abstract void connect();

    /**
     * Estabilishes a connection with a tello drone at a custom address. Can only be used once, please construct a new tello drone object
     * when reconnecting.
     * You still need to connect to the tello wifi manually before invoking this call.
     *
     * @param remoteAddr The remote address or hostname your tello uses. If unsure, use {@link #connect()} to connect to the default destination IP.
     */
    public abstract void connect(String remoteAddr);

    /**
     * Disconnects from this tello drone. Does not close this drones resources.
     */
    public abstract void disconnect();

    /**
     * Retrieves the connection state of this drone.
     * When the drone times out after not sending commands for 15 seconds, the drone automatically lands safely and
     * closes the connection.
     *
     * @return true: if drone was already connected, false: otherwise
     */
    public abstract boolean isConnected();

    public abstract void takeoff();

    public abstract void land();

    public abstract boolean isStreaming();

    public abstract void setStreaming(boolean stream);

    public abstract void emergency();

    public abstract void moveDirection(MovementDirection direction, int cm);

    public abstract void turn(TurnDirection direction, int degrees);

    public abstract void flip(FlipDirection direction);

    public abstract void move(int x, int y, int z, int speed);

    public abstract void curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed);

    public abstract void setSpeed(int speed);

    public abstract void sendRemoteControlInputs(int lr, int fb, int ud, int yaw);

    public abstract void setWifiSSIDAndPassword(String ssid, String password);

    public abstract double fetchSpeed();

    public abstract int fetchBattery();

    public abstract int fetchMotorTime();

    public abstract int fetchHeight();

    public abstract int fetchTemperature();

    public abstract int[] fetchAttitude();

    public abstract double fetchBarometer();

    public abstract double[] fetchAcceleration();

    public abstract int fetchTOFDistance();

    public abstract int fetchWifiSnr();

    public void up(int cm) {
        this.moveDirection(MovementDirection.UP, cm);
    }

    public void down(int cm) {
        this.moveDirection(MovementDirection.DOWN, cm);
    }

    public void left(int cm) {
        this.moveDirection(MovementDirection.LEFT, cm);
    }

    public void right(int cm) {
        this.moveDirection(MovementDirection.RIGHT, cm);
    }

    public void forward(int cm) {
        this.moveDirection(MovementDirection.FORWARD, cm);
    }

    public void backward(int cm) {
        this.moveDirection(MovementDirection.BACKWARD, cm);
    }

    public void turnLeft(int degrees) {
        this.turn(TurnDirection.LEFT, degrees);
    }

    public void turnRight(int degrees) {
        this.turn(TurnDirection.RIGHT, degrees);
    }

    public void addVideoListener(VideoListener listener) {
        this.videoListeners.add(listener);
    }

    public boolean removeVideoListener(VideoListener listener) {
        return this.videoListeners.remove(listener);
    }

    public List<VideoListener> getVideoListeners() {
        return videoListeners;
    }

    public void addStateListener(StateListener listener) {
        this.stateListeners.add(listener);
    }

    public boolean removeStateListener(StateListener listener) {
        return this.stateListeners.remove(listener);
    }

    public List<StateListener> getStateListeners() {
        return stateListeners;
    }

    public TelloDroneState getCachedState() {
        return cachedState;
    }

    public void setCachedState(TelloDroneState cachedState) {
        this.cachedState = cachedState;
    }

    public TelloVideoExportType getVideoExportType() {
        return videoExportType;
    }

    public void setVideoExportType(TelloVideoExportType videoExportType) {
        this.videoExportType = videoExportType;
    }
}
