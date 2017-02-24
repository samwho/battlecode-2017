package samwho.robots;

import samwho.*;
import samwho.actions.*;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public strictfp class HexagonalSpawnerGardener extends Gardener {
  private MapLocation gap;
  private Set<MapLocation> treeLocations;
  private boolean inPosition = false;
  private int treesPlanted = 0;
  private int treesScheduled = 0;

  @Override
  public void onCreate() {
    run("onCreate", () -> moveToPotentialGardeningLocation());
  }

  @Override
  public void onMoveFinished(MoveAction ma) {
    // Still mid-way through a multi-round move.
    if (!ma.reachedDestination()) {
      return;
    }

    run("onMoveFinished", () -> {
      if (!isGoodLocation(rc.getLocation())) {
        moveToPotentialGardeningLocation();
        return;
      }

      float treeRadius = 1.0f;
      float distance = rc.getType().bodyRadius + 0.01f + treeRadius;

      List<MapLocation> sc = getSurroundingCircles(treeRadius, distance);
      this.gap = sc.remove(0); // for spawning out of
      this.treeLocations = new HashSet(sc);
      this.inPosition = true;

      for (MapLocation l : this.treeLocations) {
        // TODO(samwho): cancel?
        plant(Integer.MAX_VALUE, rc.getLocation().directionTo(l));
        treesScheduled++;
      }

      build(RobotType.SOLDIER, rc.getLocation().directionTo(this.gap));
    });
  }

  @Override
  public void onTreePlanted(PlantTreeAction pta) {
    treesPlanted++;
  }

  @Override
  public void onBuildFinished(BuildAction ba) {
    build(ba.getType(), ba.getDirection());
  }

  @Override
  public void onNewRound(int round) {
    if (inPosition) {
      run("water sad trees", () -> waterSaddestNearbyTree());

      if (treesPlanted == treesScheduled) {
        run("replace missing trees", () -> {
          if (isMissingTrees()) {
            plantMissingTrees();
          }
        });
      }
    }
  }

  private TreeInfo[] getMyTrees() {
    return rc.senseNearbyTrees(gardenRadius(), rc.getTeam());
  }

  private boolean isMissingTrees() throws GameActionException {
    for (MapLocation l : this.treeLocations) {
      if (rc.senseTreeAtLocation(l) == null) {
        return true;
      }
    }

    return false;
  }

  /*
  private MapLocation getTreeGap() {
    Set<MapLocation> myTreeLocations = new HashSet<>();
    for (TreeInfo tree : getMyTrees()) {
      myTreeLocations.add(tree.location);
    }

    for (MapLocation l : this.treeLocations) {
      if (!myTreeLocations.contains(l)) {
        return l;
      }
    }

    Utils.debug_out("unable to find tree gap");

    // Should never happen
    return null;
  }
  */

  private void plantMissingTrees() throws GameActionException {
    for (MapLocation l : this.treeLocations) {
      if (rc.senseTreeAtLocation(l) == null) {
        plant(Integer.MAX_VALUE, rc.getLocation().directionTo(l));
        treesScheduled++;
      }
    }
  }

  private boolean isGoodLocation(MapLocation l) throws GameActionException {
    return rc.onTheMap(l, gardenRadius()) &&
      !rc.isCircleOccupiedExceptByThisRobot(l, gardenRadius());
  }

  private float gardenRadius() {
    float treeRadius = 1.00f;
    float myRadius = rc.getType().bodyRadius;
    float buffer = 1.0f; // so we don't set up too close to walls and whatnot

    return (2 * treeRadius) + myRadius + buffer;
  }

  private void moveToPotentialGardeningLocation() throws GameActionException {
    if (inPosition) {
      throw new GameActionException(null,
          "moveToPotentialGardeningLocation() called after in position");
    }

    float distance = rc.getType().sensorRadius - gardenRadius() - 0.01f;
    List<MapLocation> potentialSpots = getNSurroundingCircles(12, distance);

    for (MapLocation l : potentialSpots) {
      if (isGoodLocation(l)) {
        Utils.debug_out("found possible good location: " + l);
        moveTo(l);
        return;
      }
    }

    // Nothing good nearby? Run around like a nob.
    MapLocation l =
      rc.getLocation().add(Utils.randomDirection(), rc.getType().strideRadius);

    Utils.debug_out("no good locations, moving randomly to: " + l);
    moveTo(l);
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
