package samwho;
import battlecode.common.*;

public strictfp class RobotFactory {
  public static Robot create(RobotController rc) {
    Robot r;

    switch (rc.getType()) {
      case ARCHON:     r = new Archon();
      case GARDENER:   r = new Gardener();
      case SOLDIER:    r = new Soldier();
      case TANK:       r = null;
      case SCOUT:      r = null;
      case LUMBERJACK: r = new Lumberjack();
      default:         r = null;
    }

    r.setRobotController(rc);
    return r;
  }
}
