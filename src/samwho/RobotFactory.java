package samwho;
import battlecode.common.*;

public strictfp class RobotFactory {
  public static Robot create(RobotController rc) {
    switch (rc.getType()) {
      case ARCHON:     return new Archon(rc);
      case GARDENER:   return new Gardener(rc);
      case SOLDIER:    return new Soldier(rc);
      case TANK:       return null;
      case SCOUT:      return null;
      case LUMBERJACK: return new Lumberjack(rc);
      default:         return null;
    }
  }
}
