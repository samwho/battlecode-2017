package samwho.robots;

import samwho.*;
import samwho.actions.*;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public strictfp class Scout extends Robot {
  @Override
  public void onCreate() {
  }

  @Override
  public void onNewRound(int round) {
  }

  @Override
  public void onIdle() {
    run("plunder", () -> {
      if (!rc.canShake()) {
        return;
      }

      List<TreeInfo> trees = senseNearbyBulletTrees();
      if (trees.isEmpty()) {
        moveRandomly();
        return;
      }

      TreeInfo nearest = trees.get(0);
      if (rc.canShake(nearest.ID)) {
        rc.shake(nearest.ID);
        return;
      }

      moveTo(nearest.location);
    });
  }

  private List<TreeInfo> senseNearbyBulletTrees() throws GameActionException {
    List<TreeInfo> bulletTrees = new ArrayList<>();

    for (TreeInfo tree : rc.senseNearbyTrees()) {
      if (tree.team == Team.NEUTRAL && tree.containedBullets > 0) {
        bulletTrees.add(tree);
      }
    }

    return bulletTrees;
  }
}
