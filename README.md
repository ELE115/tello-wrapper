# tello-wrapper

A small Java library that connects your JVM to your tello drone. Provides access to tello sdk commands, state messages and video feed.
Based on Tello4J.

## Getting started

Include the library in your project by adding it as a gradle source dependency:

`build.gradle`:
```gradle
repositories {
    jcenter()
}

dependencies {
    implementation 'com.github.ele115:tello-wrapper:2.+'
}
```

Use the API to send instructions to your drone, receive state updates and video frames from the camera of your drone:
```java
import com.github.ele115.tello_wrapper.ITelloDrone;
import com.github.ele115.tello_wrapper.Tello;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoWindow;
import com.github.ele115.tello_wrapper.tello4j.api.world.FlipDirection;

public class FlightPlanExample {
    public static void main(String[] args) {
        // Connect to an actual drone, or a simulated drone:
        ITelloDrone drone = Tello.Connect("59D70D");
        //                = Tello.Connect("simulator");
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

## Legal

Apache-2.0

Changes from Tello4J:

* Added wrapper to get away `throws`
* Added simulator
* Integrated `mac-to-ip`
* Robust transmission
* Ping to prevent 15-second auto-landing

## Development

The package is published to [JFrog Bintray/ELE115/maven/tello-wrapper](https://bintray.com/ele115/maven/tello-wrapper).
To make a new release:

1. Create a new account on [JFrog Bintray](https://bintray.com/signup/oss).
1. Obtain your [API key](https://bintray.com/profile/edit).
1. In file `~/.gradle/gradle.properties`, add two lines:

    ```
    bintray.user=<your-bintray-user-name>
    bintray.key=<your-bintray-API-key>
    ```

1. Ask the admin of the [JFrog Bintray/ELE115](https://bintray.com/ele115) to include you as a member.
1. Double check if the version in `build.gradle` is correct.
1. Run `./gradlew bintrayUpload`.

