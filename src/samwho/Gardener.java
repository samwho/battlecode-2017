package samwho;
import battlecode.common.*;

import java.util.HashSet;
import java.util.Collections;
import java.util.Set;

public strictfp class Gardener extends Robot {
  private static final int NUM_TREES_TO_PLANT = 5;

  private Set<MapLocation> treeLocations;

  @Override
  public void onCreate() {
    enqueue(Integer.MAX_VALUE, () -> moveToGardeningLocation());
    build(RobotType.SOLDIER, rc.getLocation().directionTo(getTreeGap()));
  }

  @Override
  public void onBuildFinished() {
    build(RobotType.SOLDIER, rc.getLocation().directionTo(getTreeGap()));
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

  private boolean needMoreTrees() {
    return getMyTrees().length < NUM_TREES_TO_PLANT;
  }

  private TreeInfo[] getMyTrees() {
    return rc.senseNearbyTrees(3.0f, rc.getTeam());
  }

  // TODO(samwho): Fix this.
  private MapLocation getTreeGap() {
    for (TreeInfo tree : getMyTrees()) {
      if (!this.treeLocations.contains(tree.location)) {
        return tree.location;
      }
    }

    // Should never happen
    return null;
  }

  private void tryPlantTreeIfNeeded() throws GameActionException {
    if (!needMoreTrees()) {
      return;
    }

    Direction d = rc.getLocation().directionTo(getTreeGap());
    if (!rc.canPlantTree(d)) {
      return;
    }

    rc.plantTree(d);
  }

  private void moveToGardeningLocation() throws GameActionException {
    Utils.debug_out("attempting to find gardening location...");

    while (true) {
      // We're going to go with a hexagonal planting strategy. The idea behind
      // this is to plant 5 trees around us and then leave a gap for spawning
      // soldiers. As a result, the only condition for a gardening location at
      // the moment is enough space for our gardener and his surrounding
      // trees.
      if (!rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(), 3.03f)) {
        Utils.debug_out("found gardening location!");
        break;
      }

      // TODO(samwho): Improve pathing here. At the moment we've just settled
      // on randomly moving around until we stumble upon a valid gardening
      // location.
      while (!rc.hasMoved()) {
        tryMove(randomDirection());
      }

      // Can only move once per turn, no need to waste cycles.
      Clock.yield();
    }

    float treeRadius = 1.0f;
    float distance = rc.getType().bodyRadius + 0.01f + treeRadius;
    this.treeLocations =
      new HashSet(getSurroundingLocations(NUM_TREES_TO_PLANT + 1, distance));
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
