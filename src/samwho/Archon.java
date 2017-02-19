package samwho;
import battlecode.common.*;

public strictfp class Archon extends Robot {
  public Archon(RobotController rc) {
    super(rc);
  }

  @Override
  public void firstTurn() throws GameActionException {
    ensureDo(() -> {
      Direction d = getUnoccupiedDirectionAroundMe();

      if (d == null) {
        out("couldn't find unoccupied location");
        return false;
      }

      if (!rc.canHireGardener(d)) {
        out("can't hire a gardener right now");
        return false;
      }

      rc.hireGardener(d);
      return true;
    });
  }

  @Override
  public void doTurn() throws GameActionException {
    // Move randomly
    tryMove(randomDirection());

    // Broadcast archon's location for other robots on the team to
    // know
    MapLocation myLocation = rc.getLocation();
    rc.broadcast(0,(int)myLocation.x);
    rc.broadcast(1,(int)myLocation.y);
  }
}
