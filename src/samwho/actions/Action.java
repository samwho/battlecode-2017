package samwho.actions;

import samwho.*;
import samwho.functional.*;

import battlecode.common.*;

/**
 * Base Action class.
 *
 * TODO(samwho): there seems to be an infinite loop possibility where actions
 * are doable but their run method doesn't actually complete them. While this is
 * a bug in the implementation of a given action, it might be worth implementing
 * some sort of guard that only attempts to run an action once per turn.
 */
public abstract strictfp class Action implements Comparable<Action> {
  protected int priority;
  private GamePredicate shouldCancel;
  private String name;

  public Action(int priority, String name) {
    this.priority = priority;
    this.name = name;
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

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
