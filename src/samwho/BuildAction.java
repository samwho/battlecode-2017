package samwho;

import battlecode.common.*;

public strictfp class BuildAction extends Action {
  private Robot builder;
  private RobotType type;
  private Direction direction;

  public BuildAction(Robot builder, int priority, RobotType t, Direction d) {
    super(priority);

    this.builder = builder;
    this.type = t;
    this.direction = d;
  }

  public BuildAction(Robot builder, int priority, RobotType type) {
    this(builder, priority, type, null);
  }

  @Override
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

  @Override
  public void run() throws GameActionException {
    RobotController rc = builder.getRobotController();
    rc.buildRobot(type, direction);
  }
}

