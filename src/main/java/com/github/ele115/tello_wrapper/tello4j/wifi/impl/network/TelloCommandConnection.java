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

import com.github.ele115.tello_wrapper.tello4j.api.drone.TelloDrone;
import com.github.ele115.tello_wrapper.tello4j.api.exception.*;
import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;
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
import java.nio.charset.StandardCharsets;
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
    PingThread pingThread;
    ReceiveThread receiveThread;
    BlockingQueue<String> qString;

    TelloDrone drone;

    private long lastCommand = -1;
    private boolean onceConnected = false;

    public TelloCommandConnection(TelloDrone drone) {
        this.drone = drone;
    }

    public void connect(String remote) {
        if (onceConnected)
            throw new RuntimeException("You can not reconnect by using connect(). Please build a new tello drone object.");
        try {
            onceConnected = true;
            lastCommand = System.currentTimeMillis();
            stateThread = new TelloStateThread(this);
            videoThread = new TelloVideoThread(this);
            pingThread = new PingThread();
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
            throw new RuntimeException("Could not connect to drone", e);
        }
    }

    public void disconnect() {
        connectionState = false;
        stateThread.kill();
        videoThread.kill();
        pingThread.kill();
        receiveThread.kill();
        ds.disconnect();
        ds.close();
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

    private void send(String str) throws TelloNetworkException {
        if (!connectionState)
            throw new RuntimeException("Can not send/receive data when the connection is closed!");
        if (TelloSDKValues.DEBUG) System.out.println("[OUT] " + str);

        this.send(str.getBytes(StandardCharsets.UTF_8));
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
        String str = new String(data, StandardCharsets.UTF_8);
        if (TelloSDKValues.DEBUG) System.out.println("[IN ] " + str.trim());
        return str;
    }

    public boolean isConnected() {
        return connectionState && (lastCommand + TelloSDKValues.COMMAND_TIMEOUT) > System.currentTimeMillis();
    }

    public TelloDrone getDrone() {
        return drone;
    }

    private class PingThread extends Thread {
        public AtomicBoolean stop = new AtomicBoolean(false);

        public void run() {
            while (stop.get()) {
                try {
                    send("-- ping --");
                } catch (TelloNetworkException ignored) {
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        public void kill() {
            stop.set(true);
        }
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

        public void kill() {
            stop.set(true);
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

    public TelloResponse sendCommand(TelloCommand cmd) {
        var o = drone.getCachedState();
        var th = new ExecuteThread(cmd, (s) -> cmd.test(o, s));
        th.start();
        try {
            th.join();
        } catch (InterruptedException ignored) {
        }
        return cmd.getResponse();
    }
}
