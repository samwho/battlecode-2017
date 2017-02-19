package samwho;
import battlecode.common.*;

public strictfp class RobotFactory {
  public static Robot create(RobotController rc) {
    Robot r;

    switch (rc.getType()) {
      case ARCHON:     r = Archon();
      case GARDENER:   r = Gardener();
      case SOLDIER:    r = Soldier();
      case TANK:       r = null;
      case SCOUT:      r = null;
      case LUMBERJACK: r = Lumberjack();
      default:         r = null;
    }

    r.setRobotController(rc);
    return r;
  }
}
