package samwho.perf;

import battlecode.common.*;

public class BytecodeCounter {
  private RobotController rc;
  private int bytecodes;
  private int round;
  private int total;

  public BytecodeCounter(RobotController rc) {
    this.rc = rc;
    this.bytecodes = Clock.getBytecodeNum();
    this.round = rc.getRoundNum();
    this.total = 0;
  }

  /**
   * Gets the number of bytecodes elapsed since the last time this method was
   * called.
   */
  public int lap() {
    int b = Clock.getBytecodeNum();
    int ret = b - this.bytecodes;

    int rdiff = rc.getRoundNum() - this.round;
    ret += rc.getType().bytecodeLimit * rdiff;

    if (rdiff > 0) {
      this.round += rdiff;
    }

    this.bytecodes = b;
    this.total += ret;
    return ret;
  }

  /**
   * Gets the total number of bytecodes elapsed in the lifetime of this object.
   */
  public int total() {
    return this.total;
  }
}
