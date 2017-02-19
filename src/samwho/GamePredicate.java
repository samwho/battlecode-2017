package samwho;

import battlecode.common.*;

@FunctionalInterface
public interface GamePredicate {
  boolean test() throws GameActionException;
}
