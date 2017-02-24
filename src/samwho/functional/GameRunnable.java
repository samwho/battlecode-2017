package samwho.functional;

import battlecode.common.*;

/**
 * Simple Runnable-style functional interface for allowing the throwing of a
 * GameActionException inside of run().
 */
@FunctionalInterface
public interface GameRunnable {
  void run() throws GameActionException;
}
