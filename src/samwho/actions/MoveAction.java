package samwho.actions;

import samwho.*;
import samwho.robots.*;

import battlecode.common.*;

/**
 * An action for moving to a location over multiple turns.
 */
public strictfp class MoveAction extends Action {
  private static final float TOLERANCE = 0.01f;

  private RobotController rc;
  private Robot mover;
  private MapLocation destination;

  public MoveAction(int priority, Robot mover, MapLocation destination) {
    super(priority, "move to " + destination);

    this.rc = mover.getRobotController();
    this.mover = mover;
    this.destination = destination;
  }

  @Override
  public boolean run() throws GameActionException {
    rc.setIndicatorDot(destination, 0, 255, 0);

    if (rc.hasMoved()) {
      return false;
    }

    float bodyRadius = rc.getType().bodyRadius;
    if (rc.canSenseAllOfCircle(destination, bodyRadius) &&
        (!rc.onTheMap(destination, bodyRadius) ||
         rc.isCircleOccupiedExceptByThisRobot(destination, bodyRadius))) {
      // We're no longer able to actually get to our destination because it is
      // occupied by something, or we have found that it is not on the map.
      rc.setIndicatorDot(destination, 255, 0, 0);
      this.cancel();
      return true;
    }

    MapLocation myLocation = rc.getLocation();
    if (myLocation.distanceTo(destination) < TOLERANCE) {
      return true;
    }

    Direction moveDirection = myLocation.directionTo(destination);
    float distance = myLocation.distanceTo(destination);
    float strideRadius = rc.getType().strideRadius;
    if (distance > strideRadius) {
      distance = strideRadius;
    }

    MapLocation movingTo = myLocation.add(moveDirection, distance);
    rc.setIndicatorLine(myLocation, movingTo, 0, 0, 255);
    rc.setIndicatorLine(movingTo, destination, 0, 0, 255);

    // Make progress towards destination.
    mover.tryMove(moveDirection, distance, 30 /* offset */, 6 /* attempts */);
    return false;
  }
}
