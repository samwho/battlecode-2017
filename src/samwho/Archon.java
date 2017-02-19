package samwho;
import battlecode.common.*;

public strictfp class Archon extends Robot {
  @Override
  public void onCreate() {
    enqueue(1, spawnGardener());
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

  private GameRunnable spawnGardener() {
    return () -> {
      waitUntil(() -> rc.isBuildReady());
      waitUntil(() -> rc.getTeamBullets() >= RobotType.GARDENER.bulletCost);

      Direction d = getUnoccupiedBuildDirectionFor(RobotType.GARDENER);
      if (d == null) {
        debug_out("wasn't able to find good direction to spawn gardener");
        return;
      }

      if (!rc.canHireGardener(d)) {
        debug_out("couldn't hire gardener");
        return;
      }

      rc.hireGardener(d);
    };
  }
}
