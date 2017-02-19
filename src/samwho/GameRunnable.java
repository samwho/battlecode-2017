package samwho;

import battlecode.common.*;

@FunctionalInterface
public interface GameRunnable {
  void run() throws GameActionException;
}
