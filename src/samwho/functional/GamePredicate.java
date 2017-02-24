package samwho.functional;

import battlecode.common.*;

/**
 * Simple Predicate-like functional interface for allowing you to throw a
 * GameActionException in the test() method.
 */
@FunctionalInterface
public interface GamePredicate {
  boolean test() throws GameActionException;
}
