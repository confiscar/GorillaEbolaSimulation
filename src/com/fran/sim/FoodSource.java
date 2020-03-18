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
  /** Boolean that represents if the food source has infected someone in the current step */
  boolean infectedInCurrentStep;
  /** Amount of days needed to pass for the next possible chimpanzee visit */
  private int visitedCounter;
  /** Probability of a visited tile to pass on an infection */
  static double infectionProbability = Settings.transmissionProbability;
  /** Amount of days that the effect of chimpanzees at a food source lasts */
  private int lingerCounter;
  /** Represents how much activity has occurred on food source */
  private double heat;

  /** Java Bean to display the heat of the tile (roughly signifies traffic in the tile) */
  double getHeat() {
    return heat;
  }

  FoodSource(Int2D location) {
    this.location = location;
    this.visible = false;
    this.visitedByChimpanzees = false;
    this.heat = 0.0;
    this.visitedCounter = Settings.gorillaFoodWaitTime;
    this.lingerCounter = Settings.chimpanzeeLingerTime;
    this.infectedInCurrentStep = false;
  }

  @Override
  public void step(SimState simState) {
    infectedInCurrentStep = false;

    if (visitedByChimpanzees) {
      lingerCounter--;
      if (lingerCounter <= 0) {
        visitedByChimpanzees = false;
        lingerCounter = Settings.chimpanzeeLingerTime;
      }
    } else {
      visitedCounter--;
      if (visitedCounter <= 0) {
        if (simState.random.nextDouble() <= Settings.chimpanzeeEncounter)
          visitedByChimpanzees = true;
        visitedCounter = Settings.gorillaFoodWaitTime;
      }
    }
  }

  /** Augments the infection probability when an infected gorilla group is on the tile */
  void incrementInfectionProbability() {
    if (infectionProbability < 1.0) {
      infectionProbability += Settings.chimpanzeeInfectionProbabilityRate;
      if (infectionProbability > 1.0) infectionProbability = 1.0;
    }
  }

  /** Augments the heat variable when a gorilla group is on the tile */
  void incrementHeat() {
    heat += 0.2;
    if (heat > 255) heat = 255;
  }

  /** Used in map generation to hide any food sources that cannot be reached */
  void setVisible() {
    this.visible = true;
  }
}
