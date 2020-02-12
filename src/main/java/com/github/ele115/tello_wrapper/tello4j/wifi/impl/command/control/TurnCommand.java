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

package com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.control;

import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;
import com.github.ele115.tello_wrapper.tello4j.api.world.TurnDirection;
import com.github.ele115.tello_wrapper.tello4j.util.TelloArgumentVerifier;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.command.ControlCommand;

public class TurnCommand extends ControlCommand {
    private TurnDirection direction;
    private int amount;

    public TurnCommand(TurnDirection direction, int amount) {
        super(direction.getCommand() + " " + amount);
        TelloArgumentVerifier.checkRange(amount, 1, 3600, "The amount of %x degrees exceeded the allowed range of [%min,%max]");
        this.direction = direction;
        this.amount = amount;
    }

    @Override
    public boolean test(TelloDroneState oldState, TelloDroneState newState) {
        return Math.abs(newState.getYaw() - oldState.getYaw()) > Math.abs(amount) / 2;
    }
}
