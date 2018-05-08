package scripts;

public class StopWatch {
    /* The startTIme. */
    private long startTime;
    /* The elapsedTime. */
    private long elapsedTime;
    /* The isRunning indicator. */
    private boolean isRunningInd;

    /**
     * start().
     */
    public void start() {
        synchronized (this) {
            startTime = System.currentTimeMillis();
            isRunningInd = true;
        }
    }

    /**
     * stop().
     */
    public void stop() {
        synchronized (this) {
            elapsedTime = System.currentTimeMillis() - startTime;
            isRunningInd = false;
        }
    }

    /**
     * reset().
     */
    public void reset() {
        synchronized (this) {
            startTime = 0;
            elapsedTime = 0;
            isRunningInd = false;
        }
    }

    /**
     * getTime().
     *
     * @return the time.
     */
    public long getTime() {
        synchronized (this) {
            long currentElapsedTime = 0;
            if (isRunningInd) {
                currentElapsedTime = System.currentTimeMillis() - startTime;
            }
            return elapsedTime + currentElapsedTime;
        }
    }
}