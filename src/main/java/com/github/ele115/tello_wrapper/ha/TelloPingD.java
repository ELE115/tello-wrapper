package com.github.ele115.tello_wrapper.ha;

import com.github.ele115.tello_wrapper.tello4j.api.drone.TelloDrone;
import com.github.ele115.tello_wrapper.tello4j.api.exception.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class TelloPingD {
    protected final TelloDrone drone;
    private final PingThread pingThread = new PingThread();

    public TelloPingD(TelloDrone drone) {
        this.drone = drone;
    }

    public void preventLanding() {
        pingThread.stop.set(false);
        pingThread.start();
    }

    public void stopPreventingLanding() {
        pingThread.stop.set(true);
    }

    private class PingThread extends Thread {
        public AtomicBoolean stop = new AtomicBoolean(false);

        public void run() {
            while (true) {
                var res = stop.compareAndSet(true, false);
                if (res)
                    return;
                try {
                    drone.ping();
                } catch (TelloCommandTimedOutException | TelloNetworkException | TelloCustomCommandException | TelloGeneralCommandException | TelloNoValidIMUException ignored) {
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
