package com.github.ele115.tello_wrapper;

import me.friwi.tello4j.api.drone.TelloDrone;
import me.friwi.tello4j.api.drone.WifiDroneFactory;
import me.friwi.tello4j.api.exception.*;
import me.friwi.tello4j.api.state.StateListener;
import me.friwi.tello4j.api.state.TelloDroneState;
import me.friwi.tello4j.api.video.TelloVideoExportType;
import me.friwi.tello4j.api.video.VideoListener;
import me.friwi.tello4j.api.world.FlipDirection;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;

import java.util.List;

public final class TelloAdapter implements ITelloDrone {
    private final TelloDrone drone;

    public TelloAdapter() {
        drone = new WifiDroneFactory().build();
        try {
            drone.connect();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    public TelloAdapter(String ip) {
        drone = new WifiDroneFactory().build();
        try {
            drone.connect(ip);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public boolean isConnected() {
        return drone.isConnected();
    }

    @Override
    public void takeoff() {
        try {
            drone.takeoff();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public void land() {
        try {
            drone.land();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public boolean isStreaming() {
        return drone.isStreaming();
    }

    @Override
    public void setStreaming(boolean stream) {
        try {
            drone.setStreaming(stream);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public void emergency() {
        try {
            drone.emergency();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public void moveDirection(MovementDirection direction, int cm) {
        try {
            drone.moveDirection(direction, cm);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void turn(TurnDirection direction, int degrees) {
        try {
            drone.turn(direction, degrees);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void flip(FlipDirection direction) {
        try {
            drone.flip(direction);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void move(int x, int y, int z, int speed) {
        try {
            drone.move(x, y, z, speed);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed) {
        try {
            drone.curve(x1, y1, z1, x2, y2, z2, speed);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void setSpeed(int speed) {
        try {
            drone.setSpeed(speed);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public void sendRemoteControlInputs(int lr, int fb, int ud, int yaw) {
        try {
            drone.sendRemoteControlInputs(lr, fb, ud, yaw);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public void setWifiSSIDAndPassword(String ssid, String password) {
        try {
            drone.setWifiSSIDAndPassword(ssid, password);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public double fetchSpeed() {
        try {
            return drone.fetchSpeed();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public int fetchBattery() {
        try {
            return drone.fetchBattery();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public int fetchMotorTime() {
        try {
            return drone.fetchMotorTime();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public int fetchHeight() {
        try {
            return drone.fetchHeight();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public int fetchTemperature() {
        try {
            return drone.fetchTemperature();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public int[] fetchAttitude() {
        try {
            return drone.fetchAttitude();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public double fetchBarometer() {
        try {
            return drone.fetchBarometer();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public double[] fetchAcceleration() {
        try {
            return drone.fetchAcceleration();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public int fetchTOFDistance() {
        try {
            return drone.fetchTOFDistance();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public int fetchWifiSnr() {
        try {
            return drone.fetchWifiSnr();
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        }
    }

    @Override
    public void up(int cm) {
        try {
            drone.up(cm);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void down(int cm) {
        try {
            drone.down(cm);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void left(int cm) {
        try {
            drone.left(cm);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void right(int cm) {
        try {
            drone.right(cm);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void forward(int cm) {
        try {
            drone.forward(cm);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void backward(int cm) {
        try {
            drone.backward(cm);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void turnLeft(int degrees) {
        try {
            drone.turnLeft(degrees);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void turnRight(int degrees) {

        try {
            drone.turnRight(degrees);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException e) {
            throw new RuntimeException("Timeout", e);
        } catch (TelloCustomCommandException e) {
            throw new RuntimeException("Custom error", e);
        } catch (TelloGeneralCommandException e) {
            throw new RuntimeException("General error", e);
        } catch (TelloNoValidIMUException e) {
            throw new RuntimeException("IMU error", e);
        }
    }

    @Override
    public void addVideoListener(VideoListener listener) {
        drone.addVideoListener(listener);
    }

    @Override
    public boolean removeVideoListener(VideoListener listener) {
        return drone.removeVideoListener(listener);
    }

    @Override
    public List<VideoListener> getVideoListeners() {
        return drone.getVideoListeners();
    }

    @Override
    public void addStateListener(StateListener listener) {
        drone.addStateListener(listener);
    }

    @Override
    public boolean removeStateListener(StateListener listener) {
        return drone.removeStateListener(listener);
    }

    @Override
    public List<StateListener> getStateListeners() {
        return drone.getStateListeners();
    }

    @Override
    public TelloDroneState getCachedState() {
        return drone.getCachedState();
    }

    @Override
    public void setCachedState(TelloDroneState cachedState) {
        drone.setCachedState(cachedState);
    }

    @Override
    public TelloVideoExportType getVideoExportType() {
        return drone.getVideoExportType();
    }

    @Override
    public void setVideoExportType(TelloVideoExportType videoExportType) {
        drone.setVideoExportType(videoExportType);
    }
}
