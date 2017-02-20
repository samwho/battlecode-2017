package samwho;

import battlecode.common.*;

public strictfp class Utils {
  /**
   * Debug-only output.
   *
   * Will be stripped from tournament and scrimming games, and won't count
   * towards the bytecode limit.
   */
  public static void debug_out(String message) {
    System.out.println(message);
  }
}
