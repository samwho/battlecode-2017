package samwho.robots;

import samwho.*;

import battlecode.common.*;

public strictfp class Soldier extends Robot {
  @Override
  public void onNewTurn() {
    enqueue(0, () -> {
      if (!rc.hasMoved()) {
        Direction d = randomDirectionAvoidingBullets();
        if (d != null) {
          tryMove(d);
        }
      }

      if (rc.canFireSingleShot()) {
        RobotInfo target = findEnemyToShootAt();

        if (target != null) {
          Direction shoot = rc.getLocation().directionTo(target.location);
          rc.fireSingleShot(shoot);
        }
      }
    });
  }

  RobotInfo findEnemyToShootAt() throws GameActionException {
    RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

    for (RobotInfo target : robots) {
      if (!anyFriendliesBetweenMeAnd(target)) {
        // ...Then fire a bullet in the direction of the enemy.
        return target;
      }
    }

    return null;
  }
}
