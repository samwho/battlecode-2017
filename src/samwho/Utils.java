package samwho;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public strictfp class Utils {
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
    // is tangential to the circle, and we decide to include that as a
    // collision.
    return lec <= radius;
  }

  /**
   * Gets a list surrounding locations that could fit a circle of given radius
   * and distance away from you.
   *
   * If the circles do not fit exactly, they will be evenly spaced around your
   * location.
   *
   * @param center the central location to spread the points around
   * @param radius the radius of the circles surrounding the center
   * @param distance how far away the points should be
   * @param offset the offset, in radians, to start at
   */
  public static List<MapLocation> getSurroundingCircles(MapLocation center,
      float radius, float distance, float offset) {
    double opposite = (double)radius;
    double hypotenuse = (double)distance;
    double wedgeAngle = Math.asin(opposite / hypotenuse) * 2;
    int numWedges = (int)((Math.PI * 2) / wedgeAngle);

    double step = (Math.PI * 2) / numWedges;
    double currentAngle = offset;
    List<MapLocation> locations = new ArrayList<>(numWedges);

    for (int i = 0; i < numWedges; i++) {
      Direction d = new Direction((float)currentAngle);
      locations.add(center.add(d, distance));
      currentAngle += step;
    }

    return locations;
  }

  /**
   * See getSurroundingCircles(MapLocation, float, float, float).
   */
  public static List<MapLocation> getSurroundingCircles(MapLocation center,
      float radius, float distance) {
    return getSurroundingCircles(center, radius, distance, 0.0f);
  }

  /**
   * Returns a random Direction.
   */
  public static Direction randomDirection() {
    return new Direction((float)(Math.random() * (Math.PI * 2)));
  }

}
