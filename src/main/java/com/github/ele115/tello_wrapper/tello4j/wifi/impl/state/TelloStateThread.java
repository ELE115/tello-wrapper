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

package com.github.ele115.tello_wrapper.tello4j.wifi.impl.state;

import com.github.ele115.tello_wrapper.tello4j.api.exception.TelloException;
import com.github.ele115.tello_wrapper.tello4j.api.exception.TelloNetworkException;
import com.github.ele115.tello_wrapper.tello4j.api.state.StateListener;
import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;
import com.github.ele115.tello_wrapper.tello4j.wifi.impl.network.TelloCommandConnection;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.TelloSDKValues;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TelloStateThread extends Thread {
    private boolean running = true;
    private TelloCommandConnection connection;
    private DatagramSocket ds;

    public TelloStateThread(TelloCommandConnection connection) {
        this.connection = connection;
    }

    public void connect() throws TelloNetworkException {
        try {
            ds = new DatagramSocket(TelloSDKValues.STATE_PORT, InetAddress.getByName(TelloSDKValues.COMMANDER_IP_DST));
            ds.setSoTimeout(TelloSDKValues.STATE_SOCKET_TIMEOUT);
        } catch (Exception e) {
            throw new TelloNetworkException("Error while creating state receive socket", e);
        }
    }

    public void run() {
        setName("State-Thread");
        while (running) {
            try {
                byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                ds.receive(packet);
                handleInput(Arrays.copyOf(buf, packet.getLength()));
            } catch (Exception e) {
                //Ignore missing updates - no way to error them
                //Disconnect at end of program is also intended to end here
            }
        }
    }

    private void handleInput(byte[] bytes) throws TelloException {
        this.handleInput(new String(bytes, StandardCharsets.UTF_8));
    }

    private void handleInput(String s) throws TelloException {
        if (TelloSDKValues.DEBUG) System.err.println("[STE] " + s.trim());
        TelloDroneState old = this.connection.getDrone().getCachedState();
        TelloDroneState state = TelloStateDeserializer.deserialize(s, old);
        this.connection.getDrone().setCachedState(state);
        for (StateListener listener : this.connection.getDrone().getStateListeners()) {
            listener.onStateChanged(old, state);
        }
    }

    public void kill() {
        running = false;
        ds.close();
    }
}
