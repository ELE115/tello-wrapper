# tello-wrapper

A small Java library that connects your JVM to your tello drone. Provides access to tello sdk commands, state messages and video feed.
Based on Tello4J.

## Getting started

Include the library in your project by adding it as a gradle source dependency:

`build.gradle`:
```gradle
dependencies {
    implementation 'com.github.ele115:tello-wrapper:0.+'
    implementation 'me.friwi:tello4j:1.+'
    implementation 'com.github.b1f6c1c4:mac-to-ip:0.+'
}
```

`settings.gradle`:
```gradle
sourceControl {
    gitRepository('https://github.com/ELE115/tello-wrapper.git') {
        producesModule('com.github.ele115:tello-wrapper')
    }
}
```

Use the API to send instructions to your drone, receive state updates and video frames from the camera of your drone:
```java
public class FlightPlanExample {
    public static void main(String[] args) {
        // Connect to an actual drone, or a simulated drone:
        var drone = Facade.Connect("simulator");
        // Subscribe to state updates of our drone (e.g. current speed, attitude)
        drone.addStateListener((o, n) -> {
            // Do sth. when switching from one to another state
        });
        //Create a video window to see things with our drones eyes
        drone.addVideoListener(new VideoWindow());
        // ...or use a custom video listener to process the single frames
        drone.addVideoListener(frame -> {
            //Do sth when we received a frame
        });
        // ...and tell the drone to turn on the stream
        drone.setStreaming(true);
        // Now perform a flight plan
        drone.takeoff();
        drone.forward(30);
        drone.turnLeft(90);
        drone.forward(30);
        drone.backward(30);
        drone.flip(FlipDirection.FORWARD);
        drone.turnRight(90);
        drone.backward(30);
        drone.land();
    }
}
```

## Documentation

Refer to the javadoc of [Tello4J](https://friwi.me/tello4j/javadoc/).
