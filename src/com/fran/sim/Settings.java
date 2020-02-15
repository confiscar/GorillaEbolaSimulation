package com.fran.sim;

/**
 * Settings class controls the static options that will affect the simulation generation/running.
 * Settings should be accessible from a static instance anywhere its imported.
 *
 * @author Francisco Caeiro
 */
public class Settings {
  /** Amount of groups of gorillas in the habitat */
  public static int groupsOfGorillas = 100;
  /** Amount of food sources in grid boundary */
  public static int amountFoodSources = 1000;
  /** Defines a (2*n + 1)^2 area where the food can be placed */
  public static int foodSpreadingIntensity = 30;
  /** Maximum quantity of gorillas per group */
  public static int maxPopulation = 17;
  /** Minimum quantity of gorillas per group */
  public static int minPopulation = 8;
  /** Area of home range will be radius*2 by radius*2 */
  public static int homerangeRadius = 3;
  /** How long each side of the cell is in metres. Used to calculate density */
  public static int cellSideLength = 100;
  /** Time in days each group of gorillas stays around a food source */
  public static int gorillaFoodWaitTime = 7;
  /** Timer to delete an item off a gorillas memory */
  public static int gorillaMemoryLength = 3;
  /** Chance of encountering a chimpanzee on a tile */
  public static double chimpanzeeEncounter = 0.01;
  /** Time that the effects of the chimpanzees linger */
  public static int chimpanzeeLingerTime = 7;

  /** Hides unvisited food sources */
  public static boolean hideUnusedFoodSources = true;
  /** Enables or disables heat map of food sources */
  public static boolean enableHeatMap = true;
  /** Enables or disables the printing of interaction records */
  public static boolean enableRecordPrinting = true;
}
