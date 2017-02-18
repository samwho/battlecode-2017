package samwho;
import battlecode.common.*;

public strictfp class Soldier extends Robot {
  public Soldier(RobotController rc) {
    super(rc);
  }

  @Override
  public void doTurn() throws GameActionException {
    Team enemy = rc.getTeam().opponent();

    MapLocation myLocation = rc.getLocation();

    // See if there are any nearby enemy robots
    RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

    // If there are some...
    if (robots.length > 0) {
      // And we have enough bullets, and haven't attacked yet this turn...
      if (rc.canFireSingleShot()) {
        // ...Then fire a bullet in the direction of the enemy.
        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
      }
    }

    // Move randomly
    tryMove(randomDirection());
  }
}
