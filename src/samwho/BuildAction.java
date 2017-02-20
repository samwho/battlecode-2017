package samwho;

import battlecode.common.*;

public strictfp class BuildAction implements Comparable<BuildAction> {
  private Robot builder;
  protected int priority;
  private RobotType type;
  private Direction direction;

  public BuildAction(Robot builder, int p, RobotType t, Direction d) {
    this.builder = builder;
    this.priority = p;
    this.type = t;
    this.direction = d;
  }

  public BuildAction(Robot builder, int priority, RobotType type) {
    this(builder, priority, type, null);
  }

  public boolean isDoable() throws GameActionException {
    RobotController rc = builder.getRobotController();

    if (direction == null) {
      direction = builder.getUnoccupiedBuildDirectionFor(type);

      if (direction == null) {
        Utils.debug_out(
            "can't build " + type.name() + ", unable to find location");

        return false;
      }

      if (!rc.canBuildRobot(type, direction)) {
        Utils.debug_out("can't build " + type.name());
        direction = null;
        return false;
      }

      return true;
    }

    return rc.canBuildRobot(type, direction);
  }

  public void run() throws GameActionException {
    RobotController rc = builder.getRobotController();
    rc.buildRobot(type, direction);
  }

  @Override
  public int compareTo(BuildAction other) {
    // Negative for descending order (higher numbers means higher priority).
    return -Integer.compare(this.priority, other.priority);
  }
}

