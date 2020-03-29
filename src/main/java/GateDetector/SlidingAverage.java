package GateDetector;

public class SlidingAverage {
    private int size;
    private int[] wnd;
    private int wrIndex;
    private int sum;
    private int validCnt;

    public SlidingAverage(int size) {
        this.size = size;
        wrIndex = 0;
        wnd = new int[size];
        validCnt = 0;
    }

    public void addValue(int val) {
        if (validCnt < size) {
            validCnt++;
            sum += val;
        } else {
            sum = sum - wnd[wrIndex] + val;
        }
        wnd[wrIndex] = val;
        wrIndex = wrIndex == (size - 1) ? 0 : wrIndex + 1;
    }

    public boolean isFilled() {
        return validCnt >= size;
    }

    public double getAverage() {
        return ((double) sum) / size;
    }

}