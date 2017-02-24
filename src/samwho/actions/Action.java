package samwho.actions;

import samwho.*;
import samwho.functional.*;

import battlecode.common.*;

public abstract strictfp class Action implements Comparable<Action> {
  protected int priority;
  private GamePredicate shouldCancel;

  public Action(int priority) {
    this.priority = priority;
    this.shouldCancel = () -> false;
  }

  /**
   * Sets a cancellation predicate on the action.
   *
   * Example usage:
   *
   *   enqueue(() -> doThing()).cancelIf(() -> whoops());
   */
  public void cancelIf(GamePredicate shouldCancel) {
    this.shouldCancel = shouldCancel;
  }

  /**
   * Do something that affects the game world.
   *
   * You are expected to make sure that isDoable() returns true before calling
   * this method.
   */
  public abstract void run() throws GameActionException;

  /**
   * Checks if the run method can be run on this turn.
   *
   * There is a guarantee that this method will be called before run(), and
   * run() will not be called until this method returns true.
   *
   * This will be called at most once per turn.
   */
  public boolean isDoable() throws GameActionException {
    return true;
  }

  /**
   * Check to see whether we should cancel this action entirely.
   *
   * If this returns true, the action will be removed from the queue without the
   * run() method being called, and will cease to exist.
   *
   * This will be called at most once per turn.
   */
  public boolean shouldCancel() throws GameActionException {
    return shouldCancel.test();
  }

  /**
   * Ensures priority ordering such that higher priority numbers are considered
   * to be run first.
   */
  @Override
  public int compareTo(Action other) {
    return -Integer.compare(this.priority, other.priority);
  }

  public int getPriority() {
    return this.priority;
  }
}
