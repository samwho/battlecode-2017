package samwho;

import java.util.function.Predicate;
import java.util.PriorityQueue;

import battlecode.common.*;

public abstract strictfp class Robot {
  protected RobotController rc;
  private PriorityQueue<Action> actions = new PriorityQueue<>();

  public void setRobotController(RobotController rc) {
    this.rc = rc;
  }


  public void onIdle() { }
  public void onCreate() { }

  public void run() {
    onCreate();

    while (true) {
      Action action = actions.poll();
      if (action == null) {
        debug_out("no actions pending, calling onIdle()");
        onIdle();
        continue;
      }

      debug_out("got action, attempting to execute...");

      try {
        action.run();
      } catch (GameActionException e) {
        e.printStackTrace();
      }

      debug_out("finished running action, relooping");
    }
  }

  void out(String message) {
    System.out.println(message);
  }

  void debug_out(String message) {
    System.out.println(message);
  }

  void enqueue(int priority, GameRunnable action) {
    actions.add(new Action(priority, action));
  }

  void waitUntil(GamePredicate predicate) throws GameActionException {
    debug_out("waitUntil called...");

    while (!predicate.test()) {
      debug_out("condition not met, yielding");
      Clock.yield();
    }

    debug_out("condition met, continuing");
  }

  MapLocation getUnoccupiedLocationAroundMe() throws GameActionException {
    int attempts = 32;

    while (attempts-- > 0) {
      Direction d = randomDirection();
      MapLocation l = rc.getLocation().add(d, rc.getType().bodyRadius + 0.01f);

      if (!rc.isLocationOccupied(l)) {
        return l;
      }
    }

    return null;
  }

  Direction getUnoccupiedDirectionAroundMe() throws GameActionException {
    return rc.getLocation().directionTo(getUnoccupiedLocationAroundMe());
  }

  float deg2rad(float deg) {
    return deg * ((float)Math.PI / 180.0f);
  }

  float rad2deg(float rad) {
    return rad * (180.0f * (float)Math.PI);
  }

  /**
   * Returns a random Direction
   * @return a random Direction
   */
  Direction randomDirection() {
    return new Direction((float)Math.random() * 2 * (float)Math.PI);
  }

  /**
   * Attempts to move in a given direction, while avoiding small obstacles
   * directly in the path.
   *
   * @param dir The intended direction of movement
   * @return true if a move was performed
   * @throws GameActionException
   */
  boolean tryMove(Direction dir) throws GameActionException {
    return tryMove(dir,20,3);
  }

  /**
   * Attempts to move in a given direction, while avoiding small obstacles
   * direction in the path.
   *
   * @param dir The intended direction of movement
   * @param degreeOffset Spacing between checked directions (degrees)
   * @param checksPerSide Number of extra directions checked on each side, if
   * intended direction was unavailable
   * @return true if a move was performed
   * @throws GameActionException
   */
  boolean tryMove(Direction dir, float degreeOffset, int checksPerSide)
    throws GameActionException {
    // First, try intended direction
    if (rc.canMove(dir)) {
      rc.move(dir);
      return true;
    }

    // Now try a bunch of similar angles
    boolean moved = false;
    int currentCheck = 1;

    while(currentCheck<=checksPerSide) {
      // Try the offset of the left side
      if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
        rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
        return true;
      }
      // Try the offset on the right side
      if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
        rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
        return true;
      }
      // No move performed, try slightly further
      currentCheck++;
    }

    // A move never happened, so return false.
    return false;
  }

  /**
   * A slightly more complicated example function, this returns true if the
   * given bullet is on a collision course with the current robot. Doesn't take
   * into account objects between the bullet and this robot.
   *
   * @param bullet The bullet in question
   * @return True if the line of the bullet's path intersects with this robot's
   * current position.
   */
  boolean willCollideWithMe(BulletInfo bullet) {
    MapLocation myLocation = rc.getLocation();

    // Get relevant bullet information
    Direction propagationDirection = bullet.dir;
    MapLocation bulletLocation = bullet.location;

    // Calculate bullet relations to this robot
    Direction directionToRobot = bulletLocation.directionTo(myLocation);
    float distToRobot = bulletLocation.distanceTo(myLocation);
    float theta = propagationDirection.radiansBetween(directionToRobot);

    // If theta > 90 degrees, then the bullet is traveling away from us and we
    // can break early
    if (Math.abs(theta) > Math.PI/2) {
      return false;
    }

    // distToRobot is our hypotenuse, theta is our angle, and we want to know
    // this length of the opposite leg.  This is the distance of a line that
    // goes from myLocation and intersects perpendicularly with
    // propagationDirection.  This corresponds to the smallest radius circle
    // centered at our location that would intersect with the line that is the
    // path of the bullet.
    float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

    return (perpendicularDist <= rc.getType().bodyRadius);
  }
}
