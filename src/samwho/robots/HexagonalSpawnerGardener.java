package samwho.robots;

import samwho.*;
import samwho.actions.*;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;
import java.util.Set;

public strictfp class HexagonalSpawnerGardener extends Gardener {
  private static final int NUM_TREES_TO_PLANT = 5;
  private static final int NUM_SPACES_AROUND_ME = NUM_TREES_TO_PLANT + 1;

  private Set<MapLocation> treeLocations;
  private boolean inPosition = false;

  @Override
  public void onCreate() {
    run(() -> {
      moveToGardeningLocation();

      int i = 0;
      for (MapLocation l : treeLocations) {
        Direction d = rc.getLocation().directionTo(l);

        // If we're on the last of the tree planting locations, don't plan a
        // tree. Instead, save the space as a spawning location.
        if (i == NUM_TREES_TO_PLANT) {
          // Kick off the build loop.
          build(RobotType.SOLDIER, d);
          break;
        }

        // TODO(samwho): cancel?
        plant(Integer.MAX_VALUE, d);

        i++;
      }
    });
  }

  @Override
  public void onBuildFinished(BuildAction ba) {
    build(ba.getType(), ba.getDirection());
  }

  @Override
  public void onNewRound(int round) {
    if (inPosition) {
      run(() -> {
        tryPlantTreeIfNeeded();
        waterSaddestNearbyTree();
      });
    }
  }

  private boolean needMoreTrees() {
    return getMyTrees().length < NUM_TREES_TO_PLANT;
  }

  private TreeInfo[] getMyTrees() {
    return rc.senseNearbyTrees(3.0f, rc.getTeam());
  }

  private MapLocation getTreeGap() {
    Set<MapLocation> myTreeLocations = new HashSet();
    for (TreeInfo tree : getMyTrees()) {
      myTreeLocations.add(tree.location);
    }

    for (MapLocation l : this.treeLocations) {
      if (!myTreeLocations.contains(l)) {
        return l;
      }
    }

    // Should never happen
    return null;
  }

  /**
   * Check to make sure we're not setting up camp too close to another gardener.
   */
  private boolean isOtherGardenerNearby() {
    for (RobotInfo robot : rc.senseNearbyRobots()) {
      if (robot.type == RobotType.GARDENER && robot.team == rc.getTeam()) {
        return true;
      }
    }

    return false;
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

  private boolean isGoodLocation() throws GameActionException {
    float treeRadius = 1.00f;
    float myRadius = rc.getType().bodyRadius;
    float buffer = 0.5f; // so we don't set up too close to walls

    float radius = (2 * treeRadius) + myRadius + buffer;
    return !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(), radius) &&
      !isOtherGardenerNearby();
  }

  private void moveToGardeningLocation() throws GameActionException {
    if (inPosition) {
      Utils.debug_out("moveToGardeningLocation() called again");
      return;
    }

    Utils.debug_out("attempting to find gardening location...");
    while (true) {
      // We're going to go with a hexagonal planting strategy. The idea behind
      // this is to plant 5 trees around us and then leave a gap for spawning
      // soldiers. As a result, the only condition for a gardening location at
      // the moment is enough space for our gardener and his surrounding
      // trees.
      if (isGoodLocation()) {
        Utils.debug_out("found gardening location!");
        break;
      }

      // TODO(samwho): Improve pathing here. At the moment we've just settled
      // on randomly moving around until we stumble upon a valid gardening
      // location.
      while (!rc.hasMoved()) {
        tryMove(Utils.randomDirection());
      }

      // Can only move once per turn, no need to waste cycles.
      Clock.yield();
    }

    float treeRadius = 1.0f;
    float distance = rc.getType().bodyRadius + 0.01f + treeRadius;
    this.treeLocations =
      new HashSet(getSurroundingLocations(NUM_SPACES_AROUND_ME, distance));

    this.inPosition = true;
  }

  /**
   * Attempts to water the saddest (lowest HP) nearby tree.
   *
   * TODO(samwho): factor this out into the Gardener abstract class in some
   * sensible way.
   */
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

  /**
   * Returns the lowest health tree in the immediate vicinity of this gardener.
   *
   * TODO(samwho): factor this out into the Gardener abstract class in some
   * sensible way.
   */
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
