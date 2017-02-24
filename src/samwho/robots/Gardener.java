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

  /**
   * Called whenever a tree has been planted.
   */
  public void onTreePlanted(PlantTreeAction pta) { }

  protected PlantTreeAction plant(Direction d) {
    return plant(DEFAULT_PLANT_PRIORITY, d);
  }

  protected PlantTreeAction plant(int priority, Direction d) {
    return enqueue(new PlantTreeAction(priority, this, d));
  }
}
