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

package com.github.ele115.tello_wrapper.tello4j.wifi.impl.network;

import com.github.ele115.tello_wrapper.tello4j.api.exception.*;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.WifiDrone;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.set.RemoteControlCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.state.TelloStateThread;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.video.TelloVideoThread;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.TelloSDKValues;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.command.TelloCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.response.TelloResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class TelloCommandConnection {
    DatagramSocket ds;
    InetAddress remoteAddress;
    boolean connectionState = false;
    TelloStateThread stateThread;
    TelloVideoThread videoThread;

    WifiDrone drone;

    private long lastCommand = -1;
    private boolean onceConnected = false;

    public TelloCommandConnection(WifiDrone drone) {
        this.drone = drone;
    }

    public void connect(String remote) throws TelloNetworkException {
        if (onceConnected)
            throw new TelloNetworkException("You can not reconnect by using connect(). Please build a new tello drone object.");
        try {
            onceConnected = true;
            lastCommand = System.currentTimeMillis();
            stateThread = new TelloStateThread(this);
            videoThread = new TelloVideoThread(this);
            this.remoteAddress = InetAddress.getByName(remote);
            ds = new DatagramSocket(TelloSDKValues.COMMAND_PORT);
            ds.setSoTimeout(TelloSDKValues.COMMAND_SOCKET_TIMEOUT);
            ds.connect(remoteAddress, TelloSDKValues.COMMAND_PORT);
            stateThread.connect();
            videoThread.connect();
            stateThread.start();
            videoThread.start();
            connectionState = true;
        } catch (Exception e) {
            throw new TelloNetworkException("Could not connect to drone", e);
        }
    }

    public void disconnect() {
        connectionState = false;
        stateThread.kill();
        videoThread.kill();
        ds.disconnect();
        ds.close();
    }

    public TelloResponse sendCommand(TelloCommand cmd) throws TelloNetworkException, TelloCommandTimedOutException, TelloGeneralCommandException, TelloNoValidIMUException, TelloCustomCommandException {
        send(cmd.serializeCommand());
        //Read response, or assume ok with the remote control command
        String data = cmd instanceof RemoteControlCommand ? "ok" : readString().trim();
        int attempt = 0;
        boolean invalid;
        do {
            invalid = data.startsWith("conn_ack");
            if (!TelloSDKValues.COMMAND_REPLY_PATTERN.matcher(data).matches()) invalid = true;
            if (invalid && TelloSDKValues.DEBUG) {
                System.err.println("Dropping reply \"" + data + "\" as it might be binary");
            }
            attempt++;
            if (invalid && attempt >= TelloSDKValues.COMMAND_SOCKET_BINARY_ATTEMPTS) {
                throw new TelloNetworkException("Too many binary messages received after sending command. Broken connection?");
            }
            if (invalid) {
                data = readString().trim();
            }
        } while (invalid);
        TelloResponse response = cmd.buildResponse(data);
        cmd.setResponse(response);
        if (response == null) {
            throw new TelloNetworkException("\"" + cmd.serializeCommand() + "\" command was not answered!");
        }
        return cmd.getResponse();
    }

    void send(String str) throws TelloNetworkException {
        if (!connectionState)
            throw new TelloNetworkException("Can not send/receive data when the connection is closed!");
        if (TelloSDKValues.DEBUG) System.out.println("[OUT] " + str);

        try {
            this.send(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new TelloNetworkException("Your system does not support utf-8 encoding", e);
        }
    }

    private void send(byte[] bytes) throws TelloNetworkException {
        if (!connectionState)
            throw new TelloNetworkException("Can not send/receive data when the connection is closed!");
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteAddress, TelloSDKValues.COMMAND_PORT);
        try {
            ds.send(packet);
        } catch (IOException e) {
            throw new TelloNetworkException("Error on sending packet", e);
        }
    }

    private byte[] readBytes() throws TelloNetworkException, TelloCommandTimedOutException {
        if (!connectionState)
            throw new TelloNetworkException("Can not send/receive data when the connection is closed!");
        byte[] data = new byte[256];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            ds.receive(packet);
        } catch (SocketTimeoutException e) {
            throw new TelloCommandTimedOutException();
        } catch (IOException e) {
            throw new TelloNetworkException("Error while reading from command channel", e);
        }
        return Arrays.copyOf(data, packet.getLength());
    }

    String readString() throws TelloNetworkException, TelloCommandTimedOutException {
        if (!connectionState)
            throw new TelloNetworkException("Can not send/receive data when the connection is closed!");
        byte[] data = readBytes();
        try {
            String str = new String(data, "UTF-8");
            if (TelloSDKValues.DEBUG) System.out.println("[IN ] " + str.trim());
            return str;
        } catch (UnsupportedEncodingException e) {
            throw new TelloNetworkException("Your system does not support utf-8 encoding", e);
        }
    }

    public boolean isConnected() {
        return connectionState && (lastCommand + TelloSDKValues.COMMAND_TIMEOUT) > System.currentTimeMillis();
    }

    public WifiDrone getDrone() {
        return drone;
    }
}
