package samwho;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public strictfp class Utils {
  public static Random random = new Random();

  /**
   * Debug-only output.
   *
   * Will be stripped from tournament and scrimming games, and won't count
   * towards the bytecode limit.
   */
  public static void debug_out(String message) {
    System.out.println(message);
  }

  /**
   * Calculation shamelessly stolen from http://stackoverflow.com/a/1088058.
   *
   * The idea is to calculate whether a line segment will intersect a circle,
   * and the intention is to use it to avoid moving into spaces that will get us
   * shot.
   */
  public static boolean lineIntersectsCircle(MapLocation a, MapLocation b,
      MapLocation c, float radius) {
    // compute the euclidean distance between A and B
    double lab = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));

    // compute the direction vector D from A to B
    double dx = (b.x - a.x) / lab;
    double dy = (b.y - a.y) / lab;

    // Now the line equation is x = Dx*t + Ax, y = Dy*t + Ay with 0 <= t <= 1.

    // compute the value t of the closest point to the circle center (Cx, Cy)
    double t = dx * (c.x - a.x) + dy * (c.y - a.y);

    // This is the projection of C on the line from A to B.

    // compute the coordinates of the point E on line and closest to C
    double ex = t * dx + a.x;
    double ey = t * dy + a.y;

    // compute the euclidean distance from E to C
    double lec = Math.sqrt(Math.pow(ex - c.x, 2) + Math.pow(ey - c.y, 2));

    // test if the line intersects the circle. The == case means that the line
    // is tangential to the circle.
    return lec < radius;
  }

  /**
   * Gets a non-overlapping list surrounding locations that could fit a circle
   * of given radius and distance away from you.
   *
   * If the circles do not fit exactly, they will be evenly spaced around your
   * location.
   *
   * This method is suited to trying to find locations to spawn multiple
   * non-overlapping things.
   *
   * @param center the central location to spread the points around
   * @param radius the radius of the circles surrounding the center
   * @param distance how far away the points should be
   * @param offset the offset, in radians, to start at
   */
  public static List<MapLocation> getSurroundingLocations(MapLocation center,
      float radius, float distance, float offset) {
    double opposite = (double)radius;
    double hypotenuse = (double)distance;
    double wedgeAngle = Math.asin(opposite / hypotenuse) * 2;
    int numWedges = (int)((Math.PI * 2) / wedgeAngle);

    return getNSurroundingLocations(center, numWedges, distance, offset);
  }

  /**
   * Gets a given number of equally spaced locations a given distance away from
   * a given point.
   *
   * Useful for scanning for a location to build a single thing.
   */
  public static List<MapLocation> getNSurroundingLocations(MapLocation center,
      int count, float distance, float offset) {
    double step = (Math.PI * 2) / count;
    double currentAngle = offset;
    List<MapLocation> locations = new ArrayList<>(count);

    for (int i = 0; i < count; i++) {
      Direction d = new Direction((float)currentAngle);
      locations.add(center.add(d, distance));
      currentAngle += step;
    }

    return locations;
  }

  /**
   * See getSurroundingLocations(MapLocation, float, float, float).
   */
  public static List<MapLocation> getSurroundingLocations(MapLocation center,
      float radius, float distance) {
    return getSurroundingLocations(center, radius, distance, 0.0f);
  }

  /**
   * See getNSurroundingLocations(MapLocation, int, float, float).
   */
  public static List<MapLocation> getNSurroundingLocations(MapLocation center,
      int count, float distance) {
    return getNSurroundingLocations(center, count, distance, 0.0f);
  }

  /**
   * Returns a random Direction.
   */
  public static Direction randomDirection() {
    return new Direction((float)(Math.random() * (Math.PI * 2)));
  }

  public static List<BodyInfo> senseNearbyEverything(RobotController rc,
      float distance) {
    RobotInfo[] robots   = rc.senseNearbyRobots(distance);
    TreeInfo[] trees     = rc.senseNearbyTrees(distance);
    BulletInfo[] bullets = rc.senseNearbyBullets(distance);

    List<BodyInfo> things = new ArrayList<>(
        robots.length + trees.length + bullets.length);

    for (RobotInfo  robot  : robots)  things.add(robot);
    for (TreeInfo   tree   : trees)   things.add(tree);
    for (BulletInfo bullet : bullets) things.add(bullet);

    return things;
  }

  public static List<BodyInfo> senseNearbyRobotsAndTrees(RobotController rc,
      float distance) {
    RobotInfo[] robots = rc.senseNearbyRobots(distance);
    TreeInfo[] trees   = rc.senseNearbyTrees(distance);

    List<BodyInfo> things = new ArrayList<>(robots.length + trees.length);
    for (RobotInfo robot : robots) things.add(robot);
    for (TreeInfo  tree  : trees)  things.add(tree);

    return things;
  }

  /**
   * Picks a random direction that it's possible to walk in this turn.
   */
  public static Direction randomMovableDirection(RobotController rc) {
    MapLocation myLocation = rc.getLocation();
    float strideRadius = rc.getType().strideRadius;
    float body = rc.getType().bodyRadius;
    float offset = random.nextFloat() * (float)(Math.PI * 2);

    List<MapLocation> potentialLocations = getNSurroundingLocations(
        myLocation, 16, strideRadius, offset);

    for (MapLocation l : potentialLocations) {
      if (rc.canMove(l)) return myLocation.directionTo(l);
    }

    return null;
  }
}
