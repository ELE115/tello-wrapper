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

package com.github.ele115.tello_wrapper.tello4j.wifi.impl;

import com.github.ele115.tello_wrapper.tello4j.api.drone.TelloDrone;
import com.github.ele115.tello_wrapper.tello4j.api.world.FlipDirection;
import com.github.ele115.tello_wrapper.tello4j.api.world.MovementDirection;
import com.github.ele115.tello_wrapper.tello4j.api.world.TurnDirection;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.control.*;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.read.*;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.set.RemoteControlCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.set.SetSpeedCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.set.SetStationModeCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.set.SetWifiPasswordAndSSIDCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.network.TelloCommandConnection;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.response.TelloReadCommandResponse;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.TelloSDKValues;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.command.ReadCommand;

public class WifiDrone extends TelloDrone {
    private TelloCommandConnection commandConnection;

    private boolean streaming = false;

    public WifiDrone() {
        this.commandConnection = new TelloCommandConnection(this);
    }

    @Override
    public void connect() {
        this.connect(TelloSDKValues.DRONE_IP_DST);
    }

    @Override
    public void connect(String remoteAddr) {
        this.commandConnection.connect(remoteAddr);
        //Enter SDK mode
        this.commandConnection.sendCommand(new EnterSDKModeCommand());
    }

    @Override
    public void disconnect() {
        this.commandConnection.disconnect();
    }

    @Override
    public boolean isConnected() {
        return this.commandConnection.isConnected();
    }

    public void takeoff() {
        this.commandConnection.sendCommand(new TakeoffCommand());
    }

    public void land() {
        this.commandConnection.sendCommand(new LandCommand());
    }

    public boolean isStreaming() {
        return this.streaming;
    }

    public void setStreaming(boolean stream) {
        //Only notify drone on state change
        if (stream && !streaming) {
            this.commandConnection.sendCommand(new StreamOnCommand());
        } else if (!stream && streaming) {
            this.commandConnection.sendCommand(new StreamOffCommand());
        }
        //If state change successful, update streaming parameter
        this.streaming = stream;
    }

    public void emergency() {
        this.commandConnection.sendCommand(new EmergencyCommand());
    }

    public void moveDirection(MovementDirection direction, int cm) {
        this.commandConnection.sendCommand(new FlyDirectionCommand(direction, cm));
    }

    public void turn(TurnDirection direction, int degrees) {
        this.commandConnection.sendCommand(new TurnCommand(direction, degrees));
    }

    public void flip(FlipDirection direction) {
        this.commandConnection.sendCommand(new FlipCommand(direction));
    }

    public void move(int x, int y, int z, int speed) {
        this.commandConnection.sendCommand(new FlyParameterizedCommand(x, y, z, speed));
    }

    public void curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed) {
        this.commandConnection.sendCommand(new FlyCurveCommand(x1, x2, y1, y2, z1, z2, speed));
    }

    public void setSpeed(int speed) {
        this.commandConnection.sendCommand(new SetSpeedCommand(speed));
    }

    public void sendRemoteControlInputs(int lr, int fb, int ud, int yaw) {
        this.commandConnection.sendCommand(new RemoteControlCommand(lr, fb, ud, yaw));
    }

    public void setWifiSSIDAndPassword(String ssid, String password) {
        this.commandConnection.sendCommand(new SetWifiPasswordAndSSIDCommand(ssid, password));
    }

    public void setStationMode(String ssid, String password) {
        this.commandConnection.sendCommand(new SetStationModeCommand(ssid, password));
    }

    private Object[] fetch(ReadCommand cmd) {
        var r = this.commandConnection.sendCommand(cmd);
        if (r instanceof TelloReadCommandResponse) {
            return ((TelloReadCommandResponse) r).getReturnValues();
        } else {
            throw new RuntimeException("Error while parsing input");
        }
    }

    public double fetchSpeed() {
        return (double) fetch(new ReadSpeedCommand())[0];
    }

    public int fetchBattery() {
        return (int) fetch(new ReadBatteryCommand())[0];
    }

    public int fetchMotorTime() {
        return (int) fetch(new ReadMotorTimeCommand())[0];
    }

    public int fetchHeight() {
        return (int) fetch(new ReadHeightCommand())[0];
    }

    public int fetchTemperature() {
        return (int) fetch(new ReadTemperatureCommand())[0];
    }

    public int[] fetchAttitude() {
        Object[] in = fetch(new ReadAttitudeCommand());
        int[] ret = new int[3];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (int) in[i];
        }
        return ret;
    }

    public double fetchBarometer() {
        return (double) fetch(new ReadBarometerCommand())[0];
    }

    public double[] fetchAcceleration() {
        Object[] in = fetch(new ReadAccelerationCommand());
        double[] ret = new double[3];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (double) in[i];
        }
        return ret;
    }

    public int fetchTOFDistance() {
        return (int) fetch(new ReadTOFDistanceCommand())[0];
    }

    public int fetchWifiSnr() {
        return (int) fetch(new ReadWifiSNRCommand())[0];
    }
}
