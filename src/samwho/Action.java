package samwho;
import battlecode.common.*;

public abstract strictfp class Action implements Comparable<Action> {
  protected int priority;

  public Action(int priority) {
    this.priority = priority;
  }

  /**
   * Do something that affects the game world.
   *
   * You are expected to make sure that isDoable() returns true before calling
   * this method.
   */
  public abstract void run() throws GameActionException;

  /**
   * Checks if the run method will succeed or fail.
   *
   * There is a guarantee that this method will be called before run(), and
   * run() will not be called until this method returns true.
   */
  public boolean isDoable() throws GameActionException {
    return true;
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
