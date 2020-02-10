package com.github.ele115.tello_wrapper.ha;

import me.friwi.tello4j.api.drone.TelloDrone;
import me.friwi.tello4j.api.exception.TelloCommandTimedOutException;
import me.friwi.tello4j.api.exception.TelloCustomCommandException;
import me.friwi.tello4j.api.exception.TelloGeneralCommandException;
import me.friwi.tello4j.api.exception.TelloNetworkException;
import me.friwi.tello4j.api.state.StateListener;
import me.friwi.tello4j.api.state.TelloDroneState;

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
                    drone.fetchBattery();
                } catch (TelloCommandTimedOutException | TelloNetworkException | TelloCustomCommandException | TelloGeneralCommandException ignored) {
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
        public final Predicate<TelloDroneState> p;

        private ExecuteThread(Callable<Boolean> f, Predicate<TelloDroneState> p) {
            this.f = f;
            this.p = p;
        }

        public void run() {
            var stop = new AtomicBoolean(false);
            var l = new StateListener() {
                @Override
                public void onStateChanged(TelloDroneState oldState, TelloDroneState newState) {
                    stop.set(!p.test(newState));
                }
            };
            drone.addStateListener(l);
            Thread th;
            w:
            while (true) {
                th = new Thread(() -> {
                    try {
                        if (f.call())
                            stop.set(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                th.run();
                for (var i = 0; i < 10; i++) {
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
            drone.removeStateListener(l);
        }
    }

    public void execute(Callable<Boolean> f, Criterion ct) {
        synchronized (this) {
            var o = drone.getCachedState();
            var th = new ExecuteThread(f, (s) -> ct.test(o, s));
            th.start();
        }
    }
}