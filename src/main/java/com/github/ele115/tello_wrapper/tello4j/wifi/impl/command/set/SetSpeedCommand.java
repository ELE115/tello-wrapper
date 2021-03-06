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

package com.github.ele115.tello_wrapper.tello4j.wifi.impl.command.set;

import com.github.ele115.tello_wrapper.tello4j.util.TelloArgumentVerifier;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.command.SetCommand;

public class SetSpeedCommand extends SetCommand {
    private int speed;

    public SetSpeedCommand(int speed) {
        super("speed " + speed);
        TelloArgumentVerifier.checkRange(speed, 10, 100, "Speed of %x exceeds [%min,%max]");
        this.speed = speed;
    }
}
