package samwho.actions;

import samwho.*;
import samwho.robots.*;

import battlecode.common.*;

/**
 * An action for building units.
 */
public strictfp class BuildAction extends Action {
  private Robot builder;
  private RobotType type;
  private Direction direction;

  public BuildAction(Robot builder, int priority, RobotType t, Direction d) {
    super(priority, "build " + t);

    this.builder = builder;
    this.type = t;
    this.direction = d;
  }

  public BuildAction(Robot builder, int priority, RobotType type) {
    this(builder, priority, type, null);
  }

  @Override
  public boolean run() throws GameActionException {
    RobotController rc = builder.getRobotController();

    Direction buildDir = direction;
    if (buildDir == null) {
      // If no direction was specified when the BuildAction was created, we need
      // to find an appropriate direction every time we try the build, because
      // the user doesn't mind where the build happens.
      buildDir = builder.getUnoccupiedBuildDirectionFor(type);
    }

    if (buildDir == null) {
      // No good build direction available this turn, try again next time.
      return false;
    }

    if (!rc.canBuildRobot(type, buildDir)) {
      return false;
    }

    builder.getRobotController().buildRobot(type, buildDir);
    return true;
  }

  public RobotType getType() {
    return this.type;
  }

  public Direction getDirection() {
    return this.direction;
  }
}

