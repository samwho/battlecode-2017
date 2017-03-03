package samwho;

import battlecode.common.*;

public strictfp class Comms {
  private static final int ENEMY_LOCATION_X = 0;
  private static final int ENEMY_LOCATION_Y = 1;

  private RobotController rc;

  public Comms(RobotController rc) {
    this.rc = rc;
  }

  public void broadcastEnemyLocation(MapLocation l) throws GameActionException {
    rc.broadcastFloat(ENEMY_LOCATION_X, l.x);
    rc.broadcastFloat(ENEMY_LOCATION_Y, l.y);
  }

  public MapLocation readEnemyLocation() throws GameActionException {
    float x = rc.readBroadcastFloat(ENEMY_LOCATION_X);
    float y = rc.readBroadcastFloat(ENEMY_LOCATION_Y);

    if (x == 0 || y == 0) {
      return null;
    }

    return new MapLocation(x, y);
  }
}
