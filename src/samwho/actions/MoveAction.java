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

  private boolean canMoveTo(MapLocation l) throws GameActionException {

    return true;
  }

  @Override
  public boolean run() throws GameActionException {
    rc.setIndicatorDot(destination, 0, 255, 0);
    if (rc.hasMoved()) return false;

    RobotType t = rc.getType();
    MapLocation myLocation = rc.getLocation();

    if (myLocation.distanceTo(destination) < TOLERANCE) {
      return true;
    }

    Direction moveDirection = myLocation.directionTo(destination);
    float distance = myLocation.distanceTo(destination);

    if (!rc.canSenseAllOfCircle(destination, t.bodyRadius)) {
      mover.tryMove(moveDirection, distance, 30 /* offset */, 6 /* attempts */);
      return false;
    }

    if (!rc.onTheMap(destination, t.bodyRadius)) {
      rc.setIndicatorDot(destination, 255, 0, 0);
      this.cancel("destination not on map");
      return false;
    }


    if (rc.isCircleOccupiedExceptByThisRobot(destination, t.bodyRadius)) {
      if (t == RobotType.SCOUT) {
        if (rc.isLocationOccupiedByRobot(destination)) {
          this.cancel("destination occupied");
          return false;
        }
      } else {
        if (rc.isLocationOccupied(destination)) {
          this.cancel("destination occupied");
          return false;
        }
      }
    }

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
