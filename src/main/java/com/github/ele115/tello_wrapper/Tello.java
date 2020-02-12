package com.github.ele115.tello_wrapper;

import com.github.ele115.tello_wrapper.tello4j.api.drone.WifiDroneFactory;

public final class Tello {
    private Tello() {
        // static class
    }

    /**
     * Connect to a Tello drone.
     *
     * @param droneId If it is "simulator", then a fake drone is connected;
     *                otherwise, it should be the 6-letter ID of the drone.
     * @return Controller of the drone.
     */
    public static ITelloDrone Connect(String droneId) {
        var env = System.getenv("TELLO_SIMULATOR");
        if (env != null && env.equals("TRUE"))
            return new TelloSimulator(true);
        if (droneId == null)
            throw new RuntimeException("Please specify your Drone ID");
        if (droneId.equals("simulator"))
            return new TelloSimulator(false);
        if (droneId.equals("default")) {
            var drone = WifiDroneFactory.build();
            drone.connect();
            return drone;
        }
        if (!droneId.matches("[0-9A-F]{6}"))
            throw new RuntimeException("Drone ID incorrect, please double check");
        var id = droneId.toLowerCase();
        var mac = "60:60:1f:" + id.substring(0, 2) + ":" + id.substring(2, 4) + ":" + id.substring(4);
        var res = com.github.b1f6c1c4.mac_to_ip.Facade.Execute(mac, "192\\.168\\..*");
        if (res.isEmpty())
            throw new RuntimeException("Drone with ID " + mac + " not found, please double check");
        if (res.size() > 1)
            throw new RuntimeException("More than one drones with ID " + mac + " found");

        {
            var ip = res.get(0);
            var drone = WifiDroneFactory.build();
            drone.connect(ip);
            return drone;
        }
    }
}
