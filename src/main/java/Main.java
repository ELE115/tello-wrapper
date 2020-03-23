import com.github.ele115.tello_wrapper.Tello;
import com.github.ele115.tello_wrapper.obstacle.ObstacleBall;
import com.github.ele115.tello_wrapper.obstacle.ObstacleBox;
import com.github.ele115.tello_wrapper.obstacle.ObstacleCylinder;
import com.github.ele115.tello_wrapper.obstacle.ObstacleGate;
import com.github.ele115.tello_wrapper.tello4j.api.video.VideoWindow;
import javafx.scene.paint.Color;

public class Main {
    public static void main(String[] args) {
        Tello.setWindowSize(960, 720);
        Tello.getSimulator().addObstacle(new ObstacleCylinder(150, 200, Color.AQUA));
        Tello.getSimulator().addObstacle(new ObstacleCylinder(0, 150, Color.ORANGE));
        Tello.getSimulator().addObstacle(new ObstacleBox(0, 0, 30, Color.LIGHTBLUE));
        Tello.getSimulator().addObstacle(new ObstacleBall(0, 30, 0, Color.RED));
        Tello.getSimulator().addObstacle(new ObstacleGate(50, 80, 0, 30));
        var d1 = Tello.Connect("simulator", 75, 0, 90);
        var d2 = Tello.Connect("simulator", 0, 0, 90);
        d1.addVideoListener(new VideoWindow());
        d2.addVideoListener(new VideoWindow());
        d1.setStreaming(true);
        d2.setStreaming(true);
        d1.takeoff();
        d2.takeoff();
        d1.forward(300);
        d2.forward(300);
        d1.land();
        d2.land();
    }
}
