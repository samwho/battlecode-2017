package samwho;

import battlecode.common.*;

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
   * Convert degrees to radians.
   */
  public static float deg2rad(float deg) {
    return deg * ((float)Math.PI / 180.0f);
  }

  /**
   * Convert radians to degrees.
   */
  public static float rad2deg(float rad) {
    return rad * (180.0f * (float)Math.PI);
  }

  /**
   * Returns a random Direction.
   */
  public static Direction randomDirection() {
    return new Direction(Utils.deg2rad((float)Math.random() * 360.0f));
  }

}
