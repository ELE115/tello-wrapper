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
import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;

public class TelloStateDeserializer {
    /**
     * Some dirty code to deserialize the dirty representation received from the drone
     *
     * @param state
     * @return a new TelloDroneState
     * @throws TelloException when the format is invalid
     */
    public static TelloDroneState deserialize(String state, TelloDroneState oldState) throws TelloNetworkException {
        try {
            int pitch = 0, roll = 0, yaw = 0, speedX = 0, speedY = 0, speedZ = 0, tempLow = 0, tempHigh = 0, tofDistance = 0, height = 0, battery = 0, motorTime = 0;
            double barometer = 0, accelerationX = 0, accelerationY = 0, accelerationZ = 0;
            String[] arguments = state.trim().split(";");
            for (int i = 5; i < arguments.length; i++) {
                String number = arguments[i].split(":")[1];
                if (number.contains(".")) {
                    double value = Double.parseDouble(number);
                    switch (i) {
                        case 16:
                            barometer = value;
                            break;
                        case 18:
                            accelerationX = value;
                            break;
                        case 19:
                            accelerationY = value;
                            break;
                        case 20:
                            accelerationZ = value;
                            break;
                        default:
                            throw new TelloNetworkException("Invalid matching in state deserialization: \"" + state + "\"");
                    }
                } else {
                    int value = Integer.parseInt(number);
                    switch (i) {
                        case 5:
                            pitch = value;
                            break;
                        case 6:
                            roll = value;
                            break;
                        case 7:
                            yaw = value;
                            break;
                        case 8:
                            speedX = value;
                            break;
                        case 9:
                            speedY = value;
                            break;
                        case 10:
                            speedZ = value;
                            break;
                        case 11:
                            tempLow = value;
                            break;
                        case 12:
                            tempHigh = value;
                            break;
                        case 13:
                            tofDistance = value;
                            break;
                        case 14:
                            height = value;
                            break;
                        case 15:
                            battery = value;
                            break;
                        case 17:
                            motorTime = value;
                            break;
                        default:
                            throw new TelloNetworkException("Invalid matching in state deserialization: \"" + state + "\"");
                    }
                }
            }
            return new TelloDroneState(pitch, roll, yaw, speedX, speedY, speedZ, tempLow, tempHigh, tofDistance, height, battery, motorTime, barometer, accelerationX, accelerationY, accelerationZ);
        } catch (Exception e) {
            throw new TelloNetworkException("Error while parsing state input \"" + state + "\"");
        }
    }
}
