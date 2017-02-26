package samwho.perf;

import samwho.*;
import samwho.functional.*;

import battlecode.common.*;

import java.io.Closeable;

public class Timer implements Closeable {
  private Timer parent;
  private BytecodeCounter bc;
  private RobotController rc;
  private String desc;
  private int overhead;
  private int recorded;

  public Timer(RobotController rc, String desc) {
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
   * Discards time elapsed up until this point.
   */
  public void discard() {
    bc.lap();
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
