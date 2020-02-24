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
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.set.RemoteControlCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.state.TelloStateThread;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.video.TelloVideoThread;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.TelloSDKValues;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.command.TelloCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.response.TelloResponse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TelloCommandConnection {
    DatagramSocket ds;
    InetAddress remoteAddress;
    boolean connectionState = false;
    TelloStateThread stateThread;
    TelloVideoThread videoThread;
    PingThread pingThread;
    ReceiveThread receiveThread;
    BlockingQueue<String> qString = new LinkedBlockingQueue<>();
    AtomicInteger seq = new AtomicInteger(1);
    AtomicInteger seqr = new AtomicInteger(0);

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
            pingThread.start();
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

    private String getResponse() {
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
        if (TelloSDKValues.DEBUG) System.err.println("[OUT] " + str);

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
        if (TelloSDKValues.DEBUG) System.err.println("[IN ] " + str.trim());
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
            while (!stop.get()) {
                try {
                    Thread.sleep(2000);
                    send("-- ping --");
                } catch (Exception ignored) {
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
                    if (TelloSDKValues.INFO)
                        System.err.println("Received: " + data);
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

    private abstract class ExecuteThreadBase extends Thread {
        protected final TelloCommand f;

        private ExecuteThreadBase(TelloCommand f) {
            this.f = f;
        }

        protected String processData(String data) {
            return data;
        }

        protected boolean checkForFinish() {
            String data = f instanceof RemoteControlCommand ? "ok" : getResponse();
            if (data == null)
                return false;
            var realData = this.processData(data);
            if (realData == null)
                return false;
            if (TelloSDKValues.INFO)
                System.err.println("Building response: " + data);
            try {
                TelloResponse response = f.buildResponse(realData);
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
    }

    private class SimpleExecuteThread extends ExecuteThreadBase {
        private SimpleExecuteThread(TelloCommand f) {
            super(f);
        }

        @Override
        protected String processData(String data) {
            if (data.startsWith("Re"))
                return null;
            return data;
        }

        public void run() {
            var j = 1;
            while (true) {
                try {
                    if (TelloSDKValues.INFO)
                        System.err.println("Info: flushing queue");
                    qString.clear();
                    if (j++ > 1) {
                        if (TelloSDKValues.INFO)
                            System.err.println("Warning: re-issue command " + f.serializeCommand());
                    }
                    send(f.serializeCommand());
                } catch (TelloNetworkException e) {
                    throw new RuntimeException("Network error", e);
                }
                for (var i = 0; i < 2; i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    if (checkForFinish())
                        return;
                }
            }
        }
    }

    private class ReExecuteThread extends ExecuteThreadBase {
        private final int s;
        private final String prefix;

        private ReExecuteThread(TelloCommand f) {
            super(f);
            this.s = seq.getAndIncrement();
            prefix = "Re" + toReString();
        }

        private String toReString() {
            if (seqr.get() == 0)
                return "0";
            return "0" + (seqr.getAndIncrement() % 9 + 1);
        }

        private String toReFormat(int v) {
            return "\u0001" + (char) (v % 128 + 1);
        }

        @Override
        protected String processData(String data) {
            if (data.startsWith(prefix)) {
                var r = Integer.parseInt(data.substring(3, 4));
                if (seqr.compareAndSet(0, r)) {
                    if (TelloSDKValues.INFO)
                        System.err.println("Info: sequence number set to " + r);
                }
                return data.substring(7);
            }
            return null;
        }

        public void run() {
            var j = 1;
            while (true) {
                try {
                    if (TelloSDKValues.INFO)
                        System.err.println("Info: flushing queue");
                    qString.clear();
                    send("Re" + toReFormat(s) + toReFormat(j++) + " " + f.serializeCommand());
                    send("Re" + toReFormat(s) + toReFormat(j++) + " " + f.serializeCommand());
                    send("Re" + toReFormat(s) + toReFormat(j++) + " " + f.serializeCommand());
                    send("Re" + toReFormat(s) + toReFormat(j++) + " " + f.serializeCommand());
                } catch (TelloNetworkException e) {
                    throw new RuntimeException("Network error", e);
                }
                for (var i = 0; i < 3; i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    if (TelloSDKValues.INFO)
                        System.err.println("Info: Checking...");
                    if (checkForFinish())
                        return;
                }
            }
        }
    }

    public TelloResponse sendCommand(TelloCommand cmd) {
        ExecuteThreadBase th;
        if (cmd.isIdempotent())
            th = new SimpleExecuteThread(cmd);
        else
            th = new ReExecuteThread(cmd);
        th.start();
        try {
            th.join();
        } catch (InterruptedException ignored) {
        }
        if (TelloSDKValues.INFO)
            System.err.println("Info: finished " + cmd.serializeCommand());
        return cmd.getResponse();
    }
}
