package samwho.robots;

import battlecode.common.*;

public strictfp class Archon extends Robot {
  private static final int MAX_NUM_GARDENERS = 2;
  private int gardenerCount = 0;

  @Override
  public void onCreate() {
    build(RobotType.GARDENER);
  }

  @Override
  public void onBuildFinished(RobotType type) {
    if (type == RobotType.GARDENER) {
      this.gardenerCount++;
    }
  }

  @Override
  public void onNewTurn() {
    if (rc.getRoundNum() % 100 == 0 && gardenerCount <= MAX_NUM_GARDENERS) {
      build(RobotType.GARDENER);
    }
  }

  @Override
  public void onIdle() {
    enqueue(0, () -> {
      if (rc.hasMoved()) {
        Clock.yield();
        return;
      }

      // Move randomly
      tryMove(randomDirection());

      // Broadcast archon's location for other robots on the team to
      // know
      MapLocation myLocation = rc.getLocation();
      rc.broadcastFloat(0, myLocation.x);
      rc.broadcastFloat(1, myLocation.y);
    });
  }
}
