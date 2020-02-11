package com.github.ele115.tello_wrapper.tello4j.wifi.impl.command;

import com.github.ele115.tello_wrapper.tello4j.api.exception.TelloCustomCommandException;
import com.github.ele115.tello_wrapper.tello4j.api.exception.TelloGeneralCommandException;
import com.github.ele115.tello_wrapper.tello4j.api.exception.TelloNetworkException;
import com.github.ele115.tello_wrapper.tello4j.api.exception.TelloNoValidIMUException;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.command.TelloCommand;
import com.github.ele115.tello_wrapper.tello4j.wifi.model.response.TelloResponse;

public class PingCommand extends TelloCommand {
    @Override
    public String serializeCommand() {
        return "ping";
    }

    @Override
    public TelloResponse buildResponse(String data) throws TelloGeneralCommandException, TelloNoValidIMUException, TelloCustomCommandException, TelloNetworkException {
        return null;
    }
}
