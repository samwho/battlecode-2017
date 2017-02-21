package samwho.robots;

import samwho.*;
import samwho.actions.*;

import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import battlecode.common.*;

public abstract strictfp class Robot {
  private static final int MAX_IDLE_PER_TURN = 2;
  private static final int BUILD_PRIORITY = 127;

  protected RobotController rc;
  private PriorityQueue<Action> actionQueue = new PriorityQueue<>();
  private PriorityQueue<BuildAction> buildQueue = new PriorityQueue<>();

  // For checking when the round has changed.
  private int round = 0;

  /**
   * Only to be called by RobotFactory.
   *
   * I experimented with having this as a static member but that doesn't play
   * nicely with lambdas.
   */
  public void setRobotController(RobotController rc) {
    this.rc = rc;
  }


  /**
   * Called when robots are "idle".
   *
   * A robot is idle if it has finished running all queued actions. This
   * function is inteded for queueing up work when all other work has finished.
   */
  public void onIdle() { }

  /**
   * Called once, and only once, on a robot's creation.
   */
  public void onCreate() { }

  /**
   * Called at most once per turn.
   *
   * It's possible that this skips turns if actions run for a long time.
   */
  public void onNewTurn() { }

  /**
   * Called after a build has finished.
   *
   * The intended purpose of this is for you to maintain a clean stream of
   * robots to build.
   */
  public void onBuildFinished(RobotType type) { }

  /**
   * Starts the infinite loop of robot behaviour.
   */
  public void run() {
    int idleCount = 0;

    onCreate();

    while (true) {
      // We're in a new round now, so we can reset the idle counter.
      if (isNewRound()) {
        idleCount = 0;
        onNewTurn();

        try {
          if (buildQueue.size() > 0) {
            if (buildQueue.peek().isDoable()) {
              BuildAction ba = buildQueue.poll();
              ba.run();

              onBuildFinished(ba.getType());
            }
          }
        } catch (GameActionException e) {
          e.printStackTrace();
        }
      }

      Action action = actionQueue.poll();
      if (action == null) {
        // In order to not have our robots spinning doing nothing and consuming
        // the max number of bytecodes per turn, we have this yield after
        // MAX_IDLE_PER_TURN idle calls.
        if (idleCount >= MAX_IDLE_PER_TURN) {
          Clock.yield();
          continue;
        }

        onIdle();
        idleCount++;
        continue;
      }

      try {
        action.run();
      } catch (GameActionException e) {
        // TODO(samwho): Is it best to die instead?
        e.printStackTrace();
      }
    }
  }

  public RobotController getRobotController() {
    return this.rc;
  }

  /**
   * Check whether this robot has anything to do.
   */
  public boolean idle() {
    return actionQueue.isEmpty() && buildQueue.isEmpty();
  }

  /**
   * Check for friendly robots between me and another robot.
   *
   * Intended use is for avoiding friendly fire.
   */
  boolean anyFriendliesBetweenMeAnd(RobotInfo other)
    throws GameActionException {
    Team friendly = rc.getTeam();
    Team enemy = friendly.opponent();
    MapLocation me = rc.getLocation();
    Direction d = me.directionTo(other.location);

    int distance = (int)me.distanceTo(other.location);
    for (int i = 1; i < distance; i++) {
      MapLocation check = me.add(d, i);
      RobotInfo[] robots = rc.senseNearbyRobots(check, 1.0f, friendly);

      for (RobotInfo robot : robots) {
      // No robot at current check location? Continue to next iteration.
        if (robot == null) {
          continue;
        }

        // We found a robot but it's actually the robot we're looking for other
        // robots between, so we just return false early.
        if (robot.ID == other.ID) {
          return false;
        }

        // Above checks failed, which means we found a friendly.
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if the round has changed since this was last called.
   */
  boolean isNewRound() {
    int cur = rc.getRoundNum();
    if (cur != this.round) {
      this.round = cur;
      return true;
    }

    return false;
  }

  /**
   * Queues up an action.
   *
   * The main loop of all robots is based on a priority queue of Actions. An
   * Action is simply a GameRunnable and an int, with the int representing the
   * priority (higher being higher priority).
   *
   * Given that each robot is independent, no restrictions on the priority are
   * given. Robots are free to determine their own priorities.
   */
  void enqueue(Action action) {
    actionQueue.add(action);
  }

  void enqueue(int priority, GameRunnable runnable) {
    actionQueue.add(new RunnableAction(priority, runnable));
  }

  void enqueue(GameRunnable runnable) {
    actionQueue.add(new RunnableAction(0, runnable));
  }

  void build(int priority, RobotType type, Direction d) {
    buildQueue.add(new BuildAction(this, priority, type, d));
  }

  void build(int priority, RobotType type) {
    build(priority, type, null);
  }

  void build(RobotType type, Direction d) {
    build(BUILD_PRIORITY, type, d);
  }

  void build(RobotType type) {
    build(BUILD_PRIORITY, type);
  }

  /**
   * Waits until a condition is true.
   *
   * Evaluates a given predicate every turn until that condition is true, then
   * returns.
   */
  void waitUntil(GamePredicate predicate) throws GameActionException {
    Utils.debug_out("waitUntil called...");

    while (!predicate.test()) {
      Utils.debug_out("condition not met, yielding");
      Clock.yield();
    }

    Utils.debug_out("condition met, continuing");
  }

  protected boolean trySpawn(RobotType type) throws GameActionException {
    Direction d = getUnoccupiedBuildDirectionFor(type);
    if (d == null) {
      Utils.debug_out("wasn't able to find good direction to spawn " + type.name());
      return false;
    }

    if (!rc.canBuildRobot(type, d)) {
      Utils.debug_out("unable to spawn " + type.name() + " at " + d);
      return false;
    }

    rc.buildRobot(type, d);
    return true;
  }

  /**
   * Gets a valid direction for building, or returns null.
   *
   * When you want to build a unit and don't care what direction you build it
   * in, this function will give you a direction that's not occupied by
   * anything.
   *
   * If no such direction exists, returns null.
   */
  public Direction getUnoccupiedBuildDirectionFor(RobotType other)
    throws GameActionException {
    float distance = rc.getType().bodyRadius + 0.01f + other.bodyRadius;
    for (MapLocation l : getSurroundingLocations(6, distance)) {
      if (!rc.isCircleOccupied(l, other.bodyRadius)) {
        return rc.getLocation().directionTo(l);
      }
    }

    return null;
  }

  /**
   * Get a number of equally distributed surrounding locations a set distance
   * away.
   *
   * Imagine a clock. If you passed 12 and 1 into this function, you would get
   * 12 equally spread points around your current location, all a distance of 1
   * away.
   *
   * TODO(samwho): This function could take a radius and cleverly figure out how
   * many points you could have around you for that given radius of object.
   */
  List<MapLocation> getSurroundingLocations(int count, float distance) {
    float step = 360.0f / count;
    float currentAngle = 0.0f;
    List<MapLocation> locations = new ArrayList<>(count);

    for (int i = 0; i < count; i++) {
      Direction d = new Direction(deg2rad(currentAngle));
      locations.add(rc.getLocation().add(d, distance));
      currentAngle += step;
    }

    return locations;
  }

  /**
   * Convert degrees to radians.
   */
  float deg2rad(float deg) {
    return deg * ((float)Math.PI / 180.0f);
  }

  /**
   * Convert radians to degrees.
   */
  float rad2deg(float rad) {
    return rad * (180.0f * (float)Math.PI);
  }

  /**
   * Returns a random Direction.
   */
  Direction randomDirection() {
    return new Direction(deg2rad((float)Math.random() * 360.0f));
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
    // First, try intended direction.
    if (rc.canMove(dir)) {
      rc.move(dir);
      return true;
    }

    // Now try a bunch of similar angles.
    boolean moved = false;
    int currentCheck = 1;

    while(currentCheck <= checksPerSide) {
      // Try the offset of the left side.
      if(rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
        rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
        return true;
      }

      // Try the offset on the right side.
      if(rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
        rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
        return true;
      }

      // No move performed, try slightly further.
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
    float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta));

    return (perpendicularDist <= rc.getType().bodyRadius);
  }
}
