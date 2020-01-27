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

package com.github.ele115.tello_wrapper;

import me.friwi.tello4j.api.state.StateListener;
import me.friwi.tello4j.api.state.TelloDroneState;
import me.friwi.tello4j.api.video.TelloVideoExportType;
import me.friwi.tello4j.api.video.TelloVideoFrame;
import me.friwi.tello4j.api.video.VideoListener;
import me.friwi.tello4j.api.world.FlipDirection;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;

import java.util.List;

/**
 * This class represents all tello drones with their possible sdk actions and their current data.
 * You can construct a tello drone by using a tello drone factory (e.g. {@link me.friwi.tello4j.api.drone.WifiDroneFactory}).
 *
 * @author Fritz Windisch
 */
public interface ITelloDrone {
    /**
     * Estabilishes a connection with a tello drone at the default address (192.168.10.1). Can only be used once, please construct a new tello drone object
     * when reconnecting.
     * You still need to connect to the tello wifi manually before invoking this call.
     */
    void connect();

    /**
     * Estabilishes a connection with a tello drone at a custom address. Can only be used once, please construct a new tello drone object
     * when reconnecting.
     * You still need to connect to the tello wifi manually before invoking this call.
     *
     * @param remoteAddr The remote address or hostname your tello uses. If unsure, use {@link #connect()} to connect to the default destination IP.
     */
    void connect(String remoteAddr);

    /**
     * Disconnects from this tello drone. Does not close this drones resources.
     */
    void disconnect();

    /**
     * Retrieves the connection state of this drone.
     * When the drone times out after not sending commands for 15 seconds, the drone automatically lands safely and
     * closes the connection.
     *
     * @return true: if drone was already connected, false: otherwise
     */
    boolean isConnected();

    /**
     * Instructs this drone to take off.
     *
     */
    void takeoff();

    /**
     * Instructs this drone to land.
     *
     */
    void land();

    /**
     * Fetches whether streaming is currently enabled on this drone
     *
     * @return true: if streaming is enabled, false: otherwise
     */
    boolean isStreaming();

    /**
     * Instruct this drone to enable/disable streaming. You can listen to the stream by adding a {@link VideoListener}
     * using {@link #addVideoListener(VideoListener)}.
     *
     * @param stream true: drone should start streaming, false: drone should stop streaming.
     */
    void setStreaming(boolean stream);

    /**
     * Instructs this drone to turn off all motors.
     *
     */
    void emergency();

    /**
     * Instructs this drone to move a certain amount of centimeters in one direction.
     *
     * @param direction The direction the drone should move in
     * @param cm        The amount in centimeters to be moved
     */
    void moveDirection(MovementDirection direction, int cm);

    /**
     * Instructs this drone to turn a certain amount of degrees in one direction
     *
     * @param direction The direction to turn the drone
     * @param degrees   The amount of degrees to turn
     */
    void turn(TurnDirection direction, int degrees);

    /**
     * Instructs this drone to perform a flip in the direction you specify
     *
     * @param direction The direction to perform the flip to
     */
    void flip(FlipDirection direction);

    /**
     * Instructs this drone to move to a relative position (x, y, z) with a set speed
     *
     * @param x     Amount of centimeters to move on the x-Axis (left/right)
     * @param y     Amount of centimeters to move on the y-Axis (forward/backward)
     * @param z     Amount of centimeters to move on the z-Axis (up/down)
     * @param speed Flying speed
     */
    void move(int x, int y, int z, int speed);

