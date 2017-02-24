package samwho.actions;

import samwho.*;
import samwho.functional.*;

import battlecode.common.*;

/**
 * A generic action that calls a GameRunnable in its run() method.
 *
 * Use this when you want to do something for which there exists no other
 * subclass of Action.
 */
public strictfp class RunnableAction extends Action {
  private GameRunnable runnable;

  public RunnableAction(int priority, GameRunnable runnable) {
    super(priority);
    this.runnable = runnable;
  }

  @Override
  public void run() throws GameActionException {
    this.runnable.run();
  }
}
