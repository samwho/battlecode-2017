package samwho.actions;

import samwho.*;
import samwho.robots.*;

import battlecode.common.*;

/**
 * An action for moving to a location over multiple turns.
 */
public strictfp class MoveAction extends Action {
  private static final float TOLERANCE = 0.001f;

  private Robot mover;
  private MapLocation destination;
  private boolean reachedDestination = false;

  public MoveAction(int priority, Robot mover, MapLocation destination) {
    super(priority, "move to " + destination);

    this.mover = mover;
    this.destination = destination;
  }

  @Override
  public boolean isDoable() {
    return !mover.getRobotController().hasMoved();
  }

  @Override
  public void run() throws GameActionException {
    RobotController rc = mover.getRobotController();

    if (!rc.onTheMap(destination, rc.getType().bodyRadius)) {
      throw new GameActionException(null,
          "asked to move somewhere that robot cannot physically occupy");
    }

    MapLocation myLocation = rc.getLocation();
    if (myLocation.distanceTo(destination) < TOLERANCE) {
      this.reachedDestination = true;
      return;
    }

    Direction moveDirection = myLocation.directionTo(destination);
    float distance = myLocation.distanceTo(destination);
    float strideRadius = rc.getType().strideRadius;
    if (distance > strideRadius) {
      distance = strideRadius;
    }

    // Make progress towards destination.
    mover.tryMove(moveDirection, distance);

    // More movement may need to be done, so re-queue the move action. This is
    // how we facilitate multi-turn movement.
    mover.enqueue(this);
  }

  /**
   * Because move actions can span multiple turns, this is used to signify
   * whether we got to our destination on this turn.
   */
  public boolean reachedDestination() {
    return this.reachedDestination;
  }
}
