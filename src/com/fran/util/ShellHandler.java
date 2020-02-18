package com.fran.util;

import com.fran.sim.Settings;
import picocli.CommandLine;

public class ShellHandler {

  @CommandLine.Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "display a help message")
  private boolean helpRequested = false;

  @CommandLine.Option(
      names = {"-hr", "--homerange"},
      paramLabel = "HOMERANGE VALUE (INT)",
      description = "an integer that represents the radius of the home range of the gorillas")
  public int homeRange = -1;

  @CommandLine.Option(
      names = {"-g", "--gorillas"},
      paramLabel = "NUMBER OF GORILLA GROUPS (INT)",
      description = "number of gorillas group in the simulation")
  public int gorillas = -1;

  @CommandLine.Option(
      names = {"-f", "--food"},
      paramLabel = "NUMBER OF FOOD SOURCES (INT)",
      description = "number of food sources in the simulation")
  public int foodSources = -1;

  @CommandLine.Option(
      names = {"-s", "--spread"},
      paramLabel = "DISTANCE FOOD SOURCES ARE SPREAD OVER (INT)",
      description = "Spreads the food sources in a (2*n + 1)^2 area of cells")
  public int spread = -1;

  @CommandLine.Option(
      names = {"-mxp", "--maxpopulation"},
      paramLabel = "MAX NUMBER OF GORILLAS IN A GROUP (INT)",
      description = "upper boundary of the number of gorillas in a group")
  public int maxPopulation = -1;

  @CommandLine.Option(
      names = {"-mnp", "--minpopulation"},
      paramLabel = "MIN NUMBER OF GORILLAS IN A GROUP (INT)",
      description = "lower boundary of the number of gorillas in a group")
  public int minPopulation = -1;

  @CommandLine.Option(
      names = {"-cp", "--chimpprobability"},
      paramLabel = "PROBABILITY OF CHIMP APPEARING ON TILE (DOUBLE)",
      description =
          "probability between 0 and 1 that describes how often a chimpanzee will be seen on a tile")
  public double chimpanzeeProbability = -1.0;

  @CommandLine.Option(
      names = {"-cl", "--chimplength"},
      paramLabel = "LENGTH OF CHIMP APPEARING ON TILE (DOUBLE)",
      description = "length in weeks that describes how long a chimpanzee will be seen on a tile")
  public int chimpanzeeLength = -1;

  public void handleInputs() {
    if (homeRange != -1) {
      System.out.println("Setting home range to " + homeRange);
      Settings.homerangeRadius = homeRange;
    }
    if (gorillas != -1) {
      System.out.println("Setting number of gorilla groups to " + gorillas);
      Settings.groupsOfGorillas = gorillas;
    }
    if (foodSources != -1) {
      System.out.println("Setting number of food sources to " + foodSources);
      Settings.amountFoodSources = foodSources;
    }
    if (spread != -1) {
      int areaSide = 2 * spread + 1;
      System.out.println(
          "Setting spread of food sources to an " + areaSide + " x " + areaSide + " area");
      Settings.foodSpreadingIntensity = spread;
    }
    if (maxPopulation != -1) {
      System.out.println("Setting max population of gorilla groups to " + maxPopulation);
      Settings.maxPopulation = maxPopulation;
    }
    if (minPopulation != -1) {
      System.out.println("Setting minimum population of gorilla groups to " + minPopulation);
      Settings.minPopulation = minPopulation;
    }
    if (chimpanzeeProbability != -1) {
      System.out.println("Setting chimpanzee probability to " + chimpanzeeProbability);
      Settings.chimpanzeeEncounter = chimpanzeeProbability;
    }
    if (chimpanzeeLength != -1) {
      System.out.println("Setting chimpanzee linger time to " + chimpanzeeLength + " weeks");
      Settings.chimpanzeeLingerTime = chimpanzeeLength * 7;
    }
  }
}
