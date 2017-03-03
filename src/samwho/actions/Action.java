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
  private boolean cancel = false;
  private String cancelMessage = "";
  private int bytecodeCost = 0;

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
   * If this method returns true, the action is considered done. If it returns
   * false, the action is considered failed this turn.
   *
   * It's possible that this method is run more than once per turn, and you are
   * expected to code defensively.
   */
  public abstract boolean run() throws GameActionException;

  /**
   * Check to see whether we should cancel this action entirely.
   *
   * If this returns true, the action will be removed from the queue without the
   * run() method being called, and will cease to exist.
   *
   * This will be called at most once per turn.
   */
  public boolean isCancelled() throws GameActionException {
    return cancel || shouldCancel.test();
  }

  public String getCancelMessage() {
    return this.cancelMessage;
  }

  public void cancel(String message) {
    this.cancelMessage = message;
    cancel();
  }

  public void cancel() {
    this.cancel = true;
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

  public void setBytecodeCost(int bytecodeCost) {
    this.bytecodeCost = bytecodeCost;
  }

  public int getBytecodeCost() {
    return this.bytecodeCost;
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
