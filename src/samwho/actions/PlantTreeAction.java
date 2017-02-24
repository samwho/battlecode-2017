package samwho.actions;

import samwho.*;
import samwho.robots.*;

import battlecode.common.*;

/**
 * An action for planting a tree in a given direction.
 */
public strictfp class PlantTreeAction extends Action {
  private Direction plantDirection;
  private Gardener planter;
  private RobotController rc;

  public PlantTreeAction(int priority, Gardener planter,
      Direction plantDirection) {

    super(priority, "plant tree at angle " + plantDirection.getAngleDegrees());
    this.plantDirection = plantDirection;
    this.planter = planter;
    this.rc = planter.getRobotController();
  }

  @Override
  public boolean isDoable() throws GameActionException {
    return rc.canPlantTree(plantDirection);
  }

  @Override
  public void run() throws GameActionException {
    rc.plantTree(plantDirection);
    planter.onTreePlanted(this);
  }
}
