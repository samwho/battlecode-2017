package samwho.robots;

import samwho.*;
import samwho.actions.*;

import battlecode.common.*;

public strictfp class Archon extends Robot {
  private static final int MAX_NUM_GARDENERS = 2;
  private int gardenerCount = 0;

  @Override
  public void onCreate() {
    build(RobotType.GARDENER);
  }

  @Override
  public void onBuildFinished(BuildAction ba) {
    if (ba.getType() == RobotType.GARDENER) {
      this.gardenerCount++;
    }
  }

  @Override
  public void onNewRound(int round) {
    if (round % 100 == 0 && gardenerCount < MAX_NUM_GARDENERS) {
      build(RobotType.GARDENER);
    }
  }

  @Override
  public void onIdle() {
    run(() -> {
      if (rc.hasMoved()) {
        Clock.yield();
        return;
      }

      // Simple donation strategy.
      if (rc.getTeamBullets() > 5000) {
        rc.donate(1000);
      }

      // Move randomly
      tryMove(Utils.randomDirection());

      // Broadcast archon's location for other robots on the team to
      // know
      MapLocation myLocation = rc.getLocation();
      rc.broadcastFloat(0, myLocation.x);
      rc.broadcastFloat(1, myLocation.y);
    });
  }
}
