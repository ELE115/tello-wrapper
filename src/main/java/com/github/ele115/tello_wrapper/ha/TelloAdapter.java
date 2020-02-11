package com.github.ele115.tello_wrapper.ha;

import com.github.ele115.tello_wrapper.ITelloDrone;
import com.github.ele115.tello_wrapper.tello4j.api.drone.TelloDrone;
import com.github.ele115.tello_wrapper.tello4j.api.drone.WifiDroneFactory;
import com.github.ele115.tello_wrapper.tello4j.api.exception.*;
import com.github.ele115.tello_wrapper.tello4j.api.state.StateListener;
import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;
import com.github.ele115.tello_wrapper.tello4j.api.video.TelloVideoExportType;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoListener;
import com.github.ele115.tello_wrapper.tello4j.api.world.FlipDirection;
import com.github.ele115.tello_wrapper.tello4j.api.world.MovementDirection;
import com.github.ele115.tello_wrapper.tello4j.api.world.TurnDirection;

import java.util.List;

public class TelloAdapter implements ITelloDrone {
    private final TelloDrone drone;
    private final TelloD telloD;

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
        telloD = new TelloD(drone);
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
        telloD = new TelloD(drone);
    }

    @Override
    public boolean isConnected() {
        return drone.isConnected();
    }

    @Override
    public void takeoff() {
        // FIXME
        drone.addStateListener((oldState, newState) -> {
            System.err.printf("height=%d yaw=%d tof=%d\n", newState.getHeight(), newState.getYaw(), newState.getTofDistance());
        });
        telloD.preventLanding();
        telloD.execute(() -> {
            try {
                drone.takeoff();
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            }
        }, (oldState, newState) -> newState.getHeight() > 10);
    }

    @Override
    public void land() {
        telloD.stopPreventingLanding();
        telloD.execute(() -> {
            try {
                drone.land();
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            }
        }, (oldState, newState) -> newState.getHeight() < 5);
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
        telloD.execute(() -> {
            try {
                drone.emergency();
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            }
        }, (oldState, newState) -> newState.getHeight() < 5);
    }

    @Override
    public void moveDirection(MovementDirection direction, int cm) {
        telloD.execute(() -> {
            try {
                drone.moveDirection(direction, cm);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getTofDistance() - oldState.getTofDistance() > 10);
    }

    @Override
    public void turn(TurnDirection direction, int degrees) {
        telloD.execute(() -> {
            try {
                drone.turn(direction, degrees);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> Math.abs(newState.getYaw() - oldState.getYaw()) > 10);
    }

    @Override
    public void flip(FlipDirection direction) {
        try {
            drone.flip(direction);
        } catch (TelloNetworkException e) {
            throw new RuntimeException("Network error", e);
        } catch (TelloCommandTimedOutException ignore) {
            // TODO
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
        telloD.execute(() -> {
            try {
                drone.move(x, y, z, speed);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getTofDistance() - oldState.getTofDistance() > 10);
    }

    @Override
    public void curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed) {
        telloD.execute(() -> {
            try {
                drone.curve(x1, y1, z1, x2, y2, z2, speed);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getTofDistance() - oldState.getTofDistance() > 10);
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
        telloD.execute(() -> {
            try {
                drone.sendRemoteControlInputs(lr, fb, ud, yaw);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            }
        }, (oldState, newState) -> newState.getTofDistance() - oldState.getTofDistance() > 10);
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
        telloD.execute(() -> {
            try {
                drone.up(cm);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getHeight() - oldState.getHeight() > 10);
    }

    @Override
    public void down(int cm) {
        telloD.execute(() -> {
            try {
                drone.down(cm);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getHeight() - oldState.getHeight() < -10);
    }

    @Override
    public void left(int cm) {
        telloD.execute(() -> {
            try {
                drone.left(cm);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getTofDistance() - oldState.getTofDistance() > 10);
    }

    @Override
    public void right(int cm) {
        telloD.execute(() -> {
            try {
                drone.right(cm);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getTofDistance() - oldState.getTofDistance() > 10);
    }

    @Override
    public void forward(int cm) {
        telloD.execute(() -> {
            try {
                drone.forward(cm);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getTofDistance() - oldState.getTofDistance() > 10);
    }

    @Override
    public void backward(int cm) {
        telloD.execute(() -> {
            try {
                drone.backward(cm);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> newState.getTofDistance() - oldState.getTofDistance() > 10);
    }

    @Override
    public void turnLeft(int degrees) {
        telloD.execute(() -> {
            try {
                drone.turnLeft(degrees);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> Math.abs(newState.getYaw() - oldState.getYaw()) > 10);
    }

    @Override
    public void turnRight(int degrees) {
        telloD.execute(() -> {
            try {
                drone.turnRight(degrees);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCommandTimedOutException e) {
                return false;
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }, (oldState, newState) -> Math.abs(newState.getYaw() - oldState.getYaw()) > 10);
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
