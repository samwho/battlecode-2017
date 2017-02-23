package samwho.robots;

import samwho.*;

import battlecode.common.*;

public strictfp class Lumberjack extends Robot {
  @Override
  public void onIdle() {
    enqueue(0, () -> {
      Team enemy = rc.getTeam().opponent();

      // See if there are any enemy robots within striking range (distance 1 from
      // lumberjack's radius)
      RobotInfo[] robots = rc.senseNearbyRobots(
          RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS,
          enemy);

      if(robots.length > 0 && !rc.hasAttacked()) {
        // Use strike() to hit all nearby robots!
        rc.strike();
        return;
      }

      // No close robots, so search for robots within sight radius
      robots = rc.senseNearbyRobots(-1,enemy);

      // If there is a robot, move towards it
      if(robots.length > 0) {
        MapLocation myLocation = rc.getLocation();
        MapLocation enemyLocation = robots[0].getLocation();
        Direction toEnemy = myLocation.directionTo(enemyLocation);

        tryMove(toEnemy);
        return;
      }

      // Move Randomly
      tryMove(Utils.randomDirection());
    });
  }
}
