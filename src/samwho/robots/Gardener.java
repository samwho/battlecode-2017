package samwho.robots;

import samwho.*;
import samwho.actions.*;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;
import java.util.Set;

public abstract strictfp class Gardener extends Robot {
  private static final int DEFAULT_PLANT_PRIORITY = 0;

  private strictfp class PlantTreeAction extends Action {
    private Direction plantDirection;
    private RobotController rc;

    public PlantTreeAction(int priority, RobotController rc,
        Direction plantDirection) {

      super(priority);
      this.plantDirection = plantDirection;
      this.rc = rc;
    }

    @Override
    public boolean isDoable() throws GameActionException {
      return rc.canPlantTree(plantDirection);
    }

    @Override
    public void run() throws GameActionException {
      rc.plantTree(plantDirection);
    }
  }

  protected PlantTreeAction plant(Direction d) {
    return plant(DEFAULT_PLANT_PRIORITY, d);
  }

  protected PlantTreeAction plant(int priority, Direction d) {
    return enqueue(new PlantTreeAction(priority, rc, d));
  }
}
