package com.fran;

/**
 * Settings class controls the static options that will affect the simulation generation/running.
 * Settings should be accessible from a static instance anywhere its imported.
 *
 * @author Francisco Caeiro
 */
public class Settings {
  /** Amount of groups of gorillas in the habitat */
  public static final int groupsOfGorillas = 100;
  /** Amount of food sources in grid boundary */
  public static final int amountFoodSources = 200;
  /** How much the food sources will spread from the centre */
  public static final int foodSpreadingIntensity = 20;
  /** Maximum quantity of gorillas per group */
  public static final int maxPopulation = 100;
  /** Minimum quantity of gorillas per group */
  public static final int minPopulation = 50;
  /** Area of home range will be radius*2 by radius*2 */
  public static final int homerangeRadius = 5;
  /** Time in days each group of gorillas stays around a food source */
  public static final int gorillaFoodWaitTime = 7;
  /** Length of gorillas memory */
  public static final int gorillaFoodMemoryNumber = 4;
  /** Timer to delete an item off a gorillas memory */
  public static final int gorillaMemoryDeletionTime = 10;
  /** Hides unvisited food sources */
  public static final boolean hideUnusedFoodSources = true;
  /** Enables or disables heat map of food sources */
  public static final boolean enableHeatMap = true;
}
