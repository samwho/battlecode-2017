package samwho.actions;

import samwho.*;

import battlecode.common.*;

public strictfp class GuardedAction extends RunnableAction {
  private GamePredicate predicate;

  public GuardedAction(int priority, GamePredicate p, GameRunnable r) {
    super(priority, r);

    this.predicate = p;
  }

  @Override
  public boolean isDoable() throws GameActionException {
    return this.predicate.test();
  }
}
