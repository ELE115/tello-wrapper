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

package com.github.ele115.tello_wrapper.tello4j.wifi.model.response;

import com.github.ele115.tello_wrapper.tello4j.wifi.impl.response.CommandResultType;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.command.TelloCommand;

public class TelloResponse {
    protected CommandResultType commandResultType;
    private TelloCommand command;
    private String message;

    public TelloResponse(TelloCommand command, CommandResultType commandResultType, String message) {
        this.command = command;
        this.commandResultType = commandResultType;
        this.message = message;
    }

    public TelloCommand getCommand() {
        return command;
    }

    public CommandResultType getCommandResultType() {
        return commandResultType;
    }

    public String getMessage() {
        return message;
    }
}
