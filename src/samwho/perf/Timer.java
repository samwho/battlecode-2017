package samwho.perf;

import samwho.*;
import samwho.functional.*;

import battlecode.common.*;

import java.io.Closeable;

public class Timer implements Closeable {
  private static final boolean ENABLED = false;

  private Timer parent;
  private BytecodeCounter bc;
  private RobotController rc;
  private String desc;
  private int overhead;
  private int recorded;

  private static class FakeTimer extends Timer {
    public FakeTimer(RobotController rc, String desc) { super(rc, desc); }
    @Override public void prep() { }
    @Override public int record(String message) { return 0; }
    @Override public void close() { }
  }

  public static Timer create(RobotController rc, String desc) {
    if (!ENABLED) {
      return new FakeTimer(rc, desc);
    }

    return new Timer(rc, desc);
  }

  private Timer(RobotController rc, String desc) {
    this.rc = rc;
    this.bc = new BytecodeCounter(rc);
    this.desc = desc;
    this.overhead = 0;
    this.recorded = 0;
  }

  /**
   * Prepare to time a region of code.
   *
   * Any time elapsed up to this point is counted as overhead.
   */
  public void prep() {
    overhead += bc.lap();
  }

  /**
   * Record a named time interval in your code.
   *
   * Time is added to the recorded time (not overhead), and a message is printed
   * using Utils.debug_out.
   */
  public int record(String message) {
    int l = bc.lap();
    Utils.debug_out(l + " -> " + desc + ":" + message);
    recorded += l;
    return l;
  }

  @Override
  public void close() {
    prep();
    Utils.debug_out("\n\nTimer[" + desc + "] ->\n" +
        "  - recorded " + recorded + "\n" +
        "  - overhead " + overhead + "\n" +
        "  - total " + bc.total());
  }
}
