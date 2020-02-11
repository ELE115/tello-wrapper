package com.github.ele115.tello_wrapper.ha;

import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;

interface Criterion {
    boolean test(TelloDroneState oldState, TelloDroneState newState);
}
