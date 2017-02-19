package samwho;
import battlecode.common.*;

public strictfp class Archon extends Robot {
  @Override
  public void onCreate() {
    enqueue(1, () -> {
      Direction d = getUnoccupiedDirectionAroundMe();

      if (d == null) {
        out("wasn't able to find good direction to spawn gardener");
        return;
      }

      waitUntil(() -> rc.canHireGardener(d));
      rc.hireGardener(d);
    });
  }

  @Override
  public void onIdle() {
    enqueue(0, () -> {
      // Move randomly
      tryMove(randomDirection());

      // Broadcast archon's location for other robots on the team to
      // know
      MapLocation myLocation = rc.getLocation();
      rc.broadcast(0,(int)myLocation.x);
      rc.broadcast(1,(int)myLocation.y);
    });
  }
}
