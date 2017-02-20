package samwho;

import samwho.robots.*;

import battlecode.common.*;

public strictfp class RobotPlayer {
  @SuppressWarnings("unused")
  public static void run(RobotController rc) {
    Robot r = RobotFactory.create(rc);

    if (r == null) {
      System.err.println("unknown robot type");
      return;
    }

    r.run();
  }
}
