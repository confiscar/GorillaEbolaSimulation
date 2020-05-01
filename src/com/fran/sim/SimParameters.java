package com.fran.sim;

public class SimParameters {
  /** Amount of groups of gorillas in the habitat */
  public static int groupsOfGorillas = 100;
  /** Maximum quantity of gorillas per group */
  public static int maxPopulation = 17;
  /** Minimum quantity of gorillas per group */
  public static int minPopulation = 8;
  /** Amount of food sources in grid boundary */
  public static int amountFoodSources = 150;
  /** Defines a (2*n + 1)^2 area where the food can be placed */
  public static int foodSpreadingIntensity = 14;
  /** Area of home range will be radius*2 by radius*2 */
  public static int homerangeRadius = 2;
  /** How long each side of the cell is in metres. Used to calculate density */
  public static int cellSideLength = 100;
  /** Time in days each group of gorillas stays around a food source */
  public static int gorillaFoodWaitTime = 7;
  /** Timer to delete an item off a gorillas memory */
  public static int gorillaMemoryLength = 3;
  /** Chance of encountering a chimpanzee on a tile */
  public static double chimpanzeeEncounter = 0.001;
  /** Time that the effects of the chimpanzees linger */
  public static int chimpanzeeLingerTime = 7;
  /** Rate of increase of the chimpanzee probability after every infected gorilla */
  public static double chimpanzeeInfectionProbabilityRate = 0.0;
  /** Probability of transmission between a single gorilla to a single gorilla */
  public static double transmissionProbability = 0.36826;
  /** Probability of a gorilla recovering after getting the virus */
  public static double recoveryProbability = 0.51213;
  /** Time to recover or die from disease. Measured as n * gorillaFoodWaitTime */
  public static int infectionTime = 3;
  /** Probability that when the silverback dies, an individual gorilla will move to another group */
  public static double probabilityOfDispersal = 0.5;
}
