package samwho.actions;

import samwho.*;
import samwho.robots.*;

import battlecode.common.*;

public strictfp class MoveAction extends Action {
  private Robot mover;
  private MapLocation destination;

  public MoveAction(int priority, Robot mover, MapLocation destination) {
    super(priority);

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

    MapLocation myLocation = rc.getLocation();
    if (myLocation.equals(destination)) {
      // Destination reached! \o/
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
}
