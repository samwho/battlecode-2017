package samwho;

import battlecode.common.*;

@FunctionalInterface
public interface GamePredicate {
  boolean run() throws GameActionException;
}
