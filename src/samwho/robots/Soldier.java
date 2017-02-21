package samwho.robots;

import battlecode.common.*;

public strictfp class Soldier extends Robot {
  @Override
  public void onIdle() {
    enqueue(0, () -> {
      Team enemy = rc.getTeam().opponent();
      MapLocation myLocation = rc.getLocation();

      // See if there are any nearby enemy robots
      RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

      // If there are some...
      for (RobotInfo target : robots) {
        // And we have enough bullets, and haven't attacked yet this turn...
        if (rc.canFireSingleShot() && !anyFriendliesBetweenMeAnd(target)) {
          // ...Then fire a bullet in the direction of the enemy.
          rc.fireSingleShot(rc.getLocation().directionTo(target.location));
          break;
        }
      }

      // Move randomly
      // TODO(samwho): make them stop walking into their own bullets.
      tryMove(randomDirection());
    });
  }
}
