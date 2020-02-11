package com.github.ele115.tello_wrapper.ha;

import com.github.ele115.tello_wrapper.tello4j.api.drone.TelloDrone;
import com.github.ele115.tello_wrapper.tello4j.api.exception.*;
import com.github.ele115.tello_wrapper.tello4j.api.state.StateListener;
import com.github.ele115.tello_wrapper.tello4j.api.state.TelloDroneState;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

class TelloD {
    private final TelloDrone drone;

    TelloD(TelloDrone drone) {
        this.drone = drone;
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
                    Thread.sleep(7000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private final PingThread pingThread = new PingThread();

    public void preventLanding() {
        pingThread.stop.set(false);
        pingThread.start();
    }

    public void stopPreventingLanding() {
        pingThread.stop.set(true);
    }

    private class ExecuteThread extends Thread {
        private final Callable<Boolean> f;
        private final AtomicBoolean stop;
        private final StateListener listener;

        private ExecuteThread(Callable<Boolean> f, Predicate<TelloDroneState> p) {
            this.f = f;
            this.stop = new AtomicBoolean();
            this.listener = (oldState, newState) -> {
                if (p.test(newState)) {
                    System.err.println("valid"); // FIXME
                    removeListener();
                    stop.set(true);
                }
            };
        }

        private void removeListener() {
            drone.removeStateListener(listener);
        }

        public void run() {
            drone.addStateListener(listener);
            Thread th;
            w:
            while (true) {
                th = new Thread(() -> {
                    try {
                        System.err.println("issue"); // FIXME
                        if (f.call()) {
                            System.err.println("short cut"); // FIXME
                            stop.set(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                th.run();
                for (var i = 0; i < 30; i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    if (stop.compareAndSet(true, false))
                        break w;
                }
            }
            try {
                th.join();
            } catch (InterruptedException ignored) {
            }
            removeListener();
            System.err.println("passed"); // FIXME
        }
    }

    public void execute(Callable<Boolean> f, Criterion ct) {
        synchronized (this) {
            var o = drone.getCachedState();
            var th = new ExecuteThread(f, (s) -> ct.test(o, s));
            th.start();
            try {
                th.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
