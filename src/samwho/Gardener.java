package samwho;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public strictfp class Gardener extends Robot {
  private static final int NUM_TREES_TO_PLANT = 5;

  private List<MapLocation> treeLocations;

  @Override
  public void onCreate() {
    enqueue(Integer.MAX_VALUE, () -> moveToGardeningLocation());
    build(RobotType.SOLDIER);
  }

  @Override
  public void onBuildFinished() {
    build(RobotType.SOLDIER);
  }

  @Override
  public void onNewTurn() {
    enqueue(1, () -> tryPlantTreeIfNeeded());
    enqueue(0, () -> waterSaddestNearbyTree());
  }

  private void waterSaddestNearbyTree() throws GameActionException {
    if (!rc.canWater()) {
      return;
    }

    TreeInfo tree = getSaddestNearbyTree();
    if (tree == null) {
      return;
    }

    if (!rc.canWater(tree.ID)) {
      return;
    }

    rc.water(tree.ID);
  }

  private boolean needMoreTrees() throws GameActionException {
    return getMyTrees().length < NUM_TREES_TO_PLANT;
  }

  private TreeInfo[] getMyTrees() throws GameActionException {
    return rc.senseNearbyTrees(3.0f, rc.getTeam());
  }

  private void tryPlantTreeIfNeeded() throws GameActionException {
    if (!needMoreTrees()) {
      return;
    }

    for (MapLocation l : this.treeLocations) {
      TreeInfo tree = rc.senseTreeAtLocation(l);

      // There is already a tree at the given location, skip.
      if (tree != null) {
        continue;
      }

      // We need to plant a tree, but were for some reason unable to.
      Direction d = rc.getLocation().directionTo(l);
      if (!rc.canPlantTree(d)) {
        continue;
      }

      rc.plantTree(d);
    }
  }

  private void moveToGardeningLocation() throws GameActionException {
    while (true) {
      // We're going to go with a hexagonal planting strategy. The idea behind
      // this is to plant 5 trees around us and then leave a gap for spawning
      // soldiers. As a result, the only condition for a gardening location at
      // the moment is enough space for our gardener and his surrounding
      // trees.
      if (!rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(), 3)) {
        debug_out("found gardening location!");
        break;
      }

      // TODO(samwho): Improve pathing here. At the moment we've just settled
      // on randomly moving around until we stumble upon a valid gardening
      // location.
      while (!rc.hasMoved()) {
        debug_out("moving to find gardening location");
        tryMove(randomDirection());
      }

      // Can only move once per turn, no need to waste cycles.
      Clock.yield();
    }

    float treeRadius = 1.0f;
    float distance = rc.getType().bodyRadius + 0.01f + treeRadius;
    this.treeLocations =
      getSurroundingLocations(NUM_TREES_TO_PLANT + 1, distance);
  }

  // Returns the lowest health tree in the immediate vicinity of this gardener.
  private TreeInfo getSaddestNearbyTree() throws GameActionException {
    TreeInfo[] myTrees = getMyTrees();
    if (myTrees.length == 0) {
      return null;
    }

    // TODO(samwho): There's probably a nicer thing we can do here with
    // Collections.min and lambdas, but I couldn't figure it out.
    TreeInfo saddest = myTrees[0];
    for (int i = 0; i < myTrees.length; i++) {
      if (myTrees[i].health < saddest.health) {
        saddest = myTrees[i];
      }
    }

    return saddest;
  }
}
