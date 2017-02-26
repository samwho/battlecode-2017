package samwho;

import java.io.Closeable;

import battlecode.common.*;

public class BytecodeCounter implements Closeable {
  private RobotController rc;
  private int bytecodes;
  private int round;
  private int total;
  private String desc;

  public BytecodeCounter(RobotController rc, String desc) {
    this.rc = rc;
    this.desc = desc;
    this.bytecodes = Clock.getBytecodeNum();
    this.round = rc.getRoundNum();
    this.total = 0;
  }

  public int lap() {
    int rdiff = rc.getRoundNum() - this.round;

    int b = Clock.getBytecodeNum();
    int ret = b - this.bytecodes;

    if (rdiff > 0) {
      ret += rc.getType().bytecodeLimit * (rdiff + 1);
    }

    this.bytecodes = b;
    this.total += ret;
    return ret;
  }

  public int total() {
    return this.total;
  }

  @Override
  public String toString() {
    return "BytecodeCounter[" + desc + "] -> " + total;
  }

  @Override
  public void close() {
    lap();
    Utils.debug_out(this.toString());
  }
}
