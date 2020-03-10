package com.github.ele115.tello_wrapper;

import com.github.ele115.tello_wrapper.tello4j.api.state.StateListener;
import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;
import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoExportType;
import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoFrame;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoListener;
import com.github.ele115.tello_wrapper.tello4j.api.world.FlipDirection;
import com.github.ele115.tello_wrapper.tello4j.api.world.MovementDirection;
import com.github.ele115.tello_wrapper.tello4j.api.world.TurnDirection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class TelloSimulator implements ITelloDrone {
    private List<VideoListener> videoListeners = new ArrayList<>();
    private List<StateListener> stateListeners = new ArrayList<>();
    private List<Consumer<TelloMicroState>> microListeners = new ArrayList<>();
    private TelloDroneState cachedState;
    private TelloVideoExportType videoExportType = TelloVideoExportType.BUFFERED_IMAGE;

    private boolean streaming;
    private TelloMicroState micro;
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
                0, 0, (int) micro.rAngle,
                0, 0, 0,
                temp, temp,
                tof, height,
                battery,
                motorTime,
                barometer,
                acc[0], acc[1], acc[2]
        );
        stateListeners.forEach((sl) -> sl.onStateChanged(cachedState, state));
        microListeners.forEach((sl) -> sl.accept(micro));

        if (traceable) {
            try {
                var s = micro.rX + "," + micro.rY + "," +
                        micro.rAngle + "," + Double.toString(height) + "\n";
                Files.write(Paths.get("./tello-simulator.trace"), s.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("Cannot append to trace file");
            }
        }

        cachedState = state;
    }

    TelloSimulator(boolean trace) {
        this(trace, new TelloMicroState() {
            {
                rX = 0;
                rY = 0;
                rZ = 0;
                rAngle = 90;
            }
        });
    }

    TelloSimulator(boolean trace, TelloMicroState micro) {
        this.micro = micro;
        this.speed = 100;
        traceable = trace;
        if (traceable) {
            try {
                Files.deleteIfExists(Paths.get("./tello-simulator.trace"));
            } catch (IOException e) {
                throw new RuntimeException("Cannot remove trace file");
            }
        }
        updateState();
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    private static void sleep(long v) {
        try {
            Thread.sleep(v);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void takeoff() {
        if (micro.rZ > 0)
            return;

        for (var i = 0; i < 50; i++) {
            micro.rZ = i;
            updateState();
            sleep(50);
        }
    }

    @Override
    public void land() {
        if (micro.rZ <= 0)
            return;

        while (micro.rZ > 1) {
            micro.rZ--;
            updateState();
            sleep(50);
        }

        micro.rZ = 0;
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
        micro.rZ = 0;
        updateState();
    }

    @Override
    public void moveDirection(MovementDirection direction, int cm) {
        // TODO: call updateState multiple times
        if (micro.rZ == 0)
            throw new RuntimeException("Not taken off yet");
        switch (direction) {
            case UP:
                micro.rZ += cm;
                break;
            case DOWN:
                micro.rZ -= cm;
                break;
            case FORWARD:
                for (var i = 0; i < 100; i++) {
                    micro.rX += Math.cos(micro.rAngle / 180 * Math.PI) * cm / 100;
                    micro.rY += Math.sin(micro.rAngle / 180 * Math.PI) * cm / 100;
                    updateState();
                    sleep(2000 / speed);
                }
                break;
            case BACKWARD:
                micro.rX -= Math.cos(micro.rAngle / 180 * Math.PI) * cm;
                micro.rY -= Math.sin(micro.rAngle / 180 * Math.PI) * cm;
                break;
            case LEFT:
                micro.rX -= Math.sin(micro.rAngle / 180 * Math.PI) * cm;
                micro.rY += Math.cos(micro.rAngle / 180 * Math.PI) * cm;
                break;
            case RIGHT:
                micro.rX += Math.sin(micro.rAngle / 180 * Math.PI) * cm;
                micro.rY -= Math.cos(micro.rAngle / 180 * Math.PI) * cm;
                break;
        }
        updateState();
    }

    @Override
    public void turn(TurnDirection direction, int degrees) {
        // TODO: call updateState multiple times
        if (micro.rZ == 0)
            throw new RuntimeException("Not taken off yet");
        switch (direction) {
            case LEFT:
                micro.rAngle += degrees;
                break;
            case RIGHT:
                micro.rAngle -= degrees;
                break;
        }
        updateState();
    }

    @Override
    public void flip(FlipDirection direction) {
        // TODO: call updateState multiple times
        if (micro.rZ == 0)
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
        // TODO: call updateState multiple times
        if (micro.rZ == 0)
            throw new RuntimeException("Not taken off yet");
        micro.rX += -Math.sin(micro.rAngle / 180 * Math.PI) * y + Math.cos(micro.rAngle / 180 * Math.PI) * x;
        micro.rY += Math.cos(micro.rAngle / 180 * Math.PI) * y + Math.sin(micro.rAngle / 180 * Math.PI) * x;
        micro.rZ += z;
        updateState();
    }

    @Override
    public void curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed) {
        // TODO: call updateState multiple times
        if (micro.rZ == 0)
            throw new RuntimeException("Not taken off yet");
        micro.rX += -Math.sin(micro.rAngle / 180 * Math.PI) * y2 + Math.cos(micro.rAngle / 180 * Math.PI) * x2;
        micro.rY += Math.cos(micro.rAngle / 180 * Math.PI) * y2 + Math.sin(micro.rAngle / 180 * Math.PI) * x2;
        micro.rZ += z2;
        updateState();
    }

    @Override
    public void setSpeed(int speed) {
        this.speed = speed;
        updateState();
    }

    @Override
    public void sendRemoteControlInputs(int lr, int fb, int ud, int yaw) {
        // TODO: call updateState multiple times
        turnRight(yaw);
        move(lr, fb, ud, 0);
    }

    @Override
    public void setWifiSSIDAndPassword(String ssid, String password) {
        throw new RuntimeException("You should not call this method in simulator.");
    }

    @Override
    public void setStationMode(String ssid, String password) {
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
        return (int) micro.rZ;
    }

    @Override
    public int fetchTemperature() {
        return 50;
    }

    @Override
    public int[] fetchAttitude() {
        return new int[]{(int) micro.rX, (int) micro.rY, (int) micro.rZ};
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

    public void addMicroListener(Consumer<TelloMicroState> listener) {
        this.microListeners.add(listener);
        updateState();
    }

    public boolean removeMicroListener(Consumer<TelloMicroState> listener) {
        return this.microListeners.remove(listener);
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

    public void issueFrame(TelloVideoFrame frame) {
        if (streaming)
            this.videoListeners.forEach((vl) -> vl.onFrameReceived(frame));
    }
}
