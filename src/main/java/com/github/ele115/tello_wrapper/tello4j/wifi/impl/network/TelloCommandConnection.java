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
import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class TelloCommandConnection {
    DatagramSocket ds;
    InetAddress remoteAddress;
    boolean connectionState = false;
    TelloStateThread stateThread;
    TelloVideoThread videoThread;
    ReceiveThread receiveThread;
    BlockingQueue<String> qString;

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
            receiveThread = new ReceiveThread();
            this.remoteAddress = InetAddress.getByName(remote);
            ds = new DatagramSocket(TelloSDKValues.COMMAND_PORT);
            ds.setSoTimeout(TelloSDKValues.COMMAND_SOCKET_TIMEOUT);
            ds.connect(remoteAddress, TelloSDKValues.COMMAND_PORT);
            stateThread.connect();
            videoThread.connect();
            stateThread.start();
            videoThread.start();
            receiveThread.start();
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
        return receiveResponse(cmd);
    }

    private String getResponse(boolean block) {
        if (!block)
            return qString.poll();

        while (true) {
            try {
                return qString.take();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public TelloResponse receiveResponse(TelloCommand cmd) throws TelloNetworkException, TelloCommandTimedOutException, TelloGeneralCommandException, TelloNoValidIMUException, TelloCustomCommandException {
        //Read response, or assume ok with the remote control command
        String data = cmd instanceof RemoteControlCommand ? "ok" : getResponse(true);
        TelloResponse response = cmd.buildResponse(data);
        cmd.setResponse(response);
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

    private class ReceiveThread extends Thread {
        private final AtomicBoolean stop;

        private ReceiveThread() {
            stop = new AtomicBoolean(false);
        }

        public void run() {
            while (!stop.get()) {
                try {
                    var data = readString().trim();
                    if (data.startsWith("conn_ack"))
                        continue;
                    if (!TelloSDKValues.COMMAND_REPLY_PATTERN.matcher(data).matches())
                        continue;
                    qString.offer(data);
                    continue;
                } catch (TelloNetworkException e) {
                    e.printStackTrace();
                } catch (TelloCommandTimedOutException ignored) {
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private class ExecuteThread extends Thread {
        private final TelloCommand f;
        private final Predicate<TelloDroneState> p;

        private ExecuteThread(TelloCommand f, Predicate<TelloDroneState> p) {
            this.f = f;
            this.p = p;
        }

        private boolean checkForFinish() {
            String data = f instanceof RemoteControlCommand ? "ok" : getResponse(false);
            if (data == null)
                return false;
            try {
                TelloResponse response = f.buildResponse(data);
                f.setResponse(response);
                return true;
            } catch (TelloNetworkException e) {
                throw new RuntimeException("Network error", e);
            } catch (TelloCustomCommandException e) {
                throw new RuntimeException("Custom error", e);
            } catch (TelloGeneralCommandException e) {
                throw new RuntimeException("General error", e);
            } catch (TelloNoValidIMUException e) {
                throw new RuntimeException("IMU error", e);
            }
        }

        public void run() {
            try {
                w:
                for (var j = 0; ; j++) {
                    if (j > 0)
                        System.err.printf("Warning: re-issue command %s, the %d-th times\n", f.getClass().toString(), j);
                    try {
                        qString.clear();
                        send(f.serializeCommand());
                    } catch (TelloNetworkException e) {
                        throw new RuntimeException("Network error", e);
                    }
                    for (var i = 0; i < 30; i++) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                        if (checkForFinish())
                            return;
                        if (p.test(drone.getCachedState())) {
                            System.err.println("valid"); // FIXME
                            break w;
                        }
                    }
                }

                // Wait 3 seconds, then check stability
                for (var i = 0; ; i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    if (checkForFinish())
                        return;
                    if (i < 30)
                        continue;
                    if (!drone.getCachedState().isStable())
                        continue;

                    System.err.println("Warning: no reply found, assume finished");
                    return;
                }
            } finally {
                if (TelloSDKValues.DEBUG)
                    System.err.println("passed");
            }
        }
    }

    public TelloResponse robustSendCommand(TelloCommand cmd, Predicate<TelloDroneState> p) {
        var th = new ExecuteThread(cmd, p);
        th.start();
        try {
            th.join();
        } catch (InterruptedException ignored) {
        }
    }
}
