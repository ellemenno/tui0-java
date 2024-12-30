
public class FrameTimer {
  private long NSPF = 16666666; // nanoseconds (1e+9) alotted per frame
  private long lastFrameNS = 0;
  private long fpsTimerNS = 0;
  private int frames = 0;
  private long[] sleepSamples = null; // keep a moving average of sleep time
  private long sleepSum = 0;
  private int sleepSampleIndex = 0;
  private int fpsUpdateSec = 3;
  private int fps = 0;
  private long sleepingMS = 0;
  private long sleepAvgMS = 0;

  public FrameTimer(int targetFps, int refreshSec) {
    NSPF = (long) (1e+9/targetFps);
    fpsUpdateSec = refreshSec;
    sleepSamples = new long[targetFps*fpsUpdateSec];
  }

  public void start() {
    lastFrameNS = System.nanoTime();
  }

  public void nextFrame() {
    // take time sample
    long now = System.nanoTime();
    long frameNS = now - lastFrameNS;
    lastFrameNS = now;
    sleepingMS = (NSPF > frameNS) ? (long) ((NSPF - frameNS) * 1e-6) : 0;

    // update sleep average
    sleepSum -= sleepSamples[sleepSampleIndex];
    sleepSum += sleepingMS;
    sleepSamples[sleepSampleIndex] = sleepingMS;
    sleepSampleIndex = (sleepSampleIndex+1) % sleepSamples.length;

    // refresh values every so often
    frames++;
    if(fpsTimerNS + fpsUpdateSec*1e+9 < now) {
      fps = (int) (frames / ((now-fpsTimerNS)*1e-9) / fpsUpdateSec);
      fpsTimerNS = (long) (now + fpsUpdateSec*1e+9);
      frames = 0;
      sleepAvgMS = (int) (sleepSum / sleepSamples.length);
    }
  }

  public int getFps() { return fps; }

  public int getFpsRefreshSec() { return fpsUpdateSec; }

  public long getSleepingMS() { return sleepingMS; }

  public long getSleepAvgMS() { return sleepAvgMS; }

}
