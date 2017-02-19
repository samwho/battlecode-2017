package samwho;
import battlecode.common.*;

public strictfp class Gardener extends Robot {
  @Override
  public void onIdle() {
    enqueue(0, () -> {
      // Listen for home archon's location
      int xPos = rc.readBroadcast(0);
      int yPos = rc.readBroadcast(1);
      MapLocation archonLoc = new MapLocation(xPos,yPos);

      // Generate a random direction
      Direction dir = randomDirection();

      // Randomly attempt to build a soldier or lumberjack in this direction
      if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
        rc.buildRobot(RobotType.SOLDIER, dir);
      } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) &&
          Math.random() < .01 && rc.isBuildReady()) {
        rc.buildRobot(RobotType.LUMBERJACK, dir);
      }

      // Move randomly
      tryMove(randomDirection());
    });
  }
}
