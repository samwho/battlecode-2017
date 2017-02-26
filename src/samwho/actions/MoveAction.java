package samwho.actions;

import samwho.*;
import samwho.robots.*;

import battlecode.common.*;

/**
 * An action for moving to a location over multiple turns.
 */
public strictfp class MoveAction extends Action {
  private static final float TOLERANCE = 0.01f;

  private Robot mover;
  private MapLocation destination;

  public MoveAction(int priority, Robot mover, MapLocation destination) {
    super(priority, "move to " + destination);

    this.mover = mover;
    this.destination = destination;
  }

  @Override
  public boolean run() throws GameActionException {
    RobotController rc = mover.getRobotController();

    if (rc.hasMoved()) {
      return false;
    }

    if (!rc.onTheMap(destination, rc.getType().bodyRadius)) {
      this.cancel();
      return false;
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

    // Make progress towards destination.
    mover.tryMove(moveDirection, distance, 30 /* offset */, 6 /* attempts */);
    return false;
  }
}
