package samwho.actions;

import samwho.*;

import battlecode.common.*;

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
