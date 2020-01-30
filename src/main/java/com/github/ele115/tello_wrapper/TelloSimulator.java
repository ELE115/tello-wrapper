package com.github.ele115.tello_wrapper;

import me.friwi.tello4j.api.state.StateListener;
import me.friwi.tello4j.api.state.TelloDroneState;
import me.friwi.tello4j.api.video.TelloVideoExportType;
import me.friwi.tello4j.api.video.VideoListener;
import me.friwi.tello4j.api.world.FlipDirection;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

class TelloSimulator implements ITelloDrone {
    private List<VideoListener> videoListeners = new ArrayList<>();
    private List<StateListener> stateListeners = new ArrayList<>();
    private TelloDroneState cachedState;
    private TelloVideoExportType videoExportType = TelloVideoExportType.BUFFERED_IMAGE;

    private boolean streaming;
    private double x = 0;
    private double y = 0;
    private double yaw = 0;
    private double height = 0;
    private int speed;

    private final boolean traceable;

    protected void updateState() {
        var temp = fetchTemperature();
        var tof = fetchTOFDistance();
        var height = fetchHeight();
        var battery = fetchBattery();
        var motorTime = fetchMotorTime();
        var barometer = fetchBarometer();
        var acc = fetchAcceleration();
        var state = new TelloDroneState(
                0, 0, (int) yaw,
                0, 0, 0,
                temp, temp,
                tof, height,
                battery,
                motorTime,
                barometer,
                acc[0], acc[1], acc[2]
        );
        stateListeners.forEach((sl) -> {
            sl.onStateChanged(cachedState, state);
        });

        if (traceable) {
            try {
                var s = Double.toString(x) + "," + Double.toString(y) + "," +
                        Double.toString(yaw) + "," + Double.toString(height);
                Files.write(Paths.get("./tello-simulator.trace"), s.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("Cannot append to trace file");
            }
        }

        cachedState = state;
    }

    TelloSimulator(boolean trace) {
        traceable = trace;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void takeoff() {
        height = 50;
        updateState();
    }

    @Override
    public void land() {
        height = 0;
        updateState();
    }

    @Override
    public boolean isStreaming() {
        return streaming;
    }

    @Override
    public void setStreaming(boolean stream) {
        streaming = stream;
    }

    @Override
    public void emergency() {
        height = 0;
        updateState();
    }

    @Override
    public void moveDirection(MovementDirection direction, int cm) {
        if (height == 0)
            throw new RuntimeException("Not taken off yet");
        switch (direction) {
            case UP:
                height += cm;
                break;
            case DOWN:
                height -= cm;
                break;
            case FORWARD:
                x += Math.sin(yaw / 180 * Math.PI) * cm;
                y += Math.cos(yaw / 180 * Math.PI) * cm;
                break;
            case BACKWARD:
                x -= Math.sin(yaw / 180 * Math.PI) * cm;
                y -= Math.cos(yaw / 180 * Math.PI) * cm;
                break;
            case LEFT:
                x -= Math.cos(yaw / 180 * Math.PI) * cm;
                y += Math.sin(yaw / 180 * Math.PI) * cm;
                break;
            case RIGHT:
                x += Math.cos(yaw / 180 * Math.PI) * cm;
                y -= Math.sin(yaw / 180 * Math.PI) * cm;
                break;
        }
        updateState();
    }

    @Override
    public void turn(TurnDirection direction, int degrees) {
        if (height == 0)
            throw new RuntimeException("Not taken off yet");
        switch (direction) {
            case LEFT:
                yaw -= degrees;
                break;
            case RIGHT:
                yaw += degrees;
                break;
        }
        updateState();
    }

    @Override
    public void flip(FlipDirection direction) {
        if (height == 0)
            throw new RuntimeException("Not taken off yet");
        switch (direction) {
            case LEFT:
                left(35);
                break;
            case RIGHT:
                right(35);
                break;
            case FORWARD:
                forward(35);
                break;
            case BACKWARD:
                backward(35);
                break;
        }
    }

    @Override
    public void move(int x, int y, int z, int speed) {
        if (height == 0)
            throw new RuntimeException("Not taken off yet");
        this.x += Math.sin(yaw / 180 * Math.PI) * y + Math.cos(yaw / 180 * Math.PI) * x;
        this.y += Math.cos(yaw / 180 * Math.PI) * y - Math.sin(yaw / 180 * Math.PI) * x;
        height += z;
        updateState();
    }

    @Override
    public void curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed) {
        if (height == 0)
            throw new RuntimeException("Not taken off yet");
        x += Math.sin(yaw / 180 * Math.PI) * y2 + Math.cos(yaw / 180 * Math.PI) * x2;
        y += Math.cos(yaw / 180 * Math.PI) * y2 - Math.sin(yaw / 180 * Math.PI) * x2;
        height += z2;
        updateState();
    }

    @Override
    public void setSpeed(int speed) {
        this.speed = speed;
        updateState();
    }

    @Override
    public void sendRemoteControlInputs(int lr, int fb, int ud, int yaw) {
        turnRight(yaw);
        move(lr, fb, ud, 0);
    }

    @Override
    public void setWifiSSIDAndPassword(String ssid, String password) {
        throw new RuntimeException("You should not call this method in simulator.");
    }

    @Override
    public double fetchSpeed() {
        return speed;
    }

    @Override
    public int fetchBattery() {
        return 100;
    }

    @Override
    public int fetchMotorTime() {
        return 0;
    }

    @Override
    public int fetchHeight() {
        return (int) height;
    }

    @Override
    public int fetchTemperature() {
        return 50;
    }

    @Override
    public int[] fetchAttitude() {
        return new int[]{(int) x, (int) y, (int) height};
    }

    @Override
    public double fetchBarometer() {
        return 0;
    }

    @Override
    public double[] fetchAcceleration() {
        return new double[]{0, 0, 0};
    }

    @Override
    public int fetchTOFDistance() {
        return 0;
    }

    @Override
    public int fetchWifiSnr() {
        return 50;
    }

    @Override
    public void up(int cm) {
        this.moveDirection(MovementDirection.UP, cm);
    }

    @Override
    public void down(int cm) {
        this.moveDirection(MovementDirection.DOWN, cm);
    }

    @Override
    public void left(int cm) {
        this.moveDirection(MovementDirection.LEFT, cm);
    }

    @Override
    public void right(int cm) {
        this.moveDirection(MovementDirection.RIGHT, cm);
    }

    @Override
    public void forward(int cm) {
        this.moveDirection(MovementDirection.FORWARD, cm);
    }

    @Override
    public void backward(int cm) {
        this.moveDirection(MovementDirection.BACKWARD, cm);
    }

    @Override
    public void turnLeft(int degrees) {
        this.turn(TurnDirection.LEFT, degrees);
    }

    @Override
    public void turnRight(int degrees) {
        this.turn(TurnDirection.RIGHT, degrees);
    }

    @Override
    public void addVideoListener(VideoListener listener) {
        this.videoListeners.add(listener);
    }

    @Override
    public boolean removeVideoListener(VideoListener listener) {
        return this.videoListeners.remove(listener);
    }

    @Override
    public List<VideoListener> getVideoListeners() {
        return videoListeners;
    }

    @Override
    public void addStateListener(StateListener listener) {
        this.stateListeners.add(listener);
    }

    @Override
    public boolean removeStateListener(StateListener listener) {
        return this.stateListeners.remove(listener);
    }

    @Override
    public List<StateListener> getStateListeners() {
        return stateListeners;
    }

    @Override
    public TelloDroneState getCachedState() {
        return cachedState;
    }

    @Override
    public void setCachedState(TelloDroneState cachedState) {
        this.cachedState = cachedState;
    }

    @Override
    public TelloVideoExportType getVideoExportType() {
        return videoExportType;
    }

    @Override
    public void setVideoExportType(TelloVideoExportType videoExportType) {
        this.videoExportType = videoExportType;
    }
}
