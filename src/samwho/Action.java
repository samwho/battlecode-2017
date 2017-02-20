package samwho;
import battlecode.common.*;

public strictfp class Action implements Comparable<Action> {
  protected int priority;
  private GameRunnable action;

  public Action(int priority, GameRunnable action) {
    this.priority = priority;
    this.action = action;
  }

  public void run() throws GameActionException {
    this.action.run();
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
