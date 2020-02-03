package com.fran.sim;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
/** Class represents the food source object in the model. */
public class FoodSource implements Steppable {
  /** Represents the current location of the source */
  Int2D location;
  /** True if gorillas has this food source in home range */
  boolean visible;
  /** True if lingering effects of chimpanzees on food source */
  boolean visitedByChimpanzees;
  /** */
  private int visitedCounter;
  /** Represents how much activity has occurred on food source */
  private double heat;

  double getHeat() {
    return heat;
  }

  FoodSource(Int2D location) {
    this.location = location;
    this.visible = false;
    this.visitedByChimpanzees = false;
    this.heat = 0.0;
    this.visitedCounter = Settings.gorillaFoodWaitTime;
  }

  void incrementHeat() {
    heat += 0.2;
    if (heat > 255) heat = 255;
  }

  void setVisible() {
    this.visible = true;
  }

  @Override
  public void step(SimState simState) {
    visitedCounter--;

    if (visitedCounter <= 0) {
      if (visitedByChimpanzees) {
        visitedByChimpanzees = false;
      } else {
        if (simState.random.nextDouble() <= Settings.chimpanzeeEncounter) {
          visitedByChimpanzees = true;
        }
      }
      visitedCounter = Settings.gorillaFoodWaitTime;
    }
  }
}
