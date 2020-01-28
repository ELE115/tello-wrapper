package com.github.ele115.tello_wrapper;

import me.friwi.tello4j.api.state.StateListener;
import me.friwi.tello4j.api.state.TelloDroneState;
import me.friwi.tello4j.api.video.TelloVideoExportType;
import me.friwi.tello4j.api.video.VideoListener;
import me.friwi.tello4j.api.world.FlipDirection;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;

import java.util.List;

public class TelloSimulator implements ITelloDrone {
    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void takeoff() {

    }

    @Override
    public void land() {

    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void setStreaming(boolean stream) {

    }

    @Override
    public void emergency() {

    }

    @Override
    public void moveDirection(MovementDirection direction, int cm) {

    }

    @Override
    public void turn(TurnDirection direction, int degrees) {

    }

    @Override
    public void flip(FlipDirection direction) {

    }

    @Override
    public void move(int x, int y, int z, int speed) {

    }

    @Override
    public void curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed) {

    }

    @Override
    public void setSpeed(int speed) {

    }

    @Override
    public void sendRemoteControlInputs(int lr, int fb, int ud, int yaw) {

    }

    @Override
    public void setWifiSSIDAndPassword(String ssid, String password) {

    }

    @Override
    public double fetchSpeed() {
        return 0;
    }

    @Override
    public int fetchBattery() {
        return 0;
    }

    @Override
    public int fetchMotorTime() {
        return 0;
    }

    @Override
    public int fetchHeight() {
        return 0;
    }

    @Override
    public int fetchTemperature() {
        return 0;
    }

    @Override
    public int[] fetchAttitude() {
        return new int[0];
    }

    @Override
    public double fetchBarometer() {
        return 0;
    }

    @Override
    public double[] fetchAcceleration() {
        return new double[0];
    }

    @Override
    public int fetchTOFDistance() {
        return 0;
    }

    @Override
    public int fetchWifiSnr() {
        return 0;
    }

    @Override
    public void up(int cm) {

    }

    @Override
    public void down(int cm) {

    }

    @Override
    public void left(int cm) {

    }

    @Override
    public void right(int cm) {

    }

    @Override
    public void forward(int cm) {

    }

    @Override
    public void backward(int cm) {

    }

    @Override
    public void turnLeft(int degrees) {

    }

    @Override
    public void turnRight(int degrees) {

    }

    @Override
    public void addVideoListener(VideoListener listener) {

    }

    @Override
    public boolean removeVideoListener(VideoListener listener) {
        return false;
    }

    @Override
    public List<VideoListener> getVideoListeners() {
        return null;
    }

    @Override
    public void addStateListener(StateListener listener) {

    }

    @Override
    public boolean removeStateListener(StateListener listener) {
        return false;
    }

    @Override
    public List<StateListener> getStateListeners() {
        return null;
    }

    @Override
    public TelloDroneState getCachedState() {
        return null;
    }

    @Override
    public void setCachedState(TelloDroneState cachedState) {

    }

    @Override
    public TelloVideoExportType getVideoExportType() {
        return null;
    }

    @Override
    public void setVideoExportType(TelloVideoExportType videoExportType) {

    }
}
