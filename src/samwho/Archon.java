package samwho;
import battlecode.common.*;

public strictfp class Archon extends Robot {
  @Override
  public void onCreate() {
    build(RobotType.GARDENER);
  }

  @Override
  public void onIdle() {
    enqueue(0, () -> {
      if (rc.hasMoved()) {
        Clock.yield();
        return;
      }

      // Move randomly
      tryMove(randomDirection());

      // Broadcast archon's location for other robots on the team to
      // know
      MapLocation myLocation = rc.getLocation();
      rc.broadcastFloat(0, myLocation.x);
      rc.broadcastFloat(1, myLocation.y);
    });
  }
}
