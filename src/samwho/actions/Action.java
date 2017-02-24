package samwho.actions;

import samwho.*;

import battlecode.common.*;

public abstract strictfp class Action implements Comparable<Action> {
  protected int priority;
  private GamePredicate shouldCancel;

  public Action(int priority) {
    this.priority = priority;
    this.shouldCancel = () -> false;
  }

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
   */
  public boolean isDoable() throws GameActionException {
    return true;
  }

  /**
   * Check to see whether we should cancel this action entirely.
   */
  public boolean shouldCancel() throws GameActionException {
    return shouldCancel.test();
  }

  @Override
  public int compareTo(Action other) {
    // Negative for descending order (higher numbers means higher priority).
    return -Integer.compare(this.priority, other.priority);
  }

  public int getPriority() {
    return this.priority;
  }
}
