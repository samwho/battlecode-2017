package samwho;
import battlecode.common.*;

public strictfp class RobotFactory {
  public static Robot create(RobotController rc) {
    Robot r;

    switch (rc.getType()) {
      case ARCHON:
        r = new Archon();
        break;
      case GARDENER:
        r = new Gardener();
        break;
      case SOLDIER:
        r = new Soldier();
        break;
      case TANK:
        r = null;
        break;
      case SCOUT:
        r = null;
        break;
      case LUMBERJACK:
        r = new Lumberjack();
        break;
      default:
        r = null;
    }

    if (r != null) {
      r.setRobotController(rc);
    }

    return r;
  }
}
