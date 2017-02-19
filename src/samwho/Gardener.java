package samwho;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public strictfp class Gardener extends Robot {
  @Override
  public void onCreate() {
    enqueue(1, setUpGardeningLocation());
  }

  @Override
  public void onIdle() {
    enqueue(0, waterSaddestNearbyTree());
  }

  private GameRunnable waterSaddestNearbyTree() {
    return () -> {
      TreeInfo tree = getSaddestNearbyTree();

      if (tree == null) {
        debug_out("no trees nearby to water");
        return;
      }

      if (!rc.canWater(tree.ID)) {
        debug_out("can't water tree with ID " + tree.ID);
        return;
      }

      rc.water(tree.ID);
    };
  }

  private GameRunnable setUpGardeningLocation() {
    return () -> {
      moveToGardeningLocation().run();

      int toPlant = 5;
      float treeRadius = 1.0f;
      float distance = rc.getType().bodyRadius + 0.01f + treeRadius;

      debug_out("planting trees...");

      List<MapLocation> ls = getSurroundingLocations(toPlant + 1, distance);
      while (true) {
        for (MapLocation l : ls) {
          Direction d = rc.getLocation().directionTo(l);
          if (rc.canPlantTree(d) && toPlant > 0) {
            rc.plantTree(d);
            toPlant--;

            // We don't want nearby trees to die while we're trying to establish
            // our gardening location, so make sure we water the saddest nearby
            // tree while we have a change during setup.
            waterSaddestNearbyTree().run();
          }
        }

        if (toPlant == 0) {
          debug_out("finished planting trees!");
          break;
        }
      }
    };
  }

  private GameRunnable moveToGardeningLocation() {
    return () -> {
      while (true) {
        if (!rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(), 3)) {
          debug_out("found gardening location!");
          break;
        }

        while (!rc.hasMoved()) {
          debug_out("moving to find gardening location");
          tryMove(randomDirection());
        }
      }
    };
  }

  private TreeInfo getSaddestNearbyTree() throws GameActionException {
    float treeRadius = 1.0f;
    float distance = rc.getType().bodyRadius + 0.01f + treeRadius;

    TreeInfo[] nearbyTrees = rc.senseNearbyTrees(distance, rc.getTeam());
    if (nearbyTrees.length == 0) {
      return null;
    }

    TreeInfo saddest = nearbyTrees[0];
    for (int i = 0; i < nearbyTrees.length; i++) {
      if (nearbyTrees[i].health < saddest.health) {
        saddest = nearbyTrees[i];
      }
    }

    return saddest;
  }
}
