package samwho.robots;

import samwho.*;
import samwho.actions.*;
import samwho.functional.*;
import samwho.perf.*;

import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import battlecode.common.*;

public abstract strictfp class Robot {
  private static boolean DEBUG_PERFORMANCE = true;

  private static final int STATUS_INTERVAL = 100;

  private static final int DEFAULT_PRIORITY = 0;
  private static final int DEFAULT_RUN_PRIORITY = 0;
  private static final int DEFAULT_BUILD_PRIORITY = 0;

  protected RobotController rc;

  /**
   * The purpose of the action queue is for you to decide how your robots are
   * going to move and attack in a given turn.
   *
   * Every turn, the highest priority action that's possible this turn will run.
   * Possibility is determined by calling an Actions isDoable method. By
   * default, this returns true. It is intended for you to subclass Action and
   * implement this method. See BuildAction for an example.
   */
  private PriorityQueue<Action> actionQueue = new PriorityQueue<>();

  // For checking when the round has changed.
  private int round = 0;

  // For timing stuff. Derp.
  private Timer t;

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
   * Called at most once per round.
   *
   * It's possible that this skips rounds if actions run for a long time.
   */
  public void onNewRound(int round) { }


  /**
   * Called after a BuildAction has finished running.
   */
  public void onBuildFinished(BuildAction ba) { }

  /**
   * Called after a BuildAction has been canceled.
   */
  public void onBuildCanceled(BuildAction ba) { }

  /**
   * Called after a MoveAction has finished running.
   */
  public void onMoveFinished(MoveAction ma) { }

  /**
   * Called after a MoveAction has been canceled.
   */
  public void onMoveCanceled(MoveAction ma) { }

  /**
   * Called after an Action has finished running.
   *
   * This may be called in conjunction with the more specific handlers. Use
   * wisely.
   */
  public void onActionFinished(Action b) { }

  /**
   * Called after an Action has been canceled.
   *
   * This may be called in conjunction with the more specific handlers. Use
   * wisely.
   */
  public void onActionCanceled(Action b) { }

  /**
   * Starts the infinite loop of robot behaviour.
   *
   * After each major chunk of work (e.g. checking the build queue or calling
   * onIdle), we check if the turn has rolled over and if it has we jump back up
   * to the start of the order of operations.
   */
  public void run() throws GameActionException {
    this.round = rc.getRoundNum();
    onCreate();

    while (true) {
      try {
        this.round = rc.getRoundNum();
        t = Timer.create(rc, "r" + this.round);

        if (this.round % STATUS_INTERVAL == 0) statusReport();

        onNewRound(this.round);
        if (this.round != rc.getRoundNum()) continue;

        handleActionPhase();
        if (this.round != rc.getRoundNum()) continue;

        onIdle();
      } finally {
        // If we have time spare, yield it for now. Later we might be able to do
        // something more useful with this time.
        t.close();
        Clock.yield();
      }
    }
  }

  private void statusReport() {
    t.prep();

    Utils.debug_out("--- begin status report ---");
    Utils.debug_out("actionQueue size: " + actionQueue.size());
    for (Action a : actionQueue) {
      Utils.debug_out("  - " + a.getName());
    }
    Utils.debug_out("--- end status report ---");

    t.record("statusReport");
  }

  /**
   * Handles the phase of a turn that scans the action queue and runs what it
   * can.
   */
  private void handleActionPhase() throws GameActionException {
    while (actionQueue.size() > 0 && canDoAnything() &&
        this.round == rc.getRoundNum()) {
      int actionsRun = 0;

      t.prep();
      PriorityQueue<Action> nActionQueue;
      nActionQueue = new PriorityQueue<>(actionQueue);
      actionQueue = new PriorityQueue<>();
      t.record("queue swap");

      for (Action a : nActionQueue) {
        // We used to check for new round here but it leaves the possibility of
        // dropping actions without trying to run them due to how we due the
        // queue copying up there ^
        if (!canDoAnything()) {
          break;
        }

        if (a.isCancelled()) {
          Utils.debug_out(a.getName() + " canceled: " + a.getCancelMessage());
          onCancelledCallback(a);
          continue;
        }

        t.prep();
        boolean success = a.run();
        t.record(a.getName());

        if (a.isCancelled()) {
          Utils.debug_out(a.getName() + " canceled: " + a.getCancelMessage());
          onCancelledCallback(a);
          continue;
        }

        // Action was not successful, so we re-queue it for next turn and go to
        // the next action.
        //
        // TODO(samwho): implement flexible retry behaviour?
        if (!success) {
          actionQueue.add(a);
          continue;
        }

        actionsRun++;
        onFinishCallback(a);
      }

      // If we didn't manage to run any of the actions, we take this to mean
      // that we can't run any actions this turn, so we break out and return.
      if (actionsRun == 0) {
        break;
      }
    }
  }

  private void onFinishCallback(Action a) {
    // Run callbacks only if the action succeeded.
    t.prep();
    if (a instanceof BuildAction) {
      onBuildFinished((BuildAction)a);
      t.record("onBuildFinished");
    } else if (a instanceof MoveAction) {
      onMoveFinished((MoveAction)a);
      t.record("onMoveFinished");
    }

    // Default callback, always called.
    onActionFinished(a);
    t.record("onActionFinished");
  }

  private void onCancelledCallback(Action a) {
    // Run callbacks only if the action succeeded.
    t.prep();
    if (a instanceof BuildAction) {
      onBuildCanceled((BuildAction)a);
      t.record("onBuildCanceled");
    } else if (a instanceof MoveAction) {
      onMoveCanceled((MoveAction)a);
      t.record("onMoveCanceled");
    }

    // Default callback, always called.
    onActionCanceled(a);
    t.record("onActionCanceled");
  }


  /**
   * Checks to see if this robot can still do things to the game world on this
   * turn.
   */
  private boolean canDoAnything() {
    return
      !rc.hasMoved() ||
      rc.canStrike() ||
      rc.canWater() ||
      rc.canShake() ||
      rc.canFirePentadShot() ||
      rc.canFireTriadShot() ||
      rc.canFireSingleShot() ||
      rc.isBuildReady() ||
      rc.hasTreeBuildRequirements();
  }

  public RobotController getRobotController() {
    return this.rc;
  }

  /**
   * Check whether this robot has anything to do.
   */
  public boolean idle() {
    return actionQueue.isEmpty();
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

  public MoveAction moveTo(MapLocation destination) {
    MoveAction ma = new MoveAction(DEFAULT_PRIORITY, this, destination);
    return enqueue(ma);
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
  public <T extends Action> T enqueue(T action) {
    return actionQueue.add(action) ? action : null;
  }

  public RunnableAction run(int priority, String name, GameRunnable runnable) {
    RunnableAction a = new RunnableAction(priority, name, runnable);
    return enqueue(a);
  }

  public RunnableAction run(String name, GameRunnable runnable) {
    return run(DEFAULT_RUN_PRIORITY, name, runnable);
  }

  public BuildAction build(int priority, RobotType type, Direction d) {
    BuildAction ba = new BuildAction(this, priority, type, d);
    return enqueue(ba);
  }

  public BuildAction build(int priority, RobotType type) {
    return build(priority, type, null);
  }

  public BuildAction build(RobotType type, Direction d) {
    return build(DEFAULT_BUILD_PRIORITY, type, d);
  }

  public BuildAction build(RobotType type) {
    return build(DEFAULT_BUILD_PRIORITY, type);
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
    for (MapLocation l : getSurroundingLocations(other.bodyRadius, distance)) {
      if (!rc.isCircleOccupied(l, other.bodyRadius)) {
        return rc.getLocation().directionTo(l);
      }
    }

    return null;
  }

  /**
   * Gets a list surrounding locations around this unit.
   *
   * See Utils.getSurroundingLocations.
   */
  List<MapLocation> getSurroundingLocations(float radius, float distance) {
    return Utils.getSurroundingLocations(rc.getLocation(), radius, distance);
  }

  /**
   * Gets a list surrounding locations around this unit.
   *
   * See Utils.getSurroundingLocations.
   */
  List<MapLocation> getSurroundingLocations(float radius, float distance,
      float offset) {
    return Utils.getSurroundingLocations(
        rc.getLocation(), radius, distance, offset);
  }

  /**
   * Gets a list N surrounding locations around this unit.
   *
   * See Utils.getNSurroundingLocations.
   */
  List<MapLocation> getNSurroundingLocations(int count, float distance) {
    return Utils.getNSurroundingLocations(rc.getLocation(), count, distance);
  }

  boolean isMoveOntoBullet(MapLocation l) {
    RobotType me = rc.getType();

    for (BulletInfo bullet : rc.senseNearbyBullets(me.strideRadius)) {
      if (bulletWillHitMeAt(bullet, l)) {
        return true;
      }
    }

    return false;
  }

  boolean bulletWillHitMeAt(BulletInfo b, MapLocation l) {
    return Utils.lineIntersectsCircle(b.location,
        b.location.add(b.dir, b.speed), l, rc.getType().bodyRadius);
  }

  Direction randomDirectionAvoidingBullets() {
    Direction d;
    int maxAttempts = 32;

    for (int i = 0; i < maxAttempts; i++) {
      d = Utils.randomDirection();
      if (!isMoveOntoBullet(rc.getLocation().add(d))) {
        return d;
      }
    }

    return null;
  }

  public void moveRandomly() {
    Direction d = Utils.randomMovableDirection(rc);
    if (d == null) {
      // We're stuck. Visually indicate and try again next turn.
      rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
      run("get unstuck", () -> moveRandomly());
      return;
    }

    MapLocation l = rc.getLocation().add(d, rc.getType().strideRadius);
    Utils.debug_out("no good locations, moving randomly to: " + l);
    moveTo(l);
  }

  /**
   * Attempts to move in a given direction, while avoiding small obstacles
   * directly in the path.
   *
   * @param dir The intended direction of movement
   * @return true if a move was performed
   * @throws GameActionException
   */
  public boolean tryMove(Direction dir) throws GameActionException {
    return tryMove(dir, rc.getType().bodyRadius, 20, 3);
  }

  /**
   * Attempts to move in a given direction, while avoiding small obstacles
   * directly in the path.
   *
   * @param dir The intended direction of movement
   * @param distance The intended distance of movement
   * @return true if a move was performed
   * @throws GameActionException
   */
  public boolean tryMove(Direction dir, float distance) throws GameActionException {
    return tryMove(dir, distance, 20, 3);
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
  public boolean tryMove(Direction dir, float distance, float degreeOffset,
      int checksPerSide)
    throws GameActionException {
    // First, try intended direction.
    if (rc.canMove(dir, distance)) {
      rc.move(dir, distance);
      return true;
    }

    // Now try a bunch of similar angles.
    boolean moved = false;
    int currentCheck = 1;

    Direction newDirection;

    while(currentCheck <= checksPerSide) {
      // Try the offset of the left side.
      newDirection = dir.rotateLeftDegrees(degreeOffset * currentCheck);
      if(rc.canMove(newDirection, distance)) {
        rc.move(newDirection, distance);
        return true;
      }

      // Try the offset on the right side.
      newDirection = dir.rotateRightDegrees(degreeOffset * currentCheck);
      if(rc.canMove(newDirection, distance)) {
        rc.move(newDirection, distance);
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