    /**
     * Instructs this drone to fly a curve via one relative point to another
     *
     * @param x1    Amount of centimeters to the first point on the x-Axis (left/right)
     * @param y1    Amount of centimeters to the first point on the y-Axis (forward/backward)
     * @param z1    Amount of centimeters to the first point on the z-Axis (up/down)
     * @param x2    Amount of centimeters to the second point on the x-Axis (left/right)
     * @param y2    Amount of centimeters to the second point on the y-Axis (forward/backward)
     * @param z2    Amount of centimeters to the second point on the z-Axis (up/down)
     * @param speed Flying speed
     */
    void curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed);

    /**
     * Sets the flying speed of this drone
     *
     * @param speed Flying speed
     */
    void setSpeed(int speed);

    /**
     * Send remote control input to this drone
     *
     * @param lr  Left/right input
     * @param fb  Forward/Backward input
     * @param ud  Up/Down input
     * @param yaw Rotation input
     */
    void sendRemoteControlInputs(int lr, int fb, int ud, int yaw);

    /**
     * Changes your tello wifi name (ssid) and password. Can be reset by pressing the tello "on" button for 5 seconds
     *
     * @param ssid     New wifi ssid
     * @param password New wifi password
     */
    void setWifiSSIDAndPassword(String ssid, String password);

    /**
     * Fetch the current speed setting of this drone. Please note that this does not return the actual current speed.
     *
     * @return Speed setting
     */
    double fetchSpeed();

    /**
     * Fetch the current battery level of this drone.
     *
     * @return Battery level
     */
    int fetchBattery();

    /**
     * Fetch the current running duration of the motors/airborne time.
     *
     * @return Motor time in seconds
     */
    int fetchMotorTime();

    /**
     * Fetch the current altitude of this drone.
     *
     * @return Altitude in centimeters
     */
    int fetchHeight();

    /**
     * Fetch the current temperature of this drone (own temperature, not outside temperature).
     *
     * @return Temperature in degrees celsius
     */
    int fetchTemperature();

    /**
     * Fetches the current attitude of this drone.
     *
     * @return An array of int[]{x, y, z}, containing the relative coordinates from the starting point
     */
    int[] fetchAttitude();

    /**
     * Fetch the barometer value of this drone.
     *
     * @return Barometer value in hectopascal
     */
    double fetchBarometer();

    /**
     * Fetch the current acceleration force of this drone.
     *
     * @return An array of double[]{vx, vy, vz}. vy always contains the earth acceleration.
     */
    double[] fetchAcceleration();

    /**
     * Fetch the distance ahead using the time-of-flight sensor.
     *
     * @return Distance to the next obstacle in centimeters
     */
    int fetchTOFDistance();

    /**
     * Fetch the wifi signal-noise-ratio from this drones perspective.
     *
     * @return Signal-noise-ratio value
     */
    int fetchWifiSnr();


    /**
     * Instructs this drone to move up a certain amount of centimeters.
     *
     * @param cm The amount in centimeters to be moved
     */
    void up(int cm);

    /**
     * Instructs this drone to move down a certain amount of centimeters.
     *
     * @param cm The amount in centimeters to be moved
     */
    void down(int cm);

    /**
     * Instructs this drone to move to the left a certain amount of centimeters.
     *
     * @param cm The amount in centimeters to be moved
     */
    void left(int cm);

    /**
     * Instructs this drone to move to the right a certain amount of centimeters.
     *
     * @param cm The amount in centimeters to be moved
     */
    void right(int cm);

    /**
     * Instructs this drone to move forward a certain amount of centimeters.
     *
     * @param cm The amount in centimeters to be moved
     */
    void forward(int cm);

    /**
     * Instructs this drone to move backward a certain amount of centimeters.
     *
     * @param cm The amount in centimeters to be moved
     */
    void backward(int cm);

    /**
     * Instructs this drone to turn left a certain amount of degrees
     *
     * @param degrees The amount of degrees to turn
     */
    void turnLeft(int degrees);

    /**
     * Instructs this drone to turn right a certain amount of degrees
     *
     * @param degrees The amount of degrees to turn
     */
    void turnRight(int degrees);

    /**
     * Adds a {@link VideoListener} to this drone, which can then receive new {@link TelloVideoFrame}s from this drone
     *
     * @param listener The listener to be added
     */
    void addVideoListener(VideoListener listener);

    /**
     * Removes a {@link VideoListener} from this drone, which will no longer receive {@link TelloVideoFrame}s
     *
     * @param listener The listener to be removed
     * @return true: if the listener was previously attatched to this drone, false: otherwise
     */
    boolean removeVideoListener(VideoListener listener);

    /**
     * Lists all {@link VideoListener}s currently attached to this drone
     *
     * @return List of all {@link VideoListener}s
     */
    List<VideoListener> getVideoListeners();

    /**
     * Adds a {@link StateListener} to this drone, which can then receive new {@link TelloDroneState}s from this drone
     *
     * @param listener The listener to be added
     */
    void addStateListener(StateListener listener);

    /**
     * Removes a {@link StateListener} from this drone, which will no longer receive {@link TelloDroneState}s
     *
     * @param listener The listener to be removed
     * @return true: if the listener was previously attatched to this drone, false: otherwise
     */
    boolean removeStateListener(StateListener listener);

    /**
     * Lists all {@link StateListener}s currently attached to this drone
     *
     * @return List of all {@link StateListener}s
     */
    List<StateListener> getStateListeners();

    /**
     * Fetches the last received {@link TelloDroneState} of this drone
     *
     * @return Last received {@link TelloDroneState}
     */
    TelloDroneState getCachedState();

    /**
     * Set the last received {@link TelloDroneState} of this drone
     *
     * @param cachedState A new {@link TelloDroneState} to be cached for this drone
     */
    void setCachedState(TelloDroneState cachedState);

    /**
     * Retrieves the current {@link TelloVideoExportType} for this drone
     *
     * @return Current video export type
     */
    TelloVideoExportType getVideoExportType();

    /**
     * Sets the current {@link TelloVideoExportType} for this drone. Please note that it may take a few seconds for the
     * new type to be applied.
     *
     * @param videoExportType The new video export type
     */
    void setVideoExportType(TelloVideoExportType videoExportType);
}
