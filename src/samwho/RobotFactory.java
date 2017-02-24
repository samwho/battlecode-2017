package samwho;

import samwho.robots.*;

import battlecode.common.*;

import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Map;

public strictfp class RobotFactory {
  // The default strategy. Eventually I'll branch out into trying more
  // strategies, but for the time being this is it.
  static final Map<RobotType, Supplier<Robot>> DEFAULT_STRATEGY =
    new HashMap<RobotType, Supplier<Robot>>() {{

    put(RobotType.ARCHON,     () -> new Archon());
    put(RobotType.GARDENER,   () -> new HexagonalSpawnerGardener());
    put(RobotType.SOLDIER,    () -> new Soldier());
    put(RobotType.LUMBERJACK, () -> new Lumberjack());
  }};

  /**
   * A "strategy" is, for the time being, simply a map of RobotType to Robot
   * supplier that will implement the behaviour for that type.
   *
   * A known limitation of this is that you can't switch a robot's type mid-way
   * through a game.
   */
  static final Map<RobotType, Supplier<Robot>> STRATEGY = DEFAULT_STRATEGY;

  public static Robot create(RobotController rc) {
    Supplier<Robot> s = STRATEGY.get(rc.getType());

    // No mapping for this robot type. Handle that further up the stack.
    if (s == null) {
      return null;
    }

    Robot r = s.get();

    // Always be sure to pass the robot its RobotController, otherwise it won't
    // be able to do anything meaningful.
    r.setRobotController(rc);

    return r;
  }
}
