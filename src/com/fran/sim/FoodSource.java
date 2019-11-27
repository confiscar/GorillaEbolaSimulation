package com.fran.sim;

import sim.util.Int2D;
/** Class represents the food source object in the model. */
public class FoodSource {
  /** Represents the current location of the source */
  Int2D location;
  /** True if gorillas has this food source in home range */
  boolean visible;
  /** Represents how much activity has occurred on food source */
  private double heat;

  double getHeat() {
    return heat;
  }

  FoodSource(Int2D location) {
    this.location = location;
    this.visible = false;
    this.heat = 0.0;
  }

  public void incrementHeat() {
    heat += 0.2;
    if (heat > 255) heat = 255;
  }

  public void setVisible(boolean value){this.visible = value;}
}
