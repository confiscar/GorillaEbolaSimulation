package com.fran.sim;

/**
 * Settings class controls the static options that will affect the simulation generation/running.
 * Settings should be accessible from a static instance anywhere its imported.
 *
 * @author Francisco Caeiro
 */
public class Settings {
  /** Amount of groups of gorillas in the habitat */
  public static final int groupsOfGorillas = 20;
  /** Amount of food sources in grid boundary */
  public static final int amountFoodSources = 200;
  /** Defines a (2*n + 1)^2 area where the food can be placed */
  public static final int foodSpreadingIntensity = 20;
  /** Maximum quantity of gorillas per group */
  public static final int maxPopulation = 17;
  /** Minimum quantity of gorillas per group */
  public static final int minPopulation = 8;
  /** Area of home range will be radius*2 by radius*2 */
  public static final int homerangeRadius = 100;
  /** How long each side of the cell is in metres. Used to calculate density */
  public static final int cellSideLength = 100;
  /** Time in days each group of gorillas stays around a food source */
  public static final int gorillaFoodWaitTime = 7;
  /** Timer to delete an item off a gorillas memory */
  public static final int gorillaMemoryLength = 0;
  /** Chance of encountering a chimpanzee on a tile **/
  public static final double chimpanzeeEncounter = 0.001;

  /** Hides unvisited food sources */
  public static final boolean hideUnusedFoodSources = true;
  /** Enables or disables heat map of food sources */
  public static final boolean enableHeatMap = true;
  /** Enables or disables the printing of interaction records */
  public static final boolean enableRecordPrinting = true;
}
